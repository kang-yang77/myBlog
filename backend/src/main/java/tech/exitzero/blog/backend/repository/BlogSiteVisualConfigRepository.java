package tech.exitzero.blog.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.exitzero.blog.backend.domain.BlogSiteVisualConfig;

import java.util.List;
import java.util.Optional;

public interface BlogSiteVisualConfigRepository extends JpaRepository<BlogSiteVisualConfig, Long> {

    List<BlogSiteVisualConfig> findAllByOrderByEnabledDescUpdatedAtDesc();

    Optional<BlogSiteVisualConfig> findByEnabledTrue();

    boolean existsByConfigKey(String configKey);

    boolean existsByConfigKeyAndIdNot(String configKey, Long id);
}
