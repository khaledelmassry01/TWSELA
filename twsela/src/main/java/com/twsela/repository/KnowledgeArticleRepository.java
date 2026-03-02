package com.twsela.repository;

import com.twsela.domain.KnowledgeArticle;
import com.twsela.domain.SupportTicket.TicketCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Page<KnowledgeArticle> findByPublishedTrueOrderByViewCountDesc(Pageable pageable);

    List<KnowledgeArticle> findByCategoryAndPublishedTrue(TicketCategory category);

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.published = true AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<KnowledgeArticle> searchPublished(@Param("query") String query);
}
