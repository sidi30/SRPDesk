import { Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { FEATURES } from './config/features';
import { DashboardPage } from './pages/DashboardPage';
import { ProductsPage } from './pages/ProductsPage';
import { ProductDetailPage } from './pages/ProductDetailPage';
import { ReleaseDetailPage } from './pages/ReleaseDetailPage';
import { FindingsPage } from './pages/FindingsPage';
import { CraEventsPage } from './pages/CraEventsPage';
import { CraEventDetailPage } from './pages/CraEventDetailPage';
import { AuditTrailPage } from './pages/AuditTrailPage';
import { AiSrpDraftPage } from './pages/AiSrpDraftPage';
import { AiCommPackPage } from './pages/AiCommPackPage';
import { AiQuestionnairePage } from './pages/AiQuestionnairePage';
import { IntegrationsPage } from './pages/IntegrationsPage';
import { WebhooksPage } from './pages/WebhooksPage';
import { AdvisoriesPage } from './pages/AdvisoriesPage';
import { CvdReportsPage } from './pages/CvdReportsPage';
import { NotFoundPage } from './pages/NotFoundPage';

export function AppRoutes() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path="/products" element={<ProtectedRoute><ProductsPage /></ProtectedRoute>} />
        <Route path="/products/:id" element={<ProtectedRoute><ProductDetailPage /></ProtectedRoute>} />
        <Route path="/releases/:id" element={<ProtectedRoute><ReleaseDetailPage /></ProtectedRoute>} />
        <Route path="/findings" element={<ProtectedRoute><FindingsPage /></ProtectedRoute>} />
        <Route path="/cra-events" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><CraEventsPage /></ProtectedRoute>} />
        <Route path="/cra-events/:id" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><CraEventDetailPage /></ProtectedRoute>} />
        <Route path="/audit" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><AuditTrailPage /></ProtectedRoute>} />
        <Route path="/ai/srp-draft" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><AiSrpDraftPage /></ProtectedRoute>} />
        <Route path="/ai/comm-pack" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><AiCommPackPage /></ProtectedRoute>} />
        {FEATURES.AI_QUESTIONNAIRE && (
          <Route path="/ai/questionnaire" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><AiQuestionnairePage /></ProtectedRoute>} />
        )}
        <Route path="/integrations" element={<ProtectedRoute requiredRoles={['ADMIN']}><IntegrationsPage /></ProtectedRoute>} />
        <Route path="/webhooks" element={<ProtectedRoute requiredRoles={['ADMIN']}><WebhooksPage /></ProtectedRoute>} />
        <Route path="/advisories" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><AdvisoriesPage /></ProtectedRoute>} />
        <Route path="/cvd-reports" element={<ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}><CvdReportsPage /></ProtectedRoute>} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  );
}
