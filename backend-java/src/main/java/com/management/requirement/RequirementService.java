package com.management.requirement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.iteration.mapper.IterationMapper;
import com.management.project.mapper.ProjectMapper;
import com.management.requirement.dto.CreateRequirementRequest;
import com.management.requirement.dto.UpdateRequirementRequest;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementService {
    private final RequirementMapper requirementMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final IterationMapper iterationMapper;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 创建需求，自动生成编号 */
    @Transactional
    public Requirement create(CreateRequirementRequest req) {
        Requirement r = new Requirement();
        r.setTitle(req.getTitle());
        r.setDescription(req.getDescription());
        r.setNotes(req.getNotes());
        r.setSource(req.getSource());
        r.setSystem(req.getSystem());
        r.setProjectId(req.getProjectId());
        r.setProjectType(req.getProjectType());
        r.setPersonId(req.getPersonId());
        r.setRelevant(req.getRelevant());
        r.setPriority(req.getPriority() != null ? req.getPriority() : "medium");
        r.setStatus("planned");
        r.setTotalAmount(req.getTotalAmount());
        r.setDevTotal(req.getDevTotal());
        r.setDevPrice(req.getDevPrice());
        r.setTestTotal(req.getTestTotal());
        r.setTestPrice(req.getTestPrice());
        r.setBizTestTotal(req.getBizTestTotal());
        r.setBizTestPrice(req.getBizTestPrice());
        r.setIterationId(req.getIterationId());
        r.setRequirementId("TEMP");
        r.setNumber("TEMP");
        requirementMapper.insert(r);
        r.setRequirementId("REQ-" + java.time.Year.now().getValue() + "-" + String.format("%03d", r.getId()));
        r.setNumber(r.getRequirementId());
        requirementMapper.updateById(r);
        fillAssociations(r);
        log.info("Requirement created: id={}, title={}", r.getRequirementId(), r.getTitle());
        return r;
    }

    public List<Requirement> list(String status, String system, String projectType, String source) {
        LambdaQueryWrapper<Requirement> q = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) q.eq(Requirement::getStatus, status);
        if (system != null && !system.isBlank()) q.eq(Requirement::getSystem, system);
        if (projectType != null && !projectType.isBlank()) q.eq(Requirement::getProjectType, projectType);
        if (source != null && !source.isBlank()) q.eq(Requirement::getSource, source);
        q.orderByDesc(Requirement::getUpdatedAt);
        List<Requirement> list = requirementMapper.selectList(q);
        for (Requirement r : list) fillAssociations(r);
        return list;
    }

    public List<Requirement> opsList() {
        List<Requirement> list = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .eq(Requirement::getProjectType, "ops")
                        .ne(Requirement::getStatus, "released")
                        .orderByDesc(Requirement::getUpdatedAt));
        for (Requirement r : list) fillAssociations(r);
        return list;
    }

    public List<Requirement> projectList() {
        List<Requirement> list = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .eq(Requirement::getProjectType, "project")
                        .ne(Requirement::getStatus, "released")
                        .orderByDesc(Requirement::getUpdatedAt));
        for (Requirement r : list) fillAssociations(r);
        return list;
    }

    public Requirement getById(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        fillAssociations(r);
        return r;
    }

    public Requirement update(Long id, UpdateRequirementRequest req) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        if (req.getTitle() != null) r.setTitle(req.getTitle());
        if (req.getDescription() != null) r.setDescription(req.getDescription());
        if (req.getNotes() != null) r.setNotes(req.getNotes());
        if (req.getSource() != null) r.setSource(req.getSource());
        if (req.getSystem() != null) r.setSystem(req.getSystem());
        if (req.getProjectId() != null) r.setProjectId(req.getProjectId());
        if (req.getPersonId() != null) r.setPersonId(req.getPersonId());
        if (req.getRelevant() != null) r.setRelevant(req.getRelevant());
        if (req.getPriority() != null) r.setPriority(req.getPriority());
        if (req.getTotalAmount() != null) r.setTotalAmount(req.getTotalAmount());
        if (req.getDevTotal() != null) r.setDevTotal(req.getDevTotal());
        if (req.getDevPrice() != null) r.setDevPrice(req.getDevPrice());
        if (req.getTestTotal() != null) r.setTestTotal(req.getTestTotal());
        if (req.getTestPrice() != null) r.setTestPrice(req.getTestPrice());
        if (req.getBizTestTotal() != null) r.setBizTestTotal(req.getBizTestTotal());
        if (req.getBizTestPrice() != null) r.setBizTestPrice(req.getBizTestPrice());
        if (req.getIterationId() != null) r.setIterationId(req.getIterationId());
        requirementMapper.updateById(r);
        fillAssociations(r);
        return r;
    }

    public void changeStatus(Long id, String newStatus) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setStatus(newStatus);
        requirementMapper.updateById(r);
    }

    public void delete(Long id) {
        requirementMapper.deleteById(id);
    }

    /** 纳入发布清单 */
    public void addToRelease(Long id, String iterationId) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setIterationId(iterationId);
        r.setStatus("pending_release");
        requirementMapper.updateById(r);
    }

    /** 从发布库移除 */
    public void removeFromRelease(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setIterationId(null);
        if ("pending_release".equals(r.getStatus()) || "released".equals(r.getStatus())) {
            r.setStatus("business_test");
        }
        requirementMapper.updateById(r);
    }

    private void fillAssociations(Requirement r) {
        if (r.getProjectId() != null) {
            var p = projectMapper.selectById(r.getProjectId());
            if (p != null) { p.setPm(userMapper.selectById(p.getPmId())); r.setProject(p); }
        }
        if (r.getPersonId() != null) r.setPerson(userMapper.selectById(r.getPersonId()));
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
            try { Files.deleteIfExists(Paths.get(r.getDocumentPath())); } catch (IOException ignored) {}
        }
        r.setDocumentPath(null);
        r.setDocumentName(null);
        r.setDocumentSize(null);
        requirementMapper.updateById(r);
    }
}
