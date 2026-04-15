package tech.exitzero.blog.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tech.exitzero.blog.backend.domain.BlogArticleTimeline;

import java.util.List;

public interface BlogArticleTimelineRepository extends JpaRepository<BlogArticleTimeline, Long> {

    List<BlogArticleTimeline> findAllByArticleIdOrderByCreatedAtDesc(Long articleId);

    Page<BlogArticleTimeline> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
