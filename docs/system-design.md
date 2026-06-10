# System Design Document

## Project Title
Collaborative Project Management Tool

## Document Version
Version 0.1

## 1. Purpose
This document describes the high-level system design for the Collaborative Project Management Tool. It translates the requirements specification into an implementation-oriented architecture covering the frontend, backend, authentication, database, local development setup, testing, CI, and deployment direction.

## 2. Design Goals

The design aims to achieve the following goals:

- Keep the architecture simple enough for an educational MVP
- Separate frontend and backend concerns clearly
- Support secure authentication and role-based access control
- Allow the system to run consistently in local and containerized environments
- Make the system testable, maintainable, and deployable
- Leave room for future expansion without overengineering the MVP

## 3. Technology Stack

### 3.1 Frontend
- Angular
- TypeScript
- Angular services for API communication
- Angular routing for navigation

### 3.2 Backend
- Spring Boot
- REST API architecture
- Spring Security integration with Keycloak
- Service and repository layers for business logic and persistence

### 3.3 Data and Identity
- PostgreSQL for application data
- Keycloak for authentication, user identity, and role management

### 3.4 DevOps and Delivery
- Docker for local supporting services and containerized execution
- Git and GitHub for version control
- GitHub Actions for continuous integration
- Docker Hub for image storage
- Kubernetes for deployment orchestration

## 4. High-Level Architecture

The system follows a layered web application architecture with four main parts:

1. Angular frontend
2. Spring Boot backend
3. PostgreSQL database
4. Keycloak identity provider

At runtime, the interaction flow is:

1. A user accesses the Angular frontend through a browser.
2. The frontend redirects the user to Keycloak for authentication when needed.
3. After successful login, the frontend receives or uses an authenticated session or token context.
4. The frontend calls secured Spring Boot API endpoints.
5. The backend validates authentication and roles using Keycloak integration.
6. The backend applies business logic and reads or writes application data in PostgreSQL.
7. The backend returns structured JSON responses to the frontend.

## 5. Logical Component Design

### 5.1 Frontend Components

The Angular frontend will be responsible for:

- Rendering the user interface
- Managing navigation and route protection
- Displaying projects, tasks, boards, and comments
- Sending requests to the backend API
- Handling user state and permissions from the authenticated context

Suggested frontend feature modules:

- Authentication module
- Project module
- Task module
- Board module
- Comment module
- Shared UI module

Suggested important frontend pieces:

- Pages or containers for project list, project details, task board, task details, and my tasks
- Services for API communication
- Route guards for protected pages
- Models or interfaces matching backend DTOs

### 5.2 Backend Components

The Spring Boot backend will be responsible for:

- Exposing REST endpoints
- Validating authenticated access
- Enforcing role and membership rules
- Executing business logic
- Persisting application data
- Recording activity history

Suggested backend layers:

- Controller layer: accepts and returns API requests and responses
- Service layer: contains business rules
- Repository layer: database interaction
- Security layer: authentication context and authorization checks
- DTO layer: request and response contracts

Suggested backend modules:

- Auth context module
- User and membership module
- Project module
- Task module
- Comment module
- Activity log module
- Summary or reporting module

## 6. Authentication and Authorization Design

### 6.1 Authentication

Keycloak will manage login, identity, and global roles. The application will not implement custom password management in the MVP.

Expected authentication flow:

1. User opens the frontend.
2. If unauthenticated, the frontend redirects the user to Keycloak.
3. Keycloak authenticates the user.
4. After authentication, the frontend obtains the authenticated user context.
5. API requests include the access token or authenticated session information.
6. Spring Boot validates the token and extracts user identity and roles.

### 6.2 Authorization

Authorization will use two levels:

- System role: administrator, project manager, team member
- Project membership: whether the user belongs to the requested project

Authorization rules for the MVP:

- Administrators may access all projects and user-management-related functionality
- Project managers may manage only the projects they own or are authorized to manage
- Team members may view and work only within projects they belong to
- Task updates shall be limited by both project membership and role-based permissions

