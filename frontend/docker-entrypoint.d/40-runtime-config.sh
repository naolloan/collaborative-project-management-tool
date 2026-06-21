#!/bin/sh
set -eu

cat <<EOF >/usr/share/nginx/html/config.js
window.__collabPmConfig = {
  keycloakUrl: '${KEYCLOAK_URL:-http://localhost:8081}',
  keycloakRealm: '${KEYCLOAK_REALM:-collab-pm}',
  keycloakClientId: '${KEYCLOAK_CLIENT_ID:-collab-pm-frontend}'
};
EOF
