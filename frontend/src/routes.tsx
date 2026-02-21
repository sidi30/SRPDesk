import { Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { ProductsPage } from './pages/ProductsPage';
import { ProductDetailPage } from './pages/ProductDetailPage';
import { ReleaseDetailPage } from './pages/ReleaseDetailPage';
import { FindingsPage } from './pages/FindingsPage';
import { AuditTrailPage } from './pages/AuditTrailPage';
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
        <Route path="/audit" element={<AuditTrailPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  );
}
