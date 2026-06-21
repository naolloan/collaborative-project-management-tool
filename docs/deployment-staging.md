# Staging Deployment Guide

This project can be run as a staged full stack with Docker Compose:

- `frontend` on `http://localhost:8088`
- `backend` on `http://localhost:18080`
- `keycloak` on `http://localhost:8081`
- `postgres` on port `5433`

## 1. Prepare environment variables

Copy the example file:

```bash
cp infra/.env.example infra/.env
```

Update the secrets in `infra/.env` before using a shared environment.
The host-facing ports are configurable there as well, which is useful if your machine is already using `8080`, `8081`, `8088`, or `5433`.
If you change the Keycloak host port, also update `KEYCLOAK_HOSTNAME`, `KEYCLOAK_ISSUER_URI`, and `FRONTEND_KEYCLOAK_URL` to match it.

## 2. Start the stack

From the repository root:

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml up --build
```

## 3. Keycloak setup

The compose stack imports a starter realm automatically from:

`infra/keycloak/realm-import/collab-pm-realm.json`

That import currently creates:

- Realm: `collab-pm`
- Public client: `collab-pm-frontend`
- Redirect URI: `http://localhost:8088/*`
- Web origin: `http://localhost:8088`
- Realm roles:
  - `ADMINISTRATOR`
  - `PROJECT_MANAGER`
  - `TEAM_MEMBER`

You still need to create users and assign roles in Keycloak after startup.

## 4. Backend profile behavior

The compose stack uses the `staging` Spring profile:

- `ddl-auto=validate`
- `sql.init.mode=never`

That keeps staging safer than the development defaults once the target database schema already exists.
For a brand-new staging database, switch to one of these bootstrap approaches first:

- temporarily set `SPRING_JPA_HIBERNATE_DDL_AUTO=update` and `SPRING_SQL_INIT_MODE=always`
- or apply the schema yourself before switching back to the stricter staging defaults

If you reuse an existing `postgres_data` volume from an older run, remember that changing `POSTGRES_PASSWORD` in `.env` does not rewrite the database role password inside Postgres. Either recreate the volume or align the role password manually before starting the backend.

## 5. Current deployment gaps

Before calling this production-ready, the following should still be added:

- HTTPS and real domain configuration
- production secrets management
- production-safe database migration workflow
- separate production compose or orchestration manifests
