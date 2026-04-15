package tech.exitzero.blog.backend.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.exitzero.blog.backend.domain.ArticleStatus;
import tech.exitzero.blog.backend.domain.ArticleTimelineEventType;
import tech.exitzero.blog.backend.domain.BlogArticle;
import tech.exitzero.blog.backend.domain.BlogCategory;
import tech.exitzero.blog.backend.domain.BlogSiteVisualConfig;
import tech.exitzero.blog.backend.domain.BlogTag;
import tech.exitzero.blog.backend.domain.SiteEventType;
import tech.exitzero.blog.backend.repository.BlogArticleRepository;
import tech.exitzero.blog.backend.repository.BlogCategoryRepository;
import tech.exitzero.blog.backend.repository.BlogSiteVisualConfigRepository;
import tech.exitzero.blog.backend.repository.BlogTagRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;

@Component
public class BootstrapDataInitializer implements ApplicationRunner {

    private final BlogArticleRepository articleRepository;
    private final BlogCategoryRepository categoryRepository;
    private final BlogTagRepository tagRepository;
    private final BlogSiteVisualConfigRepository visualConfigRepository;
    private final MarkdownRenderService markdownRenderService;
    private final TimelineService timelineService;

    public BootstrapDataInitializer(
        BlogArticleRepository articleRepository,
        BlogCategoryRepository categoryRepository,
        BlogTagRepository tagRepository,
        BlogSiteVisualConfigRepository visualConfigRepository,
        MarkdownRenderService markdownRenderService,
        TimelineService timelineService
    ) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.visualConfigRepository = visualConfigRepository;
        this.markdownRenderService = markdownRenderService;
        this.timelineService = timelineService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (articleRepository.count() > 0 || categoryRepository.count() > 0) {
            return;
        }

        BlogCategory engineering = category("工程实践", "engineering", null, "系统设计、部署与架构演进的主分类。", 1);
        BlogCategory blogBuilding = category("博客搭建", "blog-building", engineering, "记录博客从静态站升级为内容工作台。", 2);
        BlogCategory aiPractice = category("AI 实战", "ai-practice", engineering, "Agent、RAG 与自动化工作流实践。", 3);

        BlogTag vitepress = tag("VitePress", "vitepress", "#3a7cff");
        BlogTag vue = tag("Vue 3", "vue-3", "#42b883");
        BlogTag ai = tag("AI", "ai", "#ff7a59");
        BlogTag workflow = tag("Workflow", "workflow", "#8f5cff");

        BlogArticle publishedArticle = new BlogArticle();
        publishedArticle.setTitle("从静态站到博客工作台");
        publishedArticle.setSlug("blog-studio-kickoff");
        publishedArticle.setSummary("把 VitePress 站点升级成带发布流、时间线和视觉配置的博客平台。");
        publishedArticle.setContentMarkdown("""
            # 从静态站到博客工作台
            
            这次升级让博客站具备了真正的内容管理能力。
            
            ## 已具备能力
            - Markdown 编辑与预览
            - 自动保存草稿
            - 封面图 / Banner / 背景图
            - 草稿 -> 待发布 -> 已发布 -> 隐藏 / 下架
            """);
        publishedArticle.setContentHtml(markdownRenderService.render(publishedArticle.getContentMarkdown()));
        publishedArticle.setStatus(ArticleStatus.PUBLISHED);
        publishedArticle.setCategory(blogBuilding);
        publishedArticle.setTags(new LinkedHashSet<>(java.util.List.of(vitepress, vue)));
        publishedArticle.setCoverImage("/images/fig1.png");
        publishedArticle.setCreatedAt(LocalDateTime.of(2026, 4, 2, 16, 30));
        publishedArticle.setUpdatedAt(LocalDateTime.of(2026, 4, 10, 23, 20));
        publishedArticle.setDraftSavedAt(LocalDateTime.of(2026, 4, 9, 21, 10));
        publishedArticle.setPublishedAt(LocalDateTime.of(2026, 4, 10, 23, 20));
        articleRepository.save(publishedArticle);

