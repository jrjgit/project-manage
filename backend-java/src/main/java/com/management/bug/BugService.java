package com.management.bug;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.management.bug.dto.*;
import com.management.bug.entity.*;
import com.management.bug.mapper.*;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.common.notification.NotificationService;
import com.management.common.workflow.WorkflowService;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BugService {
    private final BugMapper bugMapper;
    private final BugStatusHistoryMapper historyMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 按角色过滤 Bug 列表 */
    public List<Bug> listBugs(String taskId, String status, String severity) {
        JwtUserDetails u = currentUser();
        LambdaQueryWrapper<Bug> q = new LambdaQueryWrapper<>();

        switch (u.getRole()) {
            case "pm":
                break;
            case "dev_lead":
                q.eq(Bug::getAssigneeId, u.getUserId());
                break;
            case "dev":
                q.eq(Bug::getAssigneeId, u.getUserId());
                break;
            case "tester":
                q.and(w -> w.eq(Bug::getCreatorId, u.getUserId())
                        .or().eq(Bug::getStatus, "pending_verify")
                        .or().eq(Bug::getStatus, "reopened"));
                break;
            default:
                q.apply("1=0");
        }

        if (taskId != null && !taskId.isBlank()) q.eq(Bug::getTaskId, taskId);
        if (status != null && !status.isBlank()) q.eq(Bug::getStatus, status);
        if (severity != null && !severity.isBlank()) q.eq(Bug::getSeverity, severity);
        q.orderByDesc(Bug::getUpdatedAt);

        List<Bug> bugs = bugMapper.selectList(q);
        for (Bug b : bugs) fillAssociations(b);
        return bugs;
    }

    /** 创建 Bug */
    @Transactional
    public Bug createBug(CreateBugRequest req) {
        if (taskMapper.selectById(req.getTaskId()) == null)
            throw new BusinessException(400, "关联任务不存在");
        if (req.getAssigneeId() != null && userMapper.selectById(req.getAssigneeId()) == null)
            throw new BusinessException(400, "指派人不存在");

        Long userId = currentUser().getUserId();
        Bug bug = new Bug();
        bug.setTitle(req.getTitle());
        bug.setDescription(req.getDescription());
        bug.setSeverity(req.getSeverity() != null ? req.getSeverity() : "medium");
        bug.setStatus("unfixed");
        bug.setTaskId(req.getTaskId());
        bug.setCreatorId(userId);
        bug.setAssigneeId(req.getAssigneeId());
        bug.setTestType(req.getTestType() != null ? req.getTestType() : "integration");
        // 从关联任务获取需求ID
        if (req.getTaskId() != null) {
            Task t = taskMapper.selectById(req.getTaskId());
            if (t != null) bug.setRequirementId(t.getRequirementId());
        }
        bugMapper.insert(bug);

        fillAssociations(bug);

        // 通知被指派人
        if (bug.getAssigneeId() != null) {
            User target = userMapper.selectById(bug.getAssigneeId());
            if (target != null) {
                notificationService.emitBugEvent(bug, "new", "assigned",
                        currentUser().getName(), List.of(target), "");
            }
        }

        log.info("Bug created: id={}, title={}", bug.getId(), bug.getTitle());
        return bug;
    }

    /** 获取 Bug 详情 */
    public Bug getBug(Long id) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw new BusinessException(404, "bug not found");
        fillAssociations(bug);
        return bug;
    }

    /** 更新 Bug */
    public Bug updateBug(Long id, UpdateBugRequest req) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw new BusinessException(404, "bug not found");
        Long oldAssigneeId = bug.getAssigneeId();
        if (req.getTitle() != null && !req.getTitle().isBlank()) bug.setTitle(req.getTitle());
        if (req.getDescription() != null) bug.setDescription(req.getDescription());
        if (req.getSeverity() != null && !req.getSeverity().isBlank()) bug.setSeverity(req.getSeverity());
        if (req.getTaskId() != null) {
            if (taskMapper.selectById(req.getTaskId()) == null)
                throw new BusinessException(400, "关联任务不存在");
            bug.setTaskId(req.getTaskId());
            Task t = taskMapper.selectById(req.getTaskId());
            if (t != null) bug.setRequirementId(t.getRequirementId());
        }
        if (req.getAssigneeId() != null) {
            if (userMapper.selectById(req.getAssigneeId()) == null)
                throw new BusinessException(400, "指派人不存在");
            bug.setAssigneeId(req.getAssigneeId());
        }
        if (req.getRemark() != null) bug.setRemark(req.getRemark());
        if (req.getTestType() != null) bug.setTestType(req.getTestType());
        bugMapper.updateById(bug);

        // assignee_id 变化时通知新指派人
        if (req.getAssigneeId() != null && (oldAssigneeId == null || !req.getAssigneeId().equals(oldAssigneeId))) {
            User newAssignee = userMapper.selectById(req.getAssigneeId());
            if (newAssignee != null) {
                notificationService.emitGenericEvent(
                        "Bug【" + bug.getTitle() + "】您被指派为处理人，操作人：" + currentUser().getName(),
                        currentUser().getName(), List.of(newAssignee));
            }
        }

        fillAssociations(bug);
        return bug;
    }

    /** 变更 Bug 状态 */
    @Transactional
    public void changeStatus(Long bugId, ChangeBugStatusRequest req) {
        Long operatorId = currentUser().getUserId();
        String role = currentUser().getRole();
        Bug bug = bugMapper.selectById(bugId);
        if (bug == null) throw new BusinessException("Bug 不存在");

        if (("dev".equals(role) || "dev_lead".equals(role))
                && !operatorId.equals(bug.getAssigneeId())) {
            throw new BusinessException("只能操作指派给自己的 Bug");
        }

        String oldStatus = bug.getStatus();

        if (!workflowService.isAllowed(role, oldStatus, req.getNewStatus(), "bug")) {
            throw new BusinessException(
                    String.format("角色 %s 不能将 Bug 从 %s 变更为 %s", role, oldStatus, req.getNewStatus()));
        }

        bug.setStatus(req.getNewStatus());
        bugMapper.updateById(bug);

        BugStatusHistory history = new BugStatusHistory();
        history.setBugId(bugId);
        history.setFromStatus(oldStatus);
        history.setToStatus(req.getNewStatus());
        history.setChangedBy(operatorId);
        history.setComment(req.getComment());
        historyMapper.insert(history);

        log.info("Bug {} status changed: {} -> {} by user {}",
                bugId, oldStatus, req.getNewStatus(), operatorId);

        String operatorName = currentUser().getName();
        List<User> targets = resolveBugNotifyTargets(bug, oldStatus, req.getNewStatus());
        notificationService.emitBugEvent(bug, oldStatus, req.getNewStatus(), operatorName, targets, req.getComment());

        if (req.getComment() != null && !req.getComment().isBlank()) {
            bugMapper.update(null, new LambdaUpdateWrapper<Bug>()
                    .eq(Bug::getId, bugId).set(Bug::getRemark, req.getComment()));
        }
    }

    /** 获取 Bug 状态历史 */
    public List<BugStatusHistory> getBugHistory(Long id) {
        List<BugStatusHistory> list = historyMapper.selectList(
                new LambdaQueryWrapper<BugStatusHistory>()
                        .eq(BugStatusHistory::getBugId, id)
                        .orderByDesc(BugStatusHistory::getChangedAt));
        for (BugStatusHistory h : list) {
            h.setUser(userMapper.selectById(h.getChangedBy()));
        }
        return list;
    }

    private List<User> resolveBugNotifyTargets(Bug bug, String oldStatus, String newStatus) {
        List<User> targets = new ArrayList<>();
        String key = oldStatus + "->" + newStatus;
        switch (key) {
            case "new->assigned":
            case "reopened->assigned":
                if (bug.getAssigneeId() != null) addUser(targets, bug.getAssigneeId());
                break;
            case "assigned->fixing":
                addUser(targets, bug.getCreatorId());
                break;
            case "fixing->fixed":
            case "fixed->pending_verify":
                addUser(targets, bug.getCreatorId());
                break;
            case "pending_verify->closed":
            case "pending_verify->reopened":
                if (bug.getAssigneeId() != null) addUser(targets, bug.getAssigneeId());
                break;
        }
        return targets;
    }

    private void addUser(List<User> targets, Long userId) {
        User u = userMapper.selectById(userId);
        if (u != null) targets.add(u);
    }

    private void fillAssociations(Bug b) {
        if (b.getTaskId() != null) b.setTask(taskMapper.selectById(b.getTaskId()));
        if (b.getCreatorId() != null) b.setCreator(userMapper.selectById(b.getCreatorId()));
        if (b.getAssigneeId() != null) b.setAssignee(userMapper.selectById(b.getAssigneeId()));
    }

    public void uploadImage(Long id, MultipartFile file) throws IOException {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw new BusinessException(404, "Bug不存在");
        JwtUserDetails u = currentUser();
        if (!u.getUserId().equals(bug.getCreatorId()) && !"pm".equals(u.getRole()))
            throw new BusinessException(403, "无权限上传图片");
        String dir = uploadDir + "/bugs/" + id;
        java.io.File dirFile = new java.io.File(dir);
        if (!dirFile.exists()) dirFile.mkdirs();
        String ext = "";
        int dot = file.getOriginalFilename().lastIndexOf('.');
        if (dot >= 0) ext = file.getOriginalFilename().substring(dot);
        String name = java.util.UUID.randomUUID().toString() + ext;
        file.transferTo(new java.io.File(dirFile, name));
        bug.setImagePath("bugs/" + id + "/" + name);
        bug.setImageName(file.getOriginalFilename());
        bug.setImageSize(file.getSize());
        bugMapper.updateById(bug);
    }

    public void downloadImage(Long id, HttpServletResponse response) throws IOException {
        Bug bug = bugMapper.selectById(id);
        if (bug == null || bug.getImagePath() == null) throw new BusinessException(404, "图片不存在");
        java.io.File f = new java.io.File(uploadDir + "/" + bug.getImagePath());
        if (!f.exists()) throw new BusinessException(404, "文件不存在");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(bug.getImageName(), "UTF-8"));
        org.springframework.util.FileCopyUtils.copy(new java.io.FileInputStream(f), response.getOutputStream());
    }

    public void deleteImage(Long id) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw new BusinessException(404, "Bug不存在");
        JwtUserDetails u = currentUser();
        if (!u.getUserId().equals(bug.getCreatorId()) && !"pm".equals(u.getRole()))
            throw new BusinessException(403, "无权限删除图片");
        if (bug.getImagePath() != null) {
            java.io.File f = new java.io.File(uploadDir + "/" + bug.getImagePath());
            if (f.exists()) f.delete();
        }
        bug.setImagePath(null); bug.setImageName(null); bug.setImageSize(null);
        bugMapper.updateById(bug);
    }
}
