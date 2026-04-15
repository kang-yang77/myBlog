package tech.exitzero.blog.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tech.exitzero.blog.backend.api.VisualConfigApi;
import tech.exitzero.blog.backend.domain.BlogSiteVisualConfig;
import tech.exitzero.blog.backend.domain.SiteEventType;
import tech.exitzero.blog.backend.repository.BlogSiteVisualConfigRepository;
import tech.exitzero.blog.backend.support.ApiException;
import tech.exitzero.blog.backend.support.SlugUtils;

import java.util.List;

@Service
public class VisualConfigService {

    private final BlogSiteVisualConfigRepository visualConfigRepository;
    private final BlogMapper blogMapper;
    private final TimelineService timelineService;

    public VisualConfigService(
        BlogSiteVisualConfigRepository visualConfigRepository,
        BlogMapper blogMapper,
        TimelineService timelineService
    ) {
        this.visualConfigRepository = visualConfigRepository;
        this.blogMapper = blogMapper;
        this.timelineService = timelineService;
    }

    @Transactional(readOnly = true)
    public List<VisualConfigApi.VisualConfigResponse> list() {
        return visualConfigRepository.findAllByOrderByEnabledDescUpdatedAtDesc().stream()
            .map(blogMapper::toVisualConfigResponse)
            .toList();
    }

    @Transactional
    public VisualConfigApi.VisualConfigResponse create(VisualConfigApi.VisualConfigUpsertRequest request) {
        BlogSiteVisualConfig config = new BlogSiteVisualConfig();
        applyRequest(config, request, null);
        if (Boolean.TRUE.equals(request.enabled())) {
            deactivateAll();
            config.setEnabled(true);
        }
        BlogSiteVisualConfig saved = visualConfigRepository.save(config);
        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "新增全局背景", "背景「" + saved.getConfigName() + "」已加入站点视觉配置。");
        return blogMapper.toVisualConfigResponse(saved);
    }

    @Transactional
    public VisualConfigApi.VisualConfigResponse update(Long id, VisualConfigApi.VisualConfigUpsertRequest request) {
        BlogSiteVisualConfig config = getRequiredConfig(id);
        applyRequest(config, request, id);
        if (Boolean.TRUE.equals(request.enabled())) {
            deactivateAll();
            config.setEnabled(true);
        }
        BlogSiteVisualConfig saved = visualConfigRepository.save(config);
        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "更新全局背景", "背景「" + saved.getConfigName() + "」已更新。");
        return blogMapper.toVisualConfigResponse(saved);
    }

    @Transactional
    public VisualConfigApi.VisualConfigResponse activate(Long id) {
        BlogSiteVisualConfig config = getRequiredConfig(id);
        deactivateAll();
        config.setEnabled(true);
        BlogSiteVisualConfig saved = visualConfigRepository.save(config);
        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "切换站点背景", "背景「" + saved.getConfigName() + "」已设为当前站点视觉。");
        return blogMapper.toVisualConfigResponse(saved);
    }

    public BlogSiteVisualConfig getRequiredConfig(Long id) {
        return visualConfigRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "视觉配置不存在: " + id));
    }

    private void applyRequest(BlogSiteVisualConfig config, VisualConfigApi.VisualConfigUpsertRequest request, Long selfId) {
        config.setConfigKey(generateUniqueKey(request.configKey(), selfId));
        config.setConfigName(request.configName().trim());
        config.setImageUrl(defaultString(request.imageUrl()));
        config.setOverlayCss(defaultString(request.overlayCss()));
        config.setAccentColor(StringUtils.hasText(request.accentColor()) ? request.accentColor().trim() : "#3a7cff");
        config.setMotionClass(StringUtils.hasText(request.motionClass()) ? request.motionClass().trim() : "aurora");
        if (request.enabled() != null) {
            config.setEnabled(request.enabled());
        }
    }

    private void deactivateAll() {
        visualConfigRepository.findAll().forEach(item -> item.setEnabled(false));
    }

    private String generateUniqueKey(String requestKey, Long selfId) {
        String baseKey = SlugUtils.toSlug(requestKey);
        String key = baseKey;
        int suffix = 2;
        while (keyExists(key, selfId)) {
            key = baseKey + "-" + suffix++;
        }
        return key;
    }

    private boolean keyExists(String key, Long selfId) {
        return selfId == null
            ? visualConfigRepository.existsByConfigKey(key)
            : visualConfigRepository.existsByConfigKeyAndIdNot(key, selfId);
    }

    private String defaultString(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
