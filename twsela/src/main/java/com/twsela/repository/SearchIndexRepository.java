package com.twsela.repository;

import com.twsela.domain.SearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {
    List<SearchIndex> findByIsActiveTrue();
    List<SearchIndex> findByEntityType(String entityType);
}
