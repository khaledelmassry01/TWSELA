package com.twsela.repository;

import com.twsela.domain.ThirdPartyPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThirdPartyPartnerRepository extends JpaRepository<ThirdPartyPartner, Long> {
    List<ThirdPartyPartner> findByStatusOrderByNameAsc(String status);
    List<ThirdPartyPartner> findByTenantIdOrderByNameAsc(Long tenantId);
    List<ThirdPartyPartner> findByTenantIdAndStatusOrderByNameAsc(Long tenantId, String status);
}
