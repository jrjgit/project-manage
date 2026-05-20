package main

import (
	"log"
	"net/http"
	"os"
	"path/filepath"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
	"management/config"
	"management/db"
	"management/handlers"
	"management/internal/notifier"
	"management/middlewares"
	"management/models"
)

func main() {
	// 优先加载 .env 文件（如果存在），不覆盖已设置的环境变量
	_ = config.LoadEnvFile(".env")

	cfg := config.Load()

	if err := db.Init(cfg); err != nil {
		log.Fatalf("failed to initialize database: %v", err)
	}

	createDefaultAdmin()

	gin.SetMode(gin.ReleaseMode)

	r := gin.Default()

	r.Use(func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Origin, Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

	// 初始化邮件通知器
	var emailNotifier notifier.Notifier
	if cfg.NotifyEmailEnable && cfg.SMTPHost != "" {
		emailNotifier = notifier.NewEmailNotifier(cfg)
	}

	// 初始化 nanobot 通知器
	var nanobotNotifier notifier.Notifier
	if cfg.NotifyNanobotEnable && cfg.NanobotPath != "" {
		nanobotNotifier = notifier.NewNanobotNotifier(cfg)
	}

	// 异步包装
	if cfg.NotifyAsync {
		if emailNotifier != nil {
			emailNotifier = notifier.NewAsyncNotifier(emailNotifier, 100)
		}
		if nanobotNotifier != nil {
			nanobotNotifier = notifier.NewAsyncNotifier(nanobotNotifier, 100)
		}
	}

	authHandler := handlers.NewAuthHandler(cfg)
	taskHandler := handlers.NewTaskHandler(cfg, emailNotifier, nanobotNotifier)
	bugHandler := handlers.NewBugHandler(cfg, emailNotifier, nanobotNotifier)
	userHandler := handlers.NewUserHandler()
	projectHandler := handlers.NewProjectHandler()
	groupHandler := handlers.NewGroupHandler()

	authMiddleware := middlewares.AuthRequired(cfg)

	api := r.Group("/api")
	{
		api.POST("/auth/register", authHandler.Register)
		api.POST("/auth/login", authHandler.Login)

		authorized := api.Group("", authMiddleware)
		{
			authorized.GET("/tasks", taskHandler.ListTasks)
			authorized.POST("/tasks", middlewares.RoleRequired(models.RolePM), taskHandler.CreateTask)
			authorized.GET("/tasks/:id", taskHandler.GetTask)
			authorized.PUT("/tasks/:id", taskHandler.UpdateTask)
			authorized.PATCH("/tasks/:id/status", taskHandler.ChangeTaskStatusHandler)
			authorized.GET("/tasks/:id/history", taskHandler.GetTaskHistory)
		authorized.POST("/tasks/:id/assignees", middlewares.RoleRequired(models.RolePM, models.RoleDevLead), taskHandler.AddTaskAssignee)
		authorized.DELETE("/tasks/:id/assignees/:user_id", middlewares.RoleRequired(models.RolePM, models.RoleDevLead), taskHandler.RemoveTaskAssignee)

			authorized.GET("/bugs", bugHandler.ListBugs)
			authorized.POST("/bugs", middlewares.RoleRequired(models.RoleTester, models.RoleTesterLead), bugHandler.CreateBug)
			authorized.GET("/bugs/:id", bugHandler.GetBug)
			authorized.PUT("/bugs/:id", bugHandler.UpdateBug)
			authorized.PATCH("/bugs/:id/status", bugHandler.ChangeBugStatusHandler)
			authorized.GET("/bugs/:id/history", bugHandler.GetBugHistory)

			authorized.GET("/users", middlewares.RoleRequired(models.RolePM, models.RoleDevLead, models.RoleDev, models.RoleTester, models.RoleTesterLead), userHandler.ListUsers)
			authorized.GET("/users/:id", middlewares.RoleRequired(models.RolePM), userHandler.GetUser)
			authorized.PUT("/users/:id/role", middlewares.RoleRequired(models.RolePM), userHandler.UpdateUserRole)

			authorized.GET("/projects", projectHandler.ListProjects)
			authorized.POST("/projects", middlewares.RoleRequired(models.RolePM), projectHandler.CreateProject)
			authorized.GET("/projects/:id", projectHandler.GetProject)
			authorized.PUT("/projects/:id", middlewares.RoleRequired(models.RolePM), projectHandler.UpdateProject)
			authorized.DELETE("/projects/:id", middlewares.RoleRequired(models.RolePM), projectHandler.DeleteProject)

			authorized.GET("/groups", groupHandler.ListGroups)
			authorized.POST("/groups", middlewares.RoleRequired(models.RolePM), groupHandler.CreateGroup)
			authorized.GET("/groups/my-team", middlewares.RoleRequired(models.RoleDevLead), groupHandler.GetMyTeam)
			authorized.GET("/groups/:id", groupHandler.GetGroup)
			authorized.PUT("/groups/:id", middlewares.RoleRequired(models.RolePM), groupHandler.UpdateGroup)
			authorized.DELETE("/groups/:id", middlewares.RoleRequired(models.RolePM), groupHandler.DeleteGroup)
			authorized.POST("/groups/:id/members", middlewares.RoleRequired(models.RolePM), groupHandler.AddMember)
			authorized.DELETE("/groups/:id/members/:user_id", middlewares.RoleRequired(models.RolePM), groupHandler.RemoveMember)
		}
	}

	staticPath := filepath.Join("..", "frontend", "dist")
	if _, err := os.Stat(staticPath); os.IsNotExist(err) {
		staticPath = filepath.Join("frontend", "dist")
	}

	if info, err := os.Stat(staticPath); err == nil && info.IsDir() {
		r.Static("/assets", filepath.Join(staticPath, "assets"))
		r.StaticFile("/", filepath.Join(staticPath, "index.html"))
		r.NoRoute(func(c *gin.Context) {
			c.File(filepath.Join(staticPath, "index.html"))
		})
	} else {
		r.GET("/", func(c *gin.Context) {
			c.String(http.StatusOK, "Management API is running. Frontend not built yet.")
		})
	}

	log.Printf("Server starting on port %s...", cfg.Port)
	if err := r.Run(":" + cfg.Port); err != nil {
		log.Fatalf("failed to start server: %v", err)
	}
}

func createDefaultAdmin() {
	var count int64
	db.DB.Model(&models.User{}).Where("role = ?", models.RolePM).Count(&count)
	if count == 0 {
		log.Println("No PM user found, creating default admin (admin/admin123)")
		hashedPassword, err := bcrypt.GenerateFromPassword([]byte("admin123"), bcrypt.DefaultCost)
		if err != nil {
			log.Printf("failed to hash default admin password: %v", err)
			return
		}
		admin := models.User{
			Name:     "admin",
			Password: string(hashedPassword),
			Role:     models.RolePM,
		}
		if result := db.DB.Create(&admin); result.Error != nil {
			log.Printf("failed to create default admin: %v", result.Error)
			return
		}
		log.Printf("Default admin created successfully, id=%d", admin.ID)
	}
}