### 6.3 Identity Mapping

Since Keycloak is the identity source, the application database should store a reference to the Keycloak user identifier. This allows the backend to connect external identity with internal project memberships and task assignments.

The MVP does not require a separate application-managed roles table. Global roles will come from Keycloak, while project-specific access will be enforced through project membership records.

## 7. Data Design

### 7.1 Core Entities

The main data entities for the MVP are:

- User
- Project
- ProjectMember
- Task
- Comment
- ActivityLog

### 7.2 Entity Relationships

- One user can belong to many projects through ProjectMember
- One project can have many members
- One project can have many tasks
- One task belongs to one project
- One task can have one assignee in the MVP
- One task can have many comments
- One task can have many activity log entries
- One user can create many tasks and comments

### 7.3 Suggested Database Structure

#### User
- id
- keycloak_user_id
- full_name
- email
- system_role
- created_at
- updated_at

#### Project
- id
- name
- description
- created_by
- start_date
- due_date
- status
- created_at
- updated_at

#### ProjectMember
- id
- project_id
- user_id
- project_role
- joined_at

#### Task
- id
- project_id
- title
- description
- status
- priority
- assignee_id
- created_by
- due_date
- created_at
- updated_at

#### Comment
- id
- task_id
- author_id
- content
- created_at
- updated_at

#### ActivityLog
- id
- task_id
- actor_id
- action_type
- old_value
- new_value
- created_at

### 7.4 Workflow Representation

For the MVP, task status should be stored as a fixed enumerated value:

- TO_DO
- IN_PROGRESS
- REVIEW
- DONE

This keeps validation, board rendering, and reporting simpler. Custom workflows can be designed later without changing the initial conceptual model.

## 8. API Design Approach

The backend will expose RESTful endpoints organized around major resources.

### 8.1 API Modules

- `/api/projects`
- `/api/projects/{projectId}/members`
- `/api/projects/{projectId}/tasks`
- `/api/tasks/{taskId}`
- `/api/tasks/{taskId}/comments`
- `/api/tasks/{taskId}/activity`
- `/api/me/tasks`
- `/api/projects/{projectId}/summary`

### 8.2 Example Endpoint Responsibilities

- Create and list projects
- Update and archive projects
- Add and remove project members
- Create, edit, assign, and move tasks
- List board tasks by project
- Add and fetch comments
- Fetch task activity history
- Fetch tasks assigned to the current user
- Fetch project summary metrics

### 8.3 Request and Response Design

The backend should use DTOs rather than exposing entities directly. DTOs should:

- Validate input clearly
- Hide internal persistence details
- Keep the API stable for frontend development
- Allow tailored responses for board, details, and summary views

## 9. Frontend Design Approach

### 9.1 Main Screens

The MVP frontend should include:

- Login entry point or authenticated landing page
- Project list page
- Project details page
- Project member management page
- Task board page
- Task creation and edit form
- Task details page
- My tasks page

### 9.2 Frontend State and Data Flow

The frontend should:

- Use Angular services for HTTP requests
- Store only the client-side state needed for current views
- Refresh project, board, and task data from the backend when actions occur
- Use route guards to protect authenticated pages

### 9.3 Board View Design

The board page is one of the core views of the application. It should:

- Show tasks grouped by status columns
- Display key task metadata such as title, priority, assignee, and due date
- Allow task movement between columns
- Offer filtering by assignee, priority, and status
- Link to task details and comments

## 10. Local Development and Container Design

### 10.1 Local Development Services

The local development environment should support these services:

- Angular frontend
- Spring Boot backend
- PostgreSQL
- Keycloak

### 10.2 Docker Usage

Docker should be used to run supporting services locally and optionally containerize the application services. A practical local setup is:

- Frontend container or local Angular dev server
- Backend container or local Spring Boot process
- PostgreSQL container
- Keycloak container

