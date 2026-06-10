# Collaborative Project Management Tool

This repository contains the early implementation of a Jira-like collaborative project management tool built with:

- Angular frontend
- Spring Boot backend
- PostgreSQL database
- Keycloak for authentication and role management
- Docker for local supporting services

## Current Structure

- `docs/` project requirements and system design
- `backend/` Spring Boot API and business logic
- `frontend/` Angular user interface
- `infra/` local infrastructure configuration

## Current Status

The project has a working early MVP with authentication, project management, project members, task workflow, task comments, task activity, local Docker services, and GitHub Actions CI.

## Local Development

Start PostgreSQL and Keycloak:

```bash
docker context use default
docker compose -f infra/docker-compose.yml up -d
```

Local service URLs:

- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5433`
- Keycloak: `http://localhost:8081`
- Frontend: `http://127.0.0.1:4200` by default, or the alternate port printed by Angular if `4200` is busy

Run backend checks:

```bash
cd backend
unset DEBUG
mvn test
mvn spring-boot:run
```

Verify the backend health endpoint:

```bash
curl http://localhost:8080/api/health
```

If PostgreSQL is already installed locally, it may use port `5432`. This project maps the Docker PostgreSQL container to host port `5433` to avoid that conflict.

## Keycloak Local Setup

Use these values for local development:

- Realm: `collab-pm`
- Frontend client: `collab-pm-frontend`
- Realm roles: `ADMINISTRATOR`, `PROJECT_MANAGER`, `TEAM_MEMBER`

Allowed frontend URLs in the Keycloak client should include:

- Valid redirect URIs: `http://localhost:4200/*`, `http://127.0.0.1:4200/*`
- Valid post logout redirect URIs: `http://localhost:4200/*`, `http://127.0.0.1:4200/*`
- Web origins: `http://localhost:4200`, `http://127.0.0.1:4200`

If Angular starts on another port, such as `43297`, add that port to the same Keycloak client settings:

- Valid redirect URIs: `http://localhost:43297/*`, `http://127.0.0.1:43297/*`
- Valid post logout redirect URIs: `http://localhost:43297/*`, `http://127.0.0.1:43297/*`
- Web origins: `http://localhost:43297`, `http://127.0.0.1:43297`

The backend CORS configuration allows local frontend development from any `localhost` or `127.0.0.1` port.

## Docker Images

The backend and frontend each have their own Dockerfile:

- `backend/Dockerfile` builds the Spring Boot API into a runnable Java container.
- `frontend/Dockerfile` builds the Angular app and serves it with Nginx.

GitHub Actions currently verifies that both images can be built using `.github/workflows/docker-build.yml`.

Local image build commands:

```bash
docker build -t collab-pm-backend:local ./backend
docker build -t collab-pm-frontend:local ./frontend
```

Docker Hub publishing is the next infrastructure step. It will require Docker Hub repositories and these GitHub repository secrets:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

Expected Docker Hub repositories:

- `collab-pm-backend`
- `collab-pm-frontend`

After the repositories and secrets are configured, run the `Docker Hub Publish` workflow manually from the GitHub Actions tab. The workflow publishes both images with these tags:

- `latest`
- the current Git commit SHA
