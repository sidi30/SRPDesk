import { Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
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
import { NotFoundPage } from './pages/NotFoundPage';

export function AppRoutes() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/releases/:id" element={<ReleaseDetailPage />} />
        <Route path="/findings" element={<FindingsPage />} />
        <Route path="/cra-events" element={<CraEventsPage />} />
        <Route path="/cra-events/:id" element={<CraEventDetailPage />} />
        <Route path="/audit" element={<AuditTrailPage />} />
        <Route path="/ai/srp-draft" element={<AiSrpDraftPage />} />
        <Route path="/ai/comm-pack" element={<AiCommPackPage />} />
        <Route path="/ai/questionnaire" element={<AiQuestionnairePage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  );
}