For team consistency, a `docker-compose.yml` or equivalent orchestration file should eventually define local service startup.

### 10.3 Environment Configuration

The application will require environment-specific configuration values such as:

- Database URL, username, and password
- Keycloak server URL
- Keycloak realm
- Keycloak client ID
- Backend API base URL
- Frontend application URL

These values should be externalized through environment variables or environment-specific config files.

## 11. Deployment Design Direction

### 11.1 Container Strategy

The deployment model should package at least the frontend and backend as separate deployable units. PostgreSQL and Keycloak may be self-managed or externally managed depending on the deployment environment.

Recommended deployment units:

- Frontend container image
- Backend container image

### 11.2 Kubernetes Direction

Kubernetes will be considered after the application is stable in Docker. A likely Kubernetes setup will include:

- Frontend Deployment and Service
- Backend Deployment and Service
- Ingress for external access
- Secrets and ConfigMaps for environment settings

If PostgreSQL and Keycloak are self-hosted in the cluster, they may also require:

- Separate Deployments or StatefulSets
- Persistent volumes
- Service definitions

For the MVP, the important goal is to design the application so it can be deployed to Kubernetes later without major restructuring.

## 12. Testing Strategy

### 12.1 Backend Testing

The backend should include:

- Unit tests for service logic
- Repository or persistence tests where useful
- Controller or integration tests for critical API flows
- Authorization-related tests for protected endpoints

### 12.2 Frontend Testing

The frontend should include:

- Component tests for important UI units
- Service tests for API interaction logic
- Basic integration tests for core workflows if time permits

### 12.3 End-to-End Validation

Critical user flows that should be validated include:

- User authentication
- Project creation
- Project membership management
- Task creation and assignment
- Task status movement on the board
- Comment creation

## 13. CI/CD Design

GitHub Actions should be used to automate basic quality checks for both frontend and backend.

Suggested CI workflow responsibilities:

- Run frontend install and tests
- Run backend build and tests
- Validate Docker builds when appropriate
- Enforce checks on pull requests before merge

Later, CI or CD pipelines may also:

- Build and tag Docker images
- Push images to Docker Hub
- Trigger deployment steps

## 14. Security Considerations

The design should address the following security concerns:

- Use Keycloak for authentication rather than custom password handling
- Protect backend endpoints by default and open only necessary public paths
- Validate user authorization against both role and project membership
- Validate request input on the backend
- Avoid exposing sensitive data in API responses
- Store secrets outside source code

## 15. Suggested Repository or Directory Strategy

Since the frontend and backend are intended to be separate codebases or repositories, a practical structure is:

- `frontend/` for Angular application
- `backend/` for Spring Boot application
- `docs/` for requirements, design, and related documents
- `infra/` for Docker, container, and deployment configuration when needed

If separate repositories are used instead of a monorepo, the same logical separation should still be preserved in naming and documentation.

## 16. Key Design Decisions for the MVP

The following design decisions are intentionally chosen to keep the MVP achievable:

- Use Keycloak instead of custom authentication
- Use a fixed workflow instead of customizable workflow configuration
- Use single-assignee tasks instead of multiple assignees
- Separate frontend and backend cleanly
- Prioritize clear API contracts over early optimization
- Add Kubernetes support only after Docker-based execution is stable

## 17. Risks and Future Extensions

Potential implementation risks include:

- Keycloak integration complexity for first-time setup
- Coordination between frontend route protection and backend authorization
- Managing consistent configuration across local, CI, and deployment environments

Likely future extensions include:

- Custom workflows
- Sprint and backlog management
- Notifications
- File attachments
- Advanced dashboards and analytics
- Real-time updates

## 18. Conclusion

This design provides a practical architecture for the Collaborative Project Management Tool based on the agreed stack and MVP scope. It emphasizes clean separation of concerns, secure authentication with Keycloak, a maintainable Angular and Spring Boot structure, and a path from local development to Docker-based execution and future Kubernetes deployment.
