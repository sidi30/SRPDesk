package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    Optional<OrganizationEntity> findBySlug(String slug);

    @Query("SELECT o FROM OrganizationEntity o WHERE o.id IN " +
           "(SELECT m.orgId FROM OrgMemberEntity m WHERE m.userId = :userId)")
    List<OrganizationEntity> findAllByUserId(UUID userId);
}
