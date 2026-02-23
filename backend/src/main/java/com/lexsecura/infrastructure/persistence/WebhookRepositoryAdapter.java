package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Webhook;
import com.lexsecura.domain.repository.WebhookRepository;
import com.lexsecura.infrastructure.persistence.entity.WebhookEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaWebhookRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class WebhookRepositoryAdapter implements WebhookRepository {

    private final JpaWebhookRepository jpa;

    public WebhookRepositoryAdapter(JpaWebhookRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Webhook save(Webhook w) { return toDomain(jpa.save(toEntity(w))); }

    @Override
    public Optional<Webhook> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<Webhook> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Webhook> findAllEnabledByOrgId(UUID orgId) {
        return jpa.findAllByOrgIdAndEnabledTrue(orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) { jpa.deleteById(id); }

    private Webhook toDomain(WebhookEntity e) {
        Webhook w = new Webhook();
        w.setId(e.getId()); w.setOrgId(e.getOrgId()); w.setName(e.getName());
        w.setUrl(e.getUrl()); w.setSecret(e.getSecret()); w.setEventTypes(e.getEventTypes());
        w.setChannelType(e.getChannelType()); w.setEnabled(e.isEnabled());
        w.setCreatedBy(e.getCreatedBy()); w.setCreatedAt(e.getCreatedAt()); w.setUpdatedAt(e.getUpdatedAt());
        return w;
    }

    private WebhookEntity toEntity(Webhook w) {
        WebhookEntity e = new WebhookEntity();
        e.setId(w.getId()); e.setOrgId(w.getOrgId()); e.setName(w.getName());
        e.setUrl(w.getUrl()); e.setSecret(w.getSecret()); e.setEventTypes(w.getEventTypes());
        e.setChannelType(w.getChannelType()); e.setEnabled(w.isEnabled());
        e.setCreatedBy(w.getCreatedBy()); e.setCreatedAt(w.getCreatedAt()); e.setUpdatedAt(w.getUpdatedAt());
        return e;
    }
}
