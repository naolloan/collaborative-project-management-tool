# Production Deployment Guide

This repository now supports an image-based deployment flow for a hosted environment.

## What this adds

- published backend and frontend container images
- a production compose file at `infra/docker-compose.prod.yml`
- a production Spring profile
- a manual GitHub Actions deployment workflow that can ship the compose bundle to a server over SSH

## 1. What you need first

- a Linux server with Docker and Docker Compose available
- a public hostname for the frontend
- a public hostname for Keycloak
- a Docker Hub account for image publishing
- GitHub repository secrets configured for deployment
- a reverse proxy or firewall rule that exposes the frontend and Keycloak ports you choose

## 2. Required GitHub secrets

For image publishing:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

For server deployment:

- `DEPLOY_HOST`
- `DEPLOY_PORT`
- `DEPLOY_USER`
- `DEPLOY_SSH_KEY`
- `DEPLOY_PATH`
- `PROD_ENV_FILE`

`PROD_ENV_FILE` should contain the full contents of the production `.env` file that will be written on the server.

## 3. Prepare production environment values

Use `infra/.env.production.example` as the template for your server environment file.

Important values:

- `FRONTEND_PUBLIC_URL` should be the public browser URL for the Angular app
- `KEYCLOAK_PUBLIC_URL` should be the public URL users will open in the browser
- `KEYCLOAK_HOST_PORT` is the server-side port that exposes Keycloak before any outer reverse proxy
- `KEYCLOAK_ISSUER_URI` should point to the realm issuer URL
- `KEYCLOAK_JWK_SET_URI` should point to the realm certs endpoint
- `BACKEND_IMAGE` and `FRONTEND_IMAGE` should point to published images

## 4. Publish images

The Docker publish workflow pushes backend and frontend images on version tags and on manual dispatch.

Expected image tags include:

- `latest`
- the Git SHA
- the git tag, when triggered from a version tag

## 5. Deploy to a server

The `Deploy Production Stack` workflow:

- writes `infra/.env.production` from `PROD_ENV_FILE`
- copies `infra/docker-compose.prod.yml`
- generates a Keycloak realm import using the production frontend and Keycloak URLs
- pulls the configured images
- starts the stack on the target host

This workflow is manual by design so deployments stay explicit.

## 6. Recommended next hardening

- put nginx, Caddy, or Traefik in front for HTTPS and domain routing
- tighten Keycloak hostname, proxy, and admin access settings for the final hosted domain
- replace schema bootstrap/update paths with a migration tool such as Flyway
- store production secrets in a dedicated secret manager
- back up the Postgres data and exported Keycloak configuration regularly
