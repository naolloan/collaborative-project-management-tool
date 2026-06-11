# System Design Document

## Project Title
COOP WorkFlow - Collaborative Project Management Tool

## Document Version
Version 0.2

## 1. Purpose
This document describes the high-level system design for COOP WorkFlow, a collaborative project management tool adapted for a Cooperative Bank of Oromia internal-work context. It translates the requirements specification into an implementation-oriented architecture covering the frontend, backend, authentication, database, organization structure, local development setup, testing, CI, and deployment direction.

## 2. Design Goals

The design aims to achieve the following goals:

- Keep the architecture simple enough for an educational MVP
- Separate frontend and backend concerns clearly
- Support secure authentication and role-based access control
- Allow the system to run consistently in local and containerized environments
- Make the system testable, maintainable, and deployable
- Leave room for future expansion without overengineering the MVP
- Represent bank departments, branches, divisions, and teams through a simple organizational unit model
- Provide traceability for important project and task actions through activity/audit records

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

The system follows a layered web application architecture with five main logical parts:

1. Angular frontend
2. Spring Boot backend
3. PostgreSQL database
4. Keycloak identity provider
5. Bank organization model stored in application data

At runtime, the interaction flow is:

1. A user accesses the Angular frontend through a browser.
2. The frontend redirects the user to Keycloak for authentication when needed.
3. After successful login, the frontend receives or uses an authenticated session or token context.
4. The frontend calls secured Spring Boot API endpoints.
5. The backend validates authentication and roles using Keycloak integration.
6. The backend applies role, project-membership, and organizational-unit rules.
7. The backend reads or writes application data in PostgreSQL.
8. The backend records important activity or audit events.
9. The backend returns structured JSON responses to the frontend.

## 4.1 Bank Organization Context

The application is designed for one bank organization context in the MVP. Inside that context, the system represents internal structures as organizational units.

Examples of organizational units include:

- Head office
- Department
- Branch
- Division
- Team

The organizational unit model is intentionally generic. This avoids hard-coding a specific bank hierarchy too early and allows the same design to support both simple and more realistic internal structures later.

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
- Organization unit module
- Project module
- Task module
- Board module
- Comment module
- Activity and audit module
- Shared UI module

Suggested important frontend pieces:

- Pages or containers for project list, project details, organization unit management, task board, task details, and my tasks
- Services for API communication
- Route guards for protected pages
- Models or interfaces matching backend DTOs
- Filters for organizational unit, assignee, priority, and task status

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
- Organizational unit module
- Project module
- Task module
- Comment module
- Activity log module
- Summary or reporting module

The organizational unit module should be independent from the project module, but the project module should reference organizational units for project ownership and filtering.

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

The bank-specific version adds a third design dimension:

- Organizational unit context: which department, branch, division, or team owns a user or project

Authorization rules for the MVP:

- Administrators may access all projects and user-management-related functionality
- Project managers may manage only the projects they own or are authorized to manage
- Team members may view and work only within projects they belong to
- Task updates shall be limited by both project membership and role-based permissions
- Organizational unit management shall be administrator-only in the MVP
- Unit-based visibility shall be used for filtering and reporting first, then can later become a stricter authorization boundary if required

### 6.3 Identity Mapping

Since Keycloak is the identity source, the application database should store a reference to the Keycloak user identifier. This allows the backend to connect external identity with internal project memberships and task assignments.

The MVP does not require a separate application-managed roles table. Global roles will come from Keycloak, while project-specific access will be enforced through project membership records. Organizational unit assignment will be stored in the application database as part of the user profile or related user metadata.

### 6.4 Bank-Specific Authorization Direction

The initial design should not make organizational units a hard security boundary for all access, because project membership remains the clearest access rule for an MVP. Instead:

- Administrators can manage all units and projects.
- Project managers can create projects and assign the owning unit.
- Project members can view the unit associated with projects they can access.
- Unit managers may later receive read access to projects owned by their unit.

This approach avoids blocking development while leaving a clean path toward stronger enterprise authorization.

## 7. Data Design

### 7.1 Core Entities

The main data entities for the MVP are:

- User
- OrganizationalUnit
- Project
- ProjectMember
- Task
- Comment
- ActivityLog

### 7.2 Entity Relationships

- One user can belong to many projects through ProjectMember
- One user may belong to one organizational unit in the MVP
- One organizational unit can have many users
- One organizational unit can own many projects
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
- organizational_unit_id
- created_at
- updated_at

#### OrganizationalUnit
- id
- name
- type
- description
- active
- created_at
- updated_at

#### Project
- id
- name
- description
- created_by
- organizational_unit_id
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
- project_id
- task_id
- actor_id
- action_type
- target_type
- target_id
- old_value
- new_value
- message
- created_at

### 7.4 Workflow Representation

For the MVP, task status should be stored as a fixed enumerated value:

- TO_DO
- IN_PROGRESS
- REVIEW
- DONE

This keeps validation, board rendering, and reporting simpler. Custom workflows can be designed later without changing the initial conceptual model.

### 7.5 Organizational Unit Representation

Organizational unit type should be stored as a fixed enumerated value for the MVP:

- HEAD_OFFICE
- DEPARTMENT
- BRANCH
- DIVISION
- TEAM

Each project should reference one organizational unit. This allows the application to filter projects and dashboard metrics by the part of the bank responsible for the work.

