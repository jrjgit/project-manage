package com.management.requirement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.bug.BugService;
import com.management.bug.entity.Bug;
import com.management.bug.mapper.BugMapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.common.notification.NotificationService;
import com.management.iteration.entity.Iteration;
import com.management.iteration.mapper.IterationMapper;
import com.management.project.mapper.ProjectMapper;
import com.management.requirement.dto.CreateRequirementRequest;
import com.management.requirement.dto.RequirementDetailDTO;
import com.management.requirement.dto.UpdateRequirementRequest;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementService {
    private final RequirementMapper requirementMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final IterationMapper iterationMapper;
    private final TaskMapper taskMapper;
    private final BugMapper bugMapper;
    private final BugService bugService;
    private final NotificationService notificationService;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.uploadDir = new java.io.File(uploadDir).getAbsolutePath();
    }

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    @Transactional
    public Requirement create(CreateRequirementRequest req) {
        if (req.getProjectId() != null && projectMapper.selectById(req.getProjectId()) == null)
            throw new BusinessException(400, "所属项目不存在");

        String projectType = req.getProjectType();
        if (projectType == null && req.getProjectId() != null) {
            com.management.project.entity.Project p = projectMapper.selectById(req.getProjectId());
            projectType = "invite_bidding".equals(p.getProjectType()) ? "project" : "ops";
        }

        Requirement r = new Requirement();
        r.setDescription(req.getDescription());
        r.setNotes(req.getNotes());
        r.setSystem(req.getSystem());
        r.setProjectId(req.getProjectId());
        r.setProjectType(projectType);
        r.setPersonId(req.getPersonId());
        r.setPersonName(req.getPersonName());
        r.setPriority(req.getPriority() != null ? req.getPriority() : "medium");
        r.setStatus("pending_task");
        r.setTotalAmount(req.getTotalAmount());
        r.setTotalPrice(req.getTotalPrice());
        r.setDevTotal(req.getDevTotal());
        r.setDevPrice(req.getDevPrice());
        r.setTestTotal(req.getTestTotal());
        r.setTestPrice(req.getTestPrice());
        if (req.getPlannedCompletionTime() != null)
            r.setPlannedCompletionTime(java.time.OffsetDateTime.parse(req.getPlannedCompletionTime()).toLocalDateTime());
        r.setIterationId(req.getIterationId());
        if (req.getBusinessStatus() != null) r.setBusinessStatus(req.getBusinessStatus());
        r.setRequirementId("TEMP");
        r.setNumber("TEMP");
        requirementMapper.insert(r);
        r.setRequirementId("REQ-" + java.time.Year.now().getValue() + "-" + String.format("%03d", r.getId()));
        if (req.getNumber() != null && !req.getNumber().isBlank()) {
            r.setNumber(req.getNumber());
        } else {
            r.setNumber(r.getRequirementId());
        }
        requirementMapper.updateById(r);
        fillAssociations(r);

        JwtUserDetails operator = currentUser();
        List<User> notifyTargets = new ArrayList<>();
        if (r.getDevLeadId() != null) {
            User devLead = userMapper.selectById(r.getDevLeadId());
            if (devLead != null) notifyTargets.add(devLead);
        }
        if (r.getPersonId() != null) {
            User person = userMapper.selectById(r.getPersonId());
            if (person != null) notifyTargets.add(person);
        }
        if (!notifyTargets.isEmpty()) {
            String msg = "需求【" + r.getDescription() + "】已创建，当前状态：待拆任务，操作人：" + operator.getName();
            notificationService.emitGenericEvent(msg, operator.getName(), notifyTargets);
        }

        log.info("Requirement created: id={}, description={}", r.getRequirementId(), r.getDescription());
        return r;
    }

    public List<Requirement> list(String status, String system, String projectType) {
        LambdaQueryWrapper<Requirement> q = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            q.eq(Requirement::getStatus, status);
        } else {
            // 默认排除已发布和已关闭的需求
            q.notIn(Requirement::getStatus, Arrays.asList("released", "closed"));
        }
        if (system != null && !system.isBlank()) q.eq(Requirement::getSystem, system);
        if (projectType != null && !projectType.isBlank()) q.eq(Requirement::getProjectType, projectType);
        applyProjectScopeFilter(q);
        q.orderByDesc(Requirement::getUpdatedAt);
        List<Requirement> list = requirementMapper.selectList(q);
        for (Requirement r : list) { fillAssociations(r); fillProgress(r); }
        return list;
    }

    public List<Requirement> opsList() {
        LambdaQueryWrapper<Requirement> q = new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getProjectType, "ops")
                .ne(Requirement::getStatus, "released");
        applyProjectScopeFilter(q);
        q.orderByDesc(Requirement::getUpdatedAt);
        List<Requirement> list = requirementMapper.selectList(q);
        for (Requirement r : list) { fillAssociations(r); fillProgress(r); }
        return list;
    }

    public List<Requirement> projectList() {
        LambdaQueryWrapper<Requirement> q = new LambdaQueryWrapper<Requirement>()
                .eq(Requirement::getProjectType, "project")
                .ne(Requirement::getStatus, "released");
        applyProjectScopeFilter(q);
        q.orderByDesc(Requirement::getUpdatedAt);
        List<Requirement> list = requirementMapper.selectList(q);
        for (Requirement r : list) { fillAssociations(r); fillProgress(r); }
        return list;
    }

    public RequirementDetailDTO getDetail(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        fillAssociations(r);

        RequirementDetailDTO dto = toDetailDTO(r);

        // compute dev progress by terminal
        List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getRequirementId, id));
        java.util.Map<String, java.util.List<Task>> byTerminal = tasks.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getTerminal() != null ? t.getTerminal() : "其他"));
        RequirementDetailDTO.DevProgress dp = new RequirementDetailDTO.DevProgress();
        java.util.List<RequirementDetailDTO.TerminalProgress> terminals = new java.util.ArrayList<>();
        for (var entry : byTerminal.entrySet()) {
            RequirementDetailDTO.TerminalProgress tp = new RequirementDetailDTO.TerminalProgress();
            tp.setTerminal(entry.getKey());
            java.util.List<Task> tl = entry.getValue();
            tp.setTotal(tl.size());
            long done = tl.stream().filter(t -> "closed".equals(t.getStatus()) || "passed".equals(t.getStatus())).count();
            tp.setDone(done);
            int avgProgress = (int) Math.round(tl.stream().mapToInt(t -> t.getProgress() != null ? t.getProgress() : 0).average().orElse(0));
            tp.setProgress(avgProgress);
            long overdue = tl.stream().filter(t -> t.getDeadline() != null
                    && t.getDeadline().isBefore(java.time.LocalDateTime.now())
                    && !"closed".equals(t.getStatus()) && !"passed".equals(t.getStatus())).count();
            tp.setOverdueDays(overdue);
            java.util.List<RequirementDetailDTO.TaskBrief> briefs = tl.stream().map(t -> {
                RequirementDetailDTO.TaskBrief b = new RequirementDetailDTO.TaskBrief();
                b.setId(t.getId());
                b.setTitle(t.getTitle());
                b.setStatus(t.getStatus());
                return b;
            }).collect(java.util.stream.Collectors.toList());
            tp.setTasks(briefs);
            terminals.add(tp);
        }
        dp.setTerminals(terminals);
        dto.setDevProgress(dp);

        // bugs
        dto.setIntegrationTestBugs(bugMapper.selectList(
                new LambdaQueryWrapper<Bug>()
                        .eq(Bug::getRequirementId, id)
                        .eq(Bug::getTestType, "integration")));
        dto.setBusinessTestBugs(bugMapper.selectList(
                new LambdaQueryWrapper<Bug>()
                        .eq(Bug::getRequirementId, id)
                        .eq(Bug::getTestType, "business")));
        dto.setItTestBugs(bugMapper.selectList(
                new LambdaQueryWrapper<Bug>()
                        .eq(Bug::getRequirementId, id)
                        .eq(Bug::getTestType, "it_test")));

        return dto;
    }

    public Requirement update(Long id, UpdateRequirementRequest req) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        if (req.getDescription() != null) r.setDescription(req.getDescription());
        if (req.getNotes() != null) r.setNotes(req.getNotes());
        if (req.getSystem() != null) r.setSystem(req.getSystem());
        if (req.getProjectId() != null) {
            if (projectMapper.selectById(req.getProjectId()) == null)
                throw new BusinessException(400, "所属项目不存在");
            r.setProjectId(req.getProjectId());
        }
        if (req.getPersonId() != null) {
            if (userMapper.selectById(req.getPersonId()) == null)
                throw new BusinessException(400, "业务负责人不存在");
            r.setPersonId(req.getPersonId());
        }
        if (req.getPersonName() != null) r.setPersonName(req.getPersonName());
        if (req.getPriority() != null) r.setPriority(req.getPriority());
        if (req.getTotalAmount() != null) r.setTotalAmount(req.getTotalAmount());
        if (req.getTotalPrice() != null) r.setTotalPrice(req.getTotalPrice());
        if (req.getDevTotal() != null) r.setDevTotal(req.getDevTotal());
        if (req.getDevPrice() != null) r.setDevPrice(req.getDevPrice());
        if (req.getTestTotal() != null) r.setTestTotal(req.getTestTotal());
        if (req.getTestPrice() != null) r.setTestPrice(req.getTestPrice());
        if (req.getIterationId() != null) r.setIterationId(req.getIterationId());
        if (req.getBusinessStatus() != null) r.setBusinessStatus(req.getBusinessStatus());
        if (req.getNumber() != null) r.setNumber(req.getNumber());
        if (req.getProjectType() != null) r.setProjectType(req.getProjectType());
        if (req.getStatus() != null) r.setStatus(req.getStatus());
        if (req.getPlannedCompletionTime() != null)
            r.setPlannedCompletionTime(java.time.OffsetDateTime.parse(req.getPlannedCompletionTime()).toLocalDateTime());
        requirementMapper.updateById(r);
        fillAssociations(r);
        return r;
    }

    public void changeStatus(Long id, String newStatus) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        String oldStatus = r.getStatus();
        r.setStatus(newStatus);
        requirementMapper.updateById(r);

        if ("released".equals(newStatus)) {
            cleanupBugScreenshots(id);
        }

        List<User> notifyTargets = new ArrayList<>();
        if (r.getDevLeadId() != null) {
            User devLead = userMapper.selectById(r.getDevLeadId());
            if (devLead != null) notifyTargets.add(devLead);
        }
        if (!notifyTargets.isEmpty()) {
            String operatorName = currentUser().getName();
            notificationService.emitRequirementEvent(r, oldStatus, newStatus, operatorName, notifyTargets, null);
        }
    }

    /** 需求发布后删除关联Bug的截图文件和数据记录 */
    private void cleanupBugScreenshots(Long requirementId) {
        List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getRequirementId, requirementId));
        if (tasks.isEmpty()) return;
        List<Long> taskIds = tasks.stream().map(Task::getId).toList();
        List<Bug> bugs = bugMapper.selectList(
                new LambdaQueryWrapper<Bug>().in(Bug::getTaskId, taskIds));
        for (Bug bug : bugs) {
            bugService.deleteAllImages(bug.getId());
        }
        log.info("需求 {} 发布，已清理 {} 个Bug的截图", requirementId, bugs.size());
    }

    public void delete(Long id) {
        requirementMapper.deleteById(id);
    }

    /** 纳入发布清单 */
    public void addToRelease(Long id, String iterationId) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        if (iterationId != null && !iterationId.isBlank()) {
            try {
                Iteration it = iterationMapper.selectById(Long.parseLong(iterationId));
                if (it == null) throw new BusinessException(400, "迭代不存在");
                if (!"pending_publish".equals(it.getStatus())) {
                    throw new BusinessException(400, "只能关联待发布状态的迭代");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException(400, "迭代ID格式错误");
            }
        }
        r.setIterationId(iterationId);
        requirementMapper.updateById(r);
    }

    /** 从发布库移除 */
    public void removeFromRelease(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setIterationId(null);
        requirementMapper.updateById(r);
    }

    /** 获取系统板块统计数据（按 system 分组统计非 released、非 closed 需求数量） */
    public List<Map<String, Object>> getSystemStats() {
        List<Requirement> all = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .ne(Requirement::getStatus, "released")
                        .ne(Requirement::getStatus, "closed"));
        Map<String, Long> stats = all.stream()
                .filter(r -> r.getSystem() != null && !r.getSystem().isBlank())
                .collect(Collectors.groupingBy(Requirement::getSystem, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : stats.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("system", entry.getKey());
            m.put("count", entry.getValue());
            result.add(m);
        }
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return result;
    }

    /** 获取需求进度（开发进度 + 测试进度报表） */
    public Map<String, Object> getRequirementProgress(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");

        List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getRequirementId, id));
        for (Task t : tasks) {
            if (t.getAssigneeId() != null) t.setAssignee(userMapper.selectById(t.getAssigneeId()));
        }

        // 开发进度：按人员+终端分组
        List<Map<String, Object>> devProgress = new ArrayList<>();
        Map<String, List<Task>> byPersonTerminal = tasks.stream()
                .filter(t -> t.getAssigneeId() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAssigneeId() + "|" + (t.getTerminal() != null ? t.getTerminal() : "其他")));
        for (var entry : byPersonTerminal.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            Long userId = Long.parseLong(parts[0]);
            String terminal = parts[1];
            User user = userMapper.selectById(userId);
            List<Task> tl = entry.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("person", user != null ? user.getName() : "未知");
            m.put("terminal", terminal);
            m.put("tasks", tl.size());
            int avgProgress = (int) Math.round(tl.stream().mapToInt(t -> t.getProgress() != null ? t.getProgress() : 0).average().orElse(0));
            m.put("progress", avgProgress);
            m.put("done", tl.stream().filter(t -> "closed".equals(t.getStatus())).count());
            devProgress.add(m);
        }

        // 测试进度：累计BUG统计
        List<Bug> bugs = bugMapper.selectList(
                new LambdaQueryWrapper<Bug>().eq(Bug::getRequirementId, id));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("devProgress", devProgress);
        result.put("totalBugs", bugs.size());
        result.put("closedBugs", bugs.stream().filter(b -> "closed".equals(b.getStatus())).count());
        result.put("unfixedBugs", bugs.stream().filter(b -> "unfixed".equals(b.getStatus())).count());
        result.put("pendingVerifyBugs", bugs.stream().filter(b ->
                "fixed".equals(b.getStatus()) || "not_a_bug".equals(b.getStatus())).count());
        return result;
    }

    private void applyProjectScopeFilter(LambdaQueryWrapper<Requirement> q) {
        JwtUserDetails u = currentUser();
        String role = u.getRole();
        if ("pm".equals(role) || "tester".equals(role)) return;
        if ("dev_lead".equals(role)) {
            q.eq(Requirement::getDevLeadId, u.getUserId());
            return;
        }
        List<Long> projectIds = getVisibleProjectIds(u.getUserId());
        if (projectIds.isEmpty()) {
            q.apply("1=0");
        } else {
            q.in(Requirement::getProjectId, projectIds);
            q.or().isNull(Requirement::getProjectId);
        }
    }

    private List<Long> getVisibleProjectIds(Long userId) {
        List<com.management.project.entity.Project> all = projectMapper.selectList(null);
        return all.stream()
                .filter(p -> p.getHrScope() != null && !p.getHrScope().isBlank())
                .filter(p -> Arrays.stream(p.getHrScope().split(","))
                        .map(String::trim).anyMatch(id -> id.equals(String.valueOf(userId))))
                .map(com.management.project.entity.Project::getId)
                .collect(Collectors.toList());
    }

    private void fillProgress(Requirement r) {
        List<Task> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<Task>().eq(Task::getRequirementId, r.getId()));
        if (tasks.isEmpty()) {
            r.setDevProgress(null);
        } else {
            double weightedSum = 0;
            double totalPerformance = 0;
            for (Task t : tasks) {
                double perf = 1;
                if (t.getPerformance() != null) {
                    try { perf = Double.parseDouble(t.getPerformance()); } catch (NumberFormatException e) { perf = 1; }
                }
                double prog = t.getProgress() != null ? t.getProgress() : 0;
                weightedSum += perf * prog;
                totalPerformance += perf;
            }
            r.setDevProgress(totalPerformance > 0 ? (int) Math.round(weightedSum / totalPerformance) : 0);
        }

        long integrationTotal = bugMapper.selectCount(new LambdaQueryWrapper<Bug>()
                .eq(Bug::getRequirementId, r.getId()).eq(Bug::getTestType, "integration"));
        long integrationClosed = bugMapper.selectCount(new LambdaQueryWrapper<Bug>()
                .eq(Bug::getRequirementId, r.getId()).eq(Bug::getTestType, "integration").eq(Bug::getStatus, "closed"));
        r.setIntegrationTestProgress(integrationTotal > 0 ? (int) (integrationClosed * 100 / integrationTotal) : 0);

        long businessTotal = bugMapper.selectCount(new LambdaQueryWrapper<Bug>()
                .eq(Bug::getRequirementId, r.getId()).eq(Bug::getTestType, "business"));
        long businessClosed = bugMapper.selectCount(new LambdaQueryWrapper<Bug>()
                .eq(Bug::getRequirementId, r.getId()).eq(Bug::getTestType, "business").eq(Bug::getStatus, "closed"));
        r.setBusinessTestProgress(businessTotal > 0 ? (int) (businessClosed * 100 / businessTotal) : 0);

        long itTotal = bugMapper.selectCount(new LambdaQueryWrapper<Bug>()
                .eq(Bug::getRequirementId, r.getId()).eq(Bug::getTestType, "it_test"));
        long itClosed = bugMapper.selectCount(new LambdaQueryWrapper<Bug>()
                .eq(Bug::getRequirementId, r.getId()).eq(Bug::getTestType, "it_test").eq(Bug::getStatus, "closed"));
        r.setItTestProgress(itTotal > 0 ? (int) (itClosed * 100 / itTotal) : 0);

        long totalBugs = integrationTotal + businessTotal + itTotal;
        long totalClosed = integrationClosed + businessClosed + itClosed;
        r.setTestProgress(totalBugs > 0 ? (int) (totalClosed * 100 / totalBugs) : null);
    }

    private RequirementDetailDTO toDetailDTO(Requirement r) {
        RequirementDetailDTO dto = new RequirementDetailDTO();
        dto.setId(r.getId());
        dto.setRequirementId(r.getRequirementId());
        dto.setNumber(r.getNumber());
        dto.setDescription(r.getDescription());
        dto.setNotes(r.getNotes());
        dto.setStatus(r.getStatus());
        dto.setPriority(r.getPriority());
        dto.setSystem(r.getSystem());
        dto.setProjectId(r.getProjectId());
        dto.setProject(r.getProject());
        dto.setProjectType(r.getProjectType());
        dto.setPersonId(r.getPersonId());
        dto.setPerson(r.getPerson());
        dto.setPersonName(r.getPersonName());
        dto.setTotalAmount(r.getTotalAmount());
        dto.setTotalPrice(r.getTotalPrice());
        dto.setDevTotal(r.getDevTotal());
        dto.setDevPrice(r.getDevPrice());
        dto.setTestTotal(r.getTestTotal());
        dto.setTestPrice(r.getTestPrice());
        dto.setReleaseTime(r.getReleaseTime() != null ? r.getReleaseTime().toString() : null);
        dto.setPlannedCompletionTime(r.getPlannedCompletionTime() != null ? r.getPlannedCompletionTime().toString() : null);
        dto.setIterationId(r.getIterationId());
        dto.setDevLeadId(r.getDevLeadId());
        dto.setDevLead(r.getDevLead());
        dto.setIterationName(r.getIterationName());
        dto.setDocumentPath(r.getDocumentPath());
        dto.setDocumentName(r.getDocumentName());
        dto.setDocumentSize(r.getDocumentSize());
        dto.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        dto.setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null);
        return dto;
    }

    public void assignDevLead(Long id, Long devLeadId) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setDevLeadId(devLeadId);
        if ("planned".equals(r.getStatus())) {
            r.setStatus("assigned");
        }
        requirementMapper.updateById(r);

        User devLead = userMapper.selectById(devLeadId);
        if (devLead != null) {
            String operatorName = currentUser().getName();
            String msg = "需求【" + r.getDescription() + "】您被指派为开发组长，操作人：" + operatorName;
            notificationService.emitGenericEvent(msg, operatorName, List.of(devLead));
        }
    }

    private void fillAssociations(Requirement r) {
        if (r.getProjectId() != null) {
            var p = projectMapper.selectById(r.getProjectId());
            if (p != null) { p.setPm(userMapper.selectById(p.getPmId())); r.setProject(p); }
        }
        if (r.getPersonId() != null) r.setPerson(userMapper.selectById(r.getPersonId()));
        if (r.getDevLeadId() != null) r.setDevLead(userMapper.selectById(r.getDevLeadId()));
        if (r.getIterationId() != null) {
            try {
                var it = iterationMapper.selectById(Long.parseLong(r.getIterationId()));
                if (it != null) r.setIterationName(it.getName());
            } catch (NumberFormatException e) {
                // iterationId may not be numeric
            }
        }
    }

    @Transactional
    public void uploadDocument(Long id, MultipartFile file) throws IOException {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        if (file.isEmpty()) throw new BusinessException(400, "文件不能为空");

        Path dir = Paths.get(uploadDir, "requirements", String.valueOf(id));
        Files.createDirectories(dir);

        // Delete old document if exists
        if (r.getDocumentPath() != null) {
            Path oldPath = Paths.get(r.getDocumentPath());
            try { Files.deleteIfExists(oldPath); } catch (IOException ignored) {}
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.lastIndexOf('.') > 0) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String fileName = UUID.randomUUID() + ext;
        Path target = dir.resolve(fileName);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }

        r.setDocumentPath(target.toString());
        r.setDocumentName(originalName);
        r.setDocumentSize(file.getSize());
        requirementMapper.updateById(r);
    }

    public void downloadDocument(Long id, HttpServletResponse response) throws IOException {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        if (r.getDocumentPath() == null) throw new BusinessException(404, "文档不存在");

        Path path = Paths.get(r.getDocumentPath());
        if (!Files.exists(path)) throw new BusinessException(404, "文档文件不存在");

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + r.getDocumentName() + "\"");
        response.setContentLengthLong(r.getDocumentSize() != null ? r.getDocumentSize() : Files.size(path));
        Files.copy(path, response.getOutputStream());
        response.getOutputStream().flush();
    }

    @Transactional
    public void deleteDocument(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        if (r.getDocumentPath() != null) {
            java.nio.file.Path path = Paths.get(r.getDocumentPath());
            try { Files.deleteIfExists(path); } catch (IOException ignored) {}
            // 删除空目录
            java.io.File dir = path.toFile().getParentFile();
            if (dir != null && dir.isDirectory() && dir.list().length == 0) dir.delete();
        }
        r.setDocumentPath(null);
        r.setDocumentName(null);
        r.setDocumentSize(null);
        requirementMapper.updateById(r);
    }
}
