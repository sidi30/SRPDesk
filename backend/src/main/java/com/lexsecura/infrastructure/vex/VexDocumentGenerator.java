package com.lexsecura.infrastructure.vex;

import com.lexsecura.domain.model.vex.VexFormat;
import com.lexsecura.domain.model.vex.VexStatement;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;

import java.util.List;

public interface VexDocumentGenerator {
    VexFormat supportedFormat();
    String generate(Product product, Release release, List<VexStatement> statements);
}
