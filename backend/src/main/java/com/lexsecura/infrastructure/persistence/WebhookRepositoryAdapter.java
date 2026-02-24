package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Webhook;
import com.lexsecura.domain.repository.WebhookRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaWebhookRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WebhookRepositoryAdapter implements WebhookRepository {

    private final JpaWebhookRepository jpa;
    private final PersistenceMapper mapper;

    public WebhookRepositoryAdapter(JpaWebhookRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Webhook save(Webhook w) {
        return mapper.toDomain(jpa.save(mapper.toEntity(w)));
    }

    @Override
    public Optional<Webhook> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<Webhook> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Webhook> findAllEnabledByOrgId(UUID orgId) {
        return jpa.findAllByOrgIdAndEnabledTrue(orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
