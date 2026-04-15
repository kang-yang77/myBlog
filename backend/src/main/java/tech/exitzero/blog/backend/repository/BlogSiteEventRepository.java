package tech.exitzero.blog.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tech.exitzero.blog.backend.domain.BlogSiteEvent;

public interface BlogSiteEventRepository extends JpaRepository<BlogSiteEvent, Long> {

    Page<BlogSiteEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
