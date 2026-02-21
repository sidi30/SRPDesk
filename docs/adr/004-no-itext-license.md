# ADR 004: No iText — Use Apache PDFBox for PDF Generation

## Status
Accepted

## Context
PDF report generation is a planned feature. iText is a popular Java PDF library but uses AGPL license, which is incompatible with proprietary SaaS distribution.

## Decision
- Do NOT use iText (AGPL licensed)
- Use Apache PDFBox (Apache 2.0 license) when PDF generation is needed (v2)
- For MVP, no PDF export — focus on in-app compliance views

## Consequences
- No license risk from AGPL contamination
- PDFBox is less feature-rich than iText but sufficient for compliance reports
- PDF generation deferred to v2
- All dependencies must be Apache 2.0, MIT, or BSD compatible
