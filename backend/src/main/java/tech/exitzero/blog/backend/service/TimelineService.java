package tech.exitzero.blog.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.exitzero.blog.backend.api.TimelineApi;
import tech.exitzero.blog.backend.domain.ArticleTimelineEventType;
import tech.exitzero.blog.backend.domain.BlogArticle;
import tech.exitzero.blog.backend.domain.BlogArticleTimeline;
import tech.exitzero.blog.backend.domain.BlogSiteEvent;
import tech.exitzero.blog.backend.domain.SiteEventType;
import tech.exitzero.blog.backend.repository.BlogArticleTimelineRepository;
import tech.exitzero.blog.backend.repository.BlogSiteEventRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class TimelineService {

    private final BlogArticleTimelineRepository articleTimelineRepository;
    private final BlogSiteEventRepository siteEventRepository;
    private final BlogMapper blogMapper;

    public TimelineService(
        BlogArticleTimelineRepository articleTimelineRepository,
        BlogSiteEventRepository siteEventRepository,
        BlogMapper blogMapper
    ) {
        this.articleTimelineRepository = articleTimelineRepository;
        this.siteEventRepository = siteEventRepository;
        this.blogMapper = blogMapper;
    }

    @Transactional
    public void recordArticleEvent(
        BlogArticle article,
        ArticleTimelineEventType eventType,
        String eventTitle,
        String eventNote,
        Long operatorId
    ) {
        BlogArticleTimeline timeline = new BlogArticleTimeline();
        timeline.setArticle(article);
        timeline.setEventType(eventType);
        timeline.setEventTitle(eventTitle);
        timeline.setEventNote(eventNote);
        timeline.setOperatorId(operatorId);
        articleTimelineRepository.save(timeline);
    }

    @Transactional
    public void recordSiteEvent(SiteEventType eventType, String title, String description) {
        BlogSiteEvent siteEvent = new BlogSiteEvent();
        siteEvent.setEventType(eventType);
        siteEvent.setTitle(title);
        siteEvent.setDescription(description);
        siteEventRepository.save(siteEvent);
    }

    @Transactional(readOnly = true)
    public List<TimelineApi.TimelineEntryResponse> getMergedTimeline(Integer limit) {
        int safeLimit = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        PageRequest pageable = PageRequest.of(0, safeLimit);

        List<TimelineApi.TimelineEntryResponse> articleEntries = articleTimelineRepository
            .findAllByOrderByCreatedAtDesc(pageable)
            .stream()
            .map(blogMapper::toTimelineEntry)
            .toList();

        List<TimelineApi.TimelineEntryResponse> siteEntries = siteEventRepository
            .findAllByOrderByCreatedAtDesc(pageable)
            .stream()
            .map(blogMapper::toTimelineEntry)
            .toList();

        return List.copyOf(
            java.util.stream.Stream.concat(articleEntries.stream(), siteEntries.stream())
                .sorted(Comparator.comparing(TimelineApi.TimelineEntryResponse::createdAt).reversed())
                .limit(safeLimit)
                .toList()
        );
    }
}
