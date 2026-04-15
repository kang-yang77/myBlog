package tech.exitzero.blog.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.exitzero.blog.backend.domain.BlogTag;

import java.util.List;

public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {

    List<BlogTag> findAllByOrderByNameAsc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