### 7.6 Activity and Audit Representation

The existing task activity model should be extended toward a more general audit model over time. In the MVP, activity records should cover task-related events first and then expand to project, membership, and organizational unit events.

Important audit fields are:

- actor user
- action type
- target type
- target id
- project id when applicable
- task id when applicable
- old value and new value when applicable
- human-readable message
- timestamp

Audit records should be append-only from the application user's perspective.

## 8. API Design Approach

The backend will expose RESTful endpoints organized around major resources.

### 8.1 API Modules

- `/api/organizational-units`
- `/api/projects`
- `/api/projects/{projectId}/members`
- `/api/projects/{projectId}/tasks`
- `/api/tasks/{taskId}`
- `/api/tasks/{taskId}/comments`
- `/api/tasks/{taskId}/activity`
- `/api/audit`
- `/api/me/tasks`
- `/api/projects/{projectId}/summary`
- `/api/organizational-units/{unitId}/summary`

### 8.2 Example Endpoint Responsibilities

- Create, list, update, and deactivate organizational units
- Create and list projects
- Filter projects by organizational unit
- Update and archive projects
- Add and remove project members
- Create, edit, assign, and move tasks
- List board tasks by project
- Add and fetch comments
- Fetch task activity history
- Fetch audit activity for project or unit-level review
- Fetch tasks assigned to the current user
- Fetch project summary metrics
- Fetch organizational unit summary metrics

### 8.3 Request and Response Design

The backend should use DTOs rather than exposing entities directly. DTOs should:

- Validate input clearly
- Hide internal persistence details
- Keep the API stable for frontend development
- Allow tailored responses for board, details, and summary views
- Include organizational unit information in project and user responses where needed
- Avoid exposing internal security or token details

## 9. Frontend Design Approach

### 9.1 Main Screens

The MVP frontend should include:

- Login entry point or authenticated landing page
- Organizational unit management page or panel
- Project list page
- Project details page
- Project member management page
- Task board page
- Task creation and edit form
- Task details page
- My tasks page
- Unit dashboard or unit filter view

### 9.2 Frontend State and Data Flow

The frontend should:

- Use Angular services for HTTP requests
- Store only the client-side state needed for current views
- Refresh project, board, and task data from the backend when actions occur
- Use route guards to protect authenticated pages
- Load organizational units for project creation, project filtering, and dashboard context

### 9.3 Board View Design

The board page is one of the core views of the application. It should:

- Show tasks grouped by status columns
- Display key task metadata such as title, priority, assignee, and due date
- Allow task movement between columns
- Offer filtering by assignee, priority, and status
- Show the selected project's organizational unit when available
- Link to task details and comments

### 9.4 Bank-Branded UI Direction

The frontend should use a COOP-inspired internal dashboard style while avoiding hard-coded dependency on public brand assets. The visual direction should include:

- Bright cyan and blue color palette
- Clean white surfaces and rounded cards
- Clear dashboard metrics and workflow columns
- Internal enterprise wording such as workspace, delivery focus, units, members, and activity
- Responsive layout for desktop and smaller screens

The UI should remain functional before decorative. Brand polish must not hide important task, permission, or status information.

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
- Tests for organizational unit creation, update, deactivation, and project assignment
- Tests confirming non-admin users cannot manage organizational units

### 12.2 Frontend Testing

The frontend should include:

- Component tests for important UI units
- Service tests for API interaction logic
- Basic integration tests for core workflows if time permits

### 12.3 End-to-End Validation

Critical user flows that should be validated include:

- User authentication
- Organizational unit management
- Project creation
- Project assignment to an organizational unit
- Project membership management
- Task creation and assignment
- Task status movement on the board
- Comment creation
- Audit/activity record creation for important actions

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
- Restrict organizational unit management to administrators
- Treat organizational unit visibility as reporting context first, not a replacement for project membership authorization
- Validate request input on the backend
- Avoid exposing sensitive data in API responses
- Store secrets outside source code
- Keep activity and audit records append-only from normal user workflows

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
- Use a generic organizational unit entity instead of hard-coding bank departments or branches
- Assign each project to one owning organizational unit in the MVP
- Use organizational unit context for filtering and reporting first
- Keep project membership as the primary project access boundary
- Extend existing activity logging toward audit logging instead of building a separate compliance system immediately
- Separate frontend and backend cleanly
- Prioritize clear API contracts over early optimization
- Add Kubernetes support only after Docker-based execution is stable

## 16.1 Implementation Phases for Bank-Specific Features

The bank-specific implementation should proceed in phases:

1. Add organizational unit data model, backend APIs, and tests.
2. Add project ownership by organizational unit.
3. Add frontend unit management and project unit selection.
4. Add unit filtering and dashboard visibility.
5. Expand activity logs to cover project, membership, and unit changes.
6. Consider unit-manager permissions and approval workflows only after the core unit model is stable.

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
- Unit-manager read-only permissions
- Formal approval workflows
- Audit export and retention policies
- Integration with enterprise HR or directory systems

## 18. Conclusion

This design provides a practical architecture for the Collaborative Project Management Tool based on the agreed stack and MVP scope. It emphasizes clean separation of concerns, secure authentication with Keycloak, a maintainable Angular and Spring Boot structure, and a path from local development to Docker-based execution and future Kubernetes deployment.
