package handlers

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"management/config"
	"management/db"
	"management/models"
)

func TestListTasks_DevSeesOnlyAssignedTasks(t *testing.T) {
	setupTaskTestDB(t)

	var groupID uint = 7
	pm := createTaskTestUser(t, "pm-user", models.RolePM, nil)
	dev := createTaskTestUser(t, "dev-user", models.RoleDev, &groupID)
	otherDev := createTaskTestUser(t, "other-dev", models.RoleDev, &groupID)
	devLead := createTaskTestUser(t, "dev-lead", models.RoleDevLead, &groupID)

	project := createTaskTestProject(t, "Alpha", pm.ID)
	createTaskRecord(t, models.Task{Title: "Assigned To Dev", Status: models.TaskDeveloping, ProjectID: project.ID, CreatorID: pm.ID, AssigneeID: &dev.ID, DevLeadID: &devLead.ID})
	createTaskRecord(t, models.Task{Title: "Assigned To Other Dev", Status: models.TaskDeveloping, ProjectID: project.ID, CreatorID: pm.ID, AssigneeID: &otherDev.ID, DevLeadID: &devLead.ID})

	h := NewTaskHandler(&config.Config{})
	r := gin.New()
	r.GET("/tasks", func(c *gin.Context) {
		c.Set("userID", dev.ID)
		c.Set("userRole", models.RoleDev)
		c.Set("groupID", dev.GroupID)
		h.ListTasks(c)
	})

	req := httptest.NewRequest(http.MethodGet, "/tasks", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d with body %s", w.Code, w.Body.String())
	}

	var tasks []models.Task
	if err := json.Unmarshal(w.Body.Bytes(), &tasks); err != nil {
		t.Fatalf("unmarshal tasks response: %v", err)
	}

	if len(tasks) != 1 {
		t.Fatalf("expected 1 task for dev, got %d", len(tasks))
	}
	if tasks[0].Title != "Assigned To Dev" {
		t.Fatalf("expected Assigned To Dev, got %s", tasks[0].Title)
	}
}

func TestChangeTaskStatus_DevelopedCreatesPendingTestHistory(t *testing.T) {
	setupTaskTestDB(t)

	var groupID uint = 9
	pm := createTaskTestUser(t, "pm", models.RolePM, nil)
	dev := createTaskTestUser(t, "dev", models.RoleDev, &groupID)
	project := createTaskTestProject(t, "Project", pm.ID)

	task := createTaskRecord(t, models.Task{
		Title:      "Feature A",
		Status:     models.TaskDeveloping,
		ProjectID:  project.ID,
		CreatorID:  pm.ID,
		AssigneeID: &dev.ID,
	})

	h := NewTaskHandler(&config.Config{})
	if err := h.ChangeTaskStatus(task.ID, models.TaskDeveloped, dev.ID, "ready for qa"); err != nil {
		t.Fatalf("expected status change to succeed: %v", err)
	}

	var updated models.Task
	if err := db.DB.First(&updated, task.ID).Error; err != nil {
		t.Fatalf("load updated task: %v", err)
	}
	if updated.Status != models.TaskPendingTest {
		t.Fatalf("expected final status %s, got %s", models.TaskPendingTest, updated.Status)
	}

	var histories []models.TaskStatusHistory
	if err := db.DB.Where("task_id = ?", task.ID).Order("id asc").Find(&histories).Error; err != nil {
		t.Fatalf("load task histories: %v", err)
	}

	if len(histories) != 2 {
		t.Fatalf("expected 2 history rows, got %d", len(histories))
	}
	if histories[0].FromStatus != models.TaskDeveloping || histories[0].ToStatus != models.TaskDeveloped {
		t.Fatalf("expected first history developing->developed, got %s->%s", histories[0].FromStatus, histories[0].ToStatus)
	}
	if histories[1].FromStatus != models.TaskDeveloped || histories[1].ToStatus != models.TaskPendingTest {
		t.Fatalf("expected second history developed->pending_test, got %s->%s", histories[1].FromStatus, histories[1].ToStatus)
	}
}

func setupTaskTestDB(t *testing.T) {
	t.Helper()
	gin.SetMode(gin.TestMode)

	dbPath := fmt.Sprintf("file:task_test_%s?mode=memory&cache=shared", t.Name())
	if err := db.Init(&config.Config{DBPath: dbPath}); err != nil {
		t.Fatalf("init test db: %v", err)
	}
}

func createTaskTestUser(t *testing.T, name, role string, groupID *uint) models.User {
	t.Helper()
	user := models.User{
		Name:      name,
		Password:  "secret",
		Role:      role,
		GroupID:   groupID,
		CreatedAt: time.Now(),
	}
	if err := db.DB.Create(&user).Error; err != nil {
		t.Fatalf("create user %s: %v", name, err)
	}
	return user
}

func createTaskTestProject(t *testing.T, name string, pmID uint) models.Project {
	t.Helper()
	project := models.Project{Name: name, PMID: pmID, CreatedAt: time.Now()}
	if err := db.DB.Create(&project).Error; err != nil {
		t.Fatalf("create project %s: %v", name, err)
	}
	return project
}

func createTaskRecord(t *testing.T, task models.Task) models.Task {
	t.Helper()
	if task.Priority == "" {
		task.Priority = "medium"
	}
	if err := db.DB.Create(&task).Error; err != nil {
		t.Fatalf("create task %s: %v", task.Title, err)
	}
	return task
}
