package com.lexsecura.application.service;

import com.lexsecura.application.dto.CiSbomUploadResponse;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Transactional
public class CiSbomService {

    private static final Logger log = LoggerFactory.getLogger(CiSbomService.class);

    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final SbomService sbomService;

    public CiSbomService(ProductRepository productRepository,
                          ReleaseRepository releaseRepository,
                          SbomService sbomService) {
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.sbomService = sbomService;
    }

    public CiSbomUploadResponse uploadFromCi(String productName, String version,
                                              String gitRef, MultipartFile file) {
        UUID orgId = TenantContext.getOrgId();

        Product product = productRepository.findByNameAndOrgId(productName, orgId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found: '" + productName + "'. Create it in SRPDesk first."));

        boolean releaseCreated = false;
        Release release = releaseRepository
                .findByProductIdAndVersionAndOrgId(product.getId(), version, orgId)
                .orElse(null);

        if (release == null) {
            release = new Release(product.getId(), version);
            release.setOrgId(orgId);
            if (gitRef != null && !gitRef.isBlank()) {
                release.setGitRef(gitRef);
            }
            release = releaseRepository.save(release);
            releaseCreated = true;
            log.info("CI: created release {} v{} for product {}", release.getId(), version, product.getId());
        }

        SbomUploadResponse sbomResult = sbomService.ingest(release.getId(), file);

        log.info("CI SBOM uploaded: product={}, version={}, components={}, evidence={}",
                productName, version, sbomResult.componentCount(), sbomResult.evidenceId());

        return new CiSbomUploadResponse(
                release.getId(),
                sbomResult.evidenceId(),
                sbomResult.componentCount(),
                sbomResult.sha256(),
                releaseCreated
        );
    }
}
