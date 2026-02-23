package com.lexsecura.application.service;

import com.lexsecura.application.dto.ProductCreateRequest;
import com.lexsecura.application.dto.ProductResponse;
import com.lexsecura.application.dto.ProductUpdateRequest;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository, AuditService auditService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        UUID orgId = TenantContext.getOrgId();
        return productRepository.findAllByOrgId(orgId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        Product product = productRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        return toResponse(product);
    }

    public ProductResponse create(ProductCreateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        Product product = new Product(orgId, request.name(), request.type(),
                request.criticality(), request.contacts());
        product.setConformityPath(Product.computeConformityPath(product.getType()));
        product = productRepository.save(product);

        auditService.record(orgId, "PRODUCT", product.getId(), "CREATE", userId,
                Map.of("name", product.getName(), "type", product.getType(),
                        "criticality", product.getCriticality()));

        return toResponse(product);
    }

    public ProductResponse update(UUID id, ProductUpdateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        Product product = productRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        product.setName(request.name());
        if (request.type() != null) {
            product.setType(request.type());
        }
        if (request.criticality() != null) {
            product.setCriticality(request.criticality());
        }
        if (request.contacts() != null) {
            product.setContacts(request.contacts());
        }
        product.setConformityPath(Product.computeConformityPath(product.getType()));
        product.setUpdatedAt(Instant.now());

        product = productRepository.save(product);

        auditService.record(orgId, "PRODUCT", product.getId(), "UPDATE", userId,
                Map.of("name", product.getName(), "type", product.getType(),
                        "criticality", product.getCriticality()));

        return toResponse(product);
    }

    public void delete(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        Product product = productRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        productRepository.deleteByIdAndOrgId(id, orgId);

        auditService.record(orgId, "PRODUCT", id, "DELETE", userId,
                Map.of("name", product.getName()));
    }

    public Product getProduct(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        return productRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getOrgId(), p.getName(), p.getType(),
                p.getCriticality(), p.getConformityPath(), p.getContacts(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
