package tech.exitzero.blog.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.exitzero.blog.backend.domain.BlogCategory;

import java.util.List;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {

    List<BlogCategory> findAllByOrderBySortNumAscNameAsc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
