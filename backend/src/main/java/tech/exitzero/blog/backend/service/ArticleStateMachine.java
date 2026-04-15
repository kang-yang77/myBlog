package tech.exitzero.blog.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tech.exitzero.blog.backend.domain.ArticleStatus;
import tech.exitzero.blog.backend.support.ApiException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ArticleStateMachine {

    private final Map<ArticleStatus, List<ArticleStatus>> transitions = new EnumMap<>(ArticleStatus.class);

    public ArticleStateMachine() {
        transitions.put(ArticleStatus.DRAFT, List.of(ArticleStatus.PENDING));
        transitions.put(ArticleStatus.PENDING, List.of(ArticleStatus.DRAFT, ArticleStatus.PUBLISHED));
        transitions.put(ArticleStatus.PUBLISHED, List.of(ArticleStatus.HIDDEN, ArticleStatus.OFFLINE));
        transitions.put(ArticleStatus.HIDDEN, List.of(ArticleStatus.PUBLISHED, ArticleStatus.OFFLINE));
        transitions.put(ArticleStatus.OFFLINE, List.of(ArticleStatus.DRAFT, ArticleStatus.PUBLISHED));
    }

    public void assertAllowed(ArticleStatus currentStatus, ArticleStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }

        List<ArticleStatus> allowed = nextStatuses(currentStatus);
        if (!allowed.contains(nextStatus)) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "非法状态流转: " + currentStatus + " -> " + nextStatus
            );
        }
    }

    public List<ArticleStatus> nextStatuses(ArticleStatus currentStatus) {
        return transitions.getOrDefault(currentStatus, List.of());
    }
}
