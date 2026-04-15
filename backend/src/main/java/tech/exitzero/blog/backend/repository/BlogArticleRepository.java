package tech.exitzero.blog.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.exitzero.blog.backend.domain.ArticleStatus;
import tech.exitzero.blog.backend.domain.BlogArticle;

import java.util.List;

public interface BlogArticleRepository extends JpaRepository<BlogArticle, Long> {

    List<BlogArticle> findAllByOrderByUpdatedAtDesc();

    List<BlogArticle> findAllByStatusOrderByUpdatedAtDesc(ArticleStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
