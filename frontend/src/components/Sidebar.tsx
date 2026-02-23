import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthProvider';
import { FEATURES } from '../config/features';

const navigation = [
  { name: 'Dashboard', href: '/', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
  { name: 'Produits', href: '/products', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4' },
  { name: 'Vuln\u00e9rabilit\u00e9s', href: '/findings', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z' },
  { name: 'CRA War Room', href: '/cra-events', icon: 'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9' },
  { name: 'Piste d\u2019audit', href: '/audit', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4' },
];

const aiNavigation = [
  { name: 'Brouillon SRP', href: '/ai/srp-draft', icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z' },
  { name: 'Comm. Pack', href: '/ai/comm-pack', icon: 'M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z' },
];

export function Sidebar() {
  const location = useLocation();
  const { userName, roles, logout } = useAuth();

  return (
    <div className="flex flex-col w-64 bg-gray-900 text-white" role="navigation" aria-label="Navigation principale">
      <div className="flex items-center h-16 px-6 border-b border-gray-800">
        <h1 className="text-xl font-bold text-primary-400">SRPDesk</h1>
      </div>

      <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto" role="list">
        {navigation.map((item) => {
          const isActive = location.pathname === item.href ||
            (item.href !== '/' && location.pathname.startsWith(item.href));
          return (
            <Link
              key={item.name}
              to={item.href}
              aria-current={isActive ? 'page' : undefined}
              className={`flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-primary-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`}
            >
              <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={item.icon} />
              </svg>
              {item.name}
            </Link>
          );
        })}

        {/* Integrations & Notifications Section */}
        <div className="pt-4 mt-4 border-t border-gray-800">
          <p className="px-3 mb-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Integrations</p>
          {[
            { name: 'CI/CD', href: '/integrations', icon: 'M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4' },
            { name: 'Webhooks', href: '/webhooks', icon: 'M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1' },
            { name: 'Advisories', href: '/advisories', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z' },
          ].map((item) => {
            const isActive = location.pathname === item.href;
            return (
              <Link
                key={item.name}
                to={item.href}
                aria-current={isActive ? 'page' : undefined}
                className={`flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-primary-600 text-white'
                    : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                }`}
              >
                <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={item.icon} />
                </svg>
                {item.name}
              </Link>
            );
          })}
        </div>

        {/* AI Section */}
        <div className="pt-4 mt-4 border-t border-gray-800">
          <p className="px-3 mb-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">Assistant IA</p>
          {aiNavigation.map((item) => {
            const isActive = location.pathname === item.href;
            return (
              <Link
                key={item.name}
                to={item.href}
                aria-current={isActive ? 'page' : undefined}
                className={`flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-primary-600 text-white'
                    : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                }`}
              >
                <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={item.icon} />
                </svg>
                {item.name}
              </Link>
            );
          })}
          {FEATURES.AI_QUESTIONNAIRE && (
            <Link
              to="/ai/questionnaire"
              aria-current={location.pathname === '/ai/questionnaire' ? 'page' : undefined}
              className={`flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                location.pathname === '/ai/questionnaire'
                  ? 'bg-primary-600 text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`}
            >
              <svg className="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              Questionnaire
            </Link>
          )}
        </div>
      </nav>

      <div className="px-4 py-4 border-t border-gray-800">
        <div className="text-sm text-gray-400 mb-1">{userName}</div>
        <div className="text-xs text-gray-500 mb-3">{roles.filter(r => ['ADMIN', 'COMPLIANCE_MANAGER', 'CONTRIBUTOR'].includes(r)).join(', ')}</div>
        <button
          onClick={logout}
          aria-label="Se d\u00e9connecter"
          className="w-full px-3 py-2 text-sm text-gray-300 bg-gray-800 rounded-lg hover:bg-gray-700 transition-colors"
        >
          Sign out
        </button>
      </div>
    </div>
  );
}
