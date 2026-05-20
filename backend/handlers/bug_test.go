package handlers

import (
	"fmt"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"management/config"
	"management/db"
	"management/internal/notifier"
	"management/models"
)

func TestChangeBugStatus_FixedMustMoveToPendingVerify(t *testing.T) {
	setupBugTestDB(t)

	var groupID uint = 3
	pm := createBugTestUser(t, "pm", models.RolePM, nil)
	dev := createBugTestUser(t, "dev", models.RoleDev, &groupID)
	tester := createBugTestUser(t, "tester", models.RoleTester, nil)
	project := createBugTestProject(t, "Bug Project", pm.ID)
	task := createBugTestTask(t, models.Task{Title: "Task For Bug", Status: models.TaskTesting, ProjectID: project.ID, CreatorID: pm.ID})
	bug := createBugRecord(t, models.Bug{Title: "Bug A", Status: models.BugFixing, TaskID: task.ID, CreatorID: tester.ID, AssigneeID: &dev.ID, Severity: "high"})

	h := NewBugHandler(&config.Config{}, notifier.NewLogNotifier(), notifier.NewLogNotifier())
	err := h.ChangeBugStatus(bug.ID, models.BugFixed, dev.ID, "patched and ready")
	if err != nil {
		t.Fatalf("expected fixed transition to succeed before asserting semantics: %v", err)
	}

	var updated models.Bug
	if err := db.DB.First(&updated, bug.ID).Error; err != nil {
		t.Fatalf("load updated bug: %v", err)
	}
	if updated.Status != models.BugPendingVerify {
		t.Fatalf("expected final status %s, got %s", models.BugPendingVerify, updated.Status)
	}
}

func TestChangeBugStatus_TesterCanCloseFromPendingVerify(t *testing.T) {
	setupBugTestDB(t)

	var groupID uint = 4
	pm := createBugTestUser(t, "pm", models.RolePM, nil)
	dev := createBugTestUser(t, "dev", models.RoleDev, &groupID)
	tester := createBugTestUser(t, "tester", models.RoleTester, nil)
	project := createBugTestProject(t, "Bug Project", pm.ID)
	task := createBugTestTask(t, models.Task{Title: "Task For Verify", Status: models.TaskTesting, ProjectID: project.ID, CreatorID: pm.ID})
	bug := createBugRecord(t, models.Bug{Title: "Bug Verify", Status: models.BugPendingVerify, TaskID: task.ID, CreatorID: tester.ID, AssigneeID: &dev.ID, Severity: "medium"})

	h := NewBugHandler(&config.Config{}, notifier.NewLogNotifier(), notifier.NewLogNotifier())
	if err := h.ChangeBugStatus(bug.ID, models.BugClosed, tester.ID, "verified"); err != nil {
		t.Fatalf("expected tester to close from pending_verify, got error: %v", err)
	}

	var updated models.Bug
	if err := db.DB.First(&updated, bug.ID).Error; err != nil {
		t.Fatalf("load updated bug: %v", err)
	}
	if updated.Status != models.BugClosed {
		t.Fatalf("expected final status %s, got %s", models.BugClosed, updated.Status)
	}
}

func setupBugTestDB(t *testing.T) {
	t.Helper()
	gin.SetMode(gin.TestMode)

	dbPath := fmt.Sprintf("file:bug_test_%s?mode=memory&cache=shared", t.Name())
	if err := db.Init(&config.Config{DBPath: dbPath}); err != nil {
		t.Fatalf("init test db: %v", err)
	}
}

func createBugTestUser(t *testing.T, name, role string, groupID *uint) models.User {
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

func createBugTestProject(t *testing.T, name string, pmID uint) models.Project {
	t.Helper()
	project := models.Project{Name: name, PMID: pmID, CreatedAt: time.Now()}
	if err := db.DB.Create(&project).Error; err != nil {
		t.Fatalf("create project %s: %v", name, err)
	}
	return project
}

func createBugTestTask(t *testing.T, task models.Task) models.Task {
	t.Helper()
	if task.Priority == "" {
		task.Priority = "medium"
	}
	if err := db.DB.Create(&task).Error; err != nil {
		t.Fatalf("create task %s: %v", task.Title, err)
	}
	return task
}

func createBugRecord(t *testing.T, bug models.Bug) models.Bug {
	t.Helper()
	if err := db.DB.Create(&bug).Error; err != nil {
		t.Fatalf("create bug %s: %v", bug.Title, err)
	}
	return bug
}
