package tech.exitzero.blog.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.exitzero.blog.backend.api.TimelineApi;
import tech.exitzero.blog.backend.service.TimelineService;

import java.util.List;

@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping
    public List<TimelineApi.TimelineEntryResponse> timeline(
        @RequestParam(required = false) Integer limit
    ) {
        return timelineService.getMergedTimeline(limit);
    }
}
