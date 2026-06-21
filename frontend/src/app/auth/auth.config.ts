declare global {
  interface Window {
    __collabPmConfig?: {
      keycloakUrl?: string;
      keycloakRealm?: string;
      keycloakClientId?: string;
    };
  }
}

const runtimeConfig = window.__collabPmConfig;

export const authConfig = {
  url: runtimeConfig?.keycloakUrl || 'http://localhost:8081',
  realm: runtimeConfig?.keycloakRealm || 'collab-pm',
  clientId: runtimeConfig?.keycloakClientId || 'collab-pm-frontend'
};
