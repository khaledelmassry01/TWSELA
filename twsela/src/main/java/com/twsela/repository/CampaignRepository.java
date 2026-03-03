package com.twsela.repository;

import com.twsela.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByStatusOrderByCreatedAtDesc(String status);

    List<Campaign> findByCampaignTypeOrderByCreatedAtDesc(String campaignType);

    List<Campaign> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<Campaign> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status);
}