        timelineService.recordArticleEvent(publishedArticle, ArticleTimelineEventType.CREATED, "创建文章草稿", "启动站点升级计划。", 1L);
        timelineService.recordArticleEvent(publishedArticle, ArticleTimelineEventType.SUBMITTED, "提交待发布", "提交到待发布队列。", 1L);
        timelineService.recordArticleEvent(publishedArticle, ArticleTimelineEventType.PUBLISHED, "正式发布", "正式发布到博客首页。", 1L);

        BlogArticle pendingArticle = new BlogArticle();
        pendingArticle.setTitle("AI 自动化内容流的第一篇实验");
        pendingArticle.setSlug("ai-content-automation-log");
        pendingArticle.setSummary("记录如何把标签体系、时间线和内容运营串起来，让博客更像一个产品。");
        pendingArticle.setContentMarkdown("""
            # AI 自动化内容流的第一篇实验
            
            > 文章不仅是内容，更是一条时间轴记录。
            
            1. 完成分类结构
            2. 建立待发布状态
            3. 串联时间线与首页
            """);
        pendingArticle.setContentHtml(markdownRenderService.render(pendingArticle.getContentMarkdown()));
        pendingArticle.setStatus(ArticleStatus.PENDING);
        pendingArticle.setCategory(aiPractice);
        pendingArticle.setTags(new LinkedHashSet<>(java.util.List.of(ai, workflow)));
        pendingArticle.setCreatedAt(LocalDateTime.of(2026, 4, 11, 11, 10));
        pendingArticle.setUpdatedAt(LocalDateTime.of(2026, 4, 12, 20, 45));
        pendingArticle.setDraftSavedAt(LocalDateTime.of(2026, 4, 12, 20, 45));
        articleRepository.save(pendingArticle);

        timelineService.recordArticleEvent(pendingArticle, ArticleTimelineEventType.CREATED, "创建文章草稿", "新建 AI 自动化专题文章。", 1L);
        timelineService.recordArticleEvent(pendingArticle, ArticleTimelineEventType.SUBMITTED, "提交待发布", "进入待发布流程，等待最终检查。", 1L);

        BlogSiteVisualConfig aurora = new BlogSiteVisualConfig();
        aurora.setConfigKey("aurora-flow");
        aurora.setConfigName("Aurora Flow");
        aurora.setOverlayCss("radial-gradient(circle at 20% 20%, rgba(58,124,255,0.45), transparent 42%), linear-gradient(135deg, rgba(8,18,34,0.92), rgba(18,31,53,0.8))");
        aurora.setAccentColor("#3a7cff");
        aurora.setMotionClass("aurora");
        aurora.setEnabled(true);
        visualConfigRepository.save(aurora);

        BlogSiteVisualConfig ember = new BlogSiteVisualConfig();
        ember.setConfigKey("ember-notes");
        ember.setConfigName("Ember Notes");
        ember.setOverlayCss("radial-gradient(circle at 25% 15%, rgba(255,181,71,0.25), transparent 30%), linear-gradient(135deg, rgba(31,20,15,0.92), rgba(63,31,22,0.78))");
        ember.setAccentColor("#ff7a59");
        ember.setMotionClass("ember");
        ember.setEnabled(false);
        visualConfigRepository.save(ember);

        timelineService.recordSiteEvent(SiteEventType.SYSTEM, "站点启动重构", "从纯文档博客转向内容工作台，开始补齐发布流与时间轴能力。");
        timelineService.recordSiteEvent(SiteEventType.CONTENT, "分类与标签层完成", "支持多级分类与多对多标签映射，为后续聚合与检索打基础。");
        timelineService.recordSiteEvent(SiteEventType.RELEASE, "发布工作台上线", "编辑器、自动保存、封面上传和状态机在同一页联动。");
    }

    private BlogCategory category(String name, String slug, BlogCategory parent, String description, int sortNum) {
        BlogCategory category = new BlogCategory();
        category.setName(name);
        category.setSlug(slug);
        category.setParent(parent);
        category.setDescription(description);
        category.setSortNum(sortNum);
        return categoryRepository.save(category);
    }

    private BlogTag tag(String name, String slug, String color) {
        BlogTag tag = new BlogTag();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setColor(color);
        return tagRepository.save(tag);
    }
}
