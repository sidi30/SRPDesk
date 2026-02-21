-- CRA Requirements Seed Data
-- Based on EU Cyber Resilience Act (2024/2847) key articles and annexes

INSERT INTO requirements (id, article_ref, title, description, category, applicable_to) VALUES
-- Article 13 - Obligations of manufacturers
(gen_random_uuid(), 'Art.13.1', 'Security risk assessment', 'Manufacturers shall carry out a cybersecurity risk assessment of the product with digital elements and take the outcome into account during planning, design, development, production, delivery and maintenance phases.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Art.13.2', 'Due diligence on components', 'When placing a product with digital elements on the market, the manufacturer shall ensure that the product has been designed and developed in accordance with the essential requirements set out in Annex I.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Art.13.3', 'Technical documentation', 'Manufacturers shall draw up the technical documentation referred to in Annex VII before placing the product on the market and shall keep it up to date.', 'CONFORMITY_ASSESSMENT', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Art.13.4', 'Conformity assessment procedures', 'Manufacturers shall carry out the applicable conformity assessment procedures referred to in Article 32.', 'CONFORMITY_ASSESSMENT', 'CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Art.13.5', 'EU declaration of conformity', 'Manufacturers shall draw up an EU declaration of conformity and affix the CE marking.', 'CONFORMITY_ASSESSMENT', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Art.13.6', 'Vulnerability handling process', 'Manufacturers shall put in place appropriate policies and procedures to handle and remediate vulnerabilities, including coordinated vulnerability disclosure.', 'VULNERABILITY_HANDLING', 'DEFAULT,CLASS_I,CLASS_II'),

-- Article 14 - Reporting obligations
(gen_random_uuid(), 'Art.14.1', 'Actively exploited vulnerability notification', 'Manufacturers shall notify ENISA of any actively exploited vulnerability within 24 hours of becoming aware.', 'INCIDENT_REPORTING', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Art.14.2', 'Severe incident notification', 'Manufacturers shall notify ENISA of any severe incident having an impact on the security of the product within 24 hours.', 'INCIDENT_REPORTING', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Art.14.3', 'Vulnerability notification to users', 'Manufacturers shall inform the users of the product about the vulnerability and corrective measures without undue delay.', 'INCIDENT_REPORTING', 'DEFAULT,CLASS_I,CLASS_II'),

-- Annex I Part I - Security requirements for products
(gen_random_uuid(), 'Annex.I.1.1', 'Security by default configuration', 'Products shall be made available on the market with a secure by default configuration, including the possibility to reset to the original state.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.2', 'Protection against unauthorized access', 'Products shall be designed to protect the confidentiality of stored, transmitted and otherwise processed data, including through encryption of relevant data at rest and in transit.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.3', 'Data integrity protection', 'Products shall protect the integrity of stored, transmitted and otherwise processed data, commands, programs and configuration against manipulation or modification.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.4', 'Data minimization', 'Products shall process only data that is adequate, relevant and limited to what is necessary for their intended use.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.5', 'Availability and resilience', 'Products shall be designed to ensure availability of essential functions, including resilience against and mitigation of denial-of-service attacks.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.6', 'Minimize attack surface', 'Products shall be designed to reduce attack surfaces, including external interfaces.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.7', 'Incident impact mitigation', 'Products shall be designed to mitigate the impact of a security incident using appropriate exploitation mitigation mechanisms and techniques.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.8', 'Security logging', 'Products shall provide security-relevant information by recording and/or monitoring relevant internal activity, including the access to or modification of data, services or functions.', 'SECURITY_BY_DESIGN', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.1.9', 'Secure update mechanism', 'Products shall ensure that security updates can be installed in a timely manner, including through automatic updates with user opt-out.', 'UPDATE_MANAGEMENT', 'DEFAULT,CLASS_I,CLASS_II'),

-- Annex I Part II - Vulnerability handling requirements
(gen_random_uuid(), 'Annex.I.2.1', 'Identify and document vulnerabilities', 'Manufacturers shall identify and document vulnerabilities and components contained in products, including by drawing up an SBOM.', 'SBOM', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.2.2', 'Timely vulnerability remediation', 'Apply effective and regular tests and reviews of the security of the product and address and remediate vulnerabilities without delay.', 'VULNERABILITY_HANDLING', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.2.3', 'Security updates distribution', 'Ensure timely distribution of security updates, free of charge for the support period.', 'UPDATE_MANAGEMENT', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.2.4', 'Coordinated vulnerability disclosure', 'Apply a policy on coordinated vulnerability disclosure.', 'VULNERABILITY_HANDLING', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.2.5', 'Vulnerability information sharing', 'Share information about vulnerabilities, including third-party components, providing the SBOM upon request.', 'SBOM', 'DEFAULT,CLASS_I,CLASS_II'),
(gen_random_uuid(), 'Annex.I.2.6', 'Security support period', 'Ensure a support period of at least 5 years from the product placement on the market.', 'UPDATE_MANAGEMENT', 'DEFAULT,CLASS_I,CLASS_II'),

-- Annex II - Information and instructions to the user
(gen_random_uuid(), 'Annex.II.1', 'User documentation', 'Provide clear and understandable instructions for users on secure installation, operation, and maintenance of the product.', 'CONFORMITY_ASSESSMENT', 'DEFAULT,CLASS_I,CLASS_II');
