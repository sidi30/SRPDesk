import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import keycloak from './keycloak';

interface AuthContextType {
  isAuthenticated: boolean;
  token: string | undefined;
  userId: string | undefined;
  orgId: string | undefined;
  roles: string[];
  userName: string | undefined;
  login: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    keycloak
      .init({ onLoad: 'login-required', checkLoginIframe: false })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        setIsLoading(false);

        // Token refresh
        setInterval(() => {
          keycloak.updateToken(60).catch(() => {
            keycloak.login();
          });
        }, 30000);
      })
      .catch((err) => {
        console.error('Keycloak init error:', err);
        setIsLoading(false);
      });
  }, []);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg text-gray-600">Loading...</div>
      </div>
    );
  }

  const token = keycloak.token;
  const userId = keycloak.subject;
  const orgId = keycloak.tokenParsed?.org_id as string | undefined;
  const roles = (keycloak.tokenParsed?.roles as string[]) ??
    (keycloak.tokenParsed?.realm_access?.roles as string[]) ?? [];
  const userName = keycloak.tokenParsed?.preferred_username as string | undefined;

  const hasRole = (role: string) => roles.includes(role);
  const login = () => keycloak.login();
  const logout = () => keycloak.logout();

  return (
    <AuthContext.Provider
      value={{ isAuthenticated, token, userId, orgId, roles, userName, login, logout, hasRole }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
