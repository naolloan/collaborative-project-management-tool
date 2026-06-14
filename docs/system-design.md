# System Design Document

## Project Title
COOP WorkFlow - Collaborative Project Management Tool

## Document Version
Version 0.3

## 1. Purpose
This document describes the high-level system design for COOP WorkFlow, a collaborative project and delivery management platform for the Cooperative Bank of Oromia. It translates the updated requirements into an implementation-oriented architecture covering product modules, user experience direction, frontend and backend structure, data design, authentication, local development, testing, CI, and deployment direction.

## 2. Design Goals

The design aims to achieve the following goals:

- support a business-focused enterprise user experience
- keep technical complexity manageable for an incremental educational implementation
- separate frontend and backend concerns clearly
- support secure authentication and role-based access control
- represent COOP organizational ownership through units
- provide portfolio, project, and personal work visibility in one platform
- give the product a wider, more realistic internal dashboard layout on desktop screens
- leave room for future enterprise features without overengineering the current phase

## 3. System Context and Architecture Overview

The system uses a layered web application architecture with four primary runtime parts and one supporting business model.

Primary runtime parts:

1. Angular frontend
2. Spring Boot backend
3. PostgreSQL database
4. Keycloak identity provider

Supporting business model:

5. COOP organizational structure represented as application data

At runtime, the interaction flow is expected to be:

1. A user accesses the Angular frontend through a browser.
2. The frontend redirects the user to Keycloak when authentication is required.
3. After successful login, the frontend obtains authenticated user context.
4. The frontend calls secured Spring Boot API endpoints.
5. The backend validates identity and roles using Keycloak integration.
6. The backend applies role, project membership, and organizational context rules.
7. The backend reads or writes application data in PostgreSQL.
8. The backend records important activity and audit events.
9. The backend returns structured JSON responses for the frontend views.

## 4. Frontend Design Approach

### 4.1 Application Shell and Navigation
The frontend should present itself as an internal enterprise workspace rather than a narrow single-column demo page. The desktop shell should therefore move toward a wider or full-width layout with controlled horizontal gutters.

Key shell characteristics:

- primary navigation for dashboard, projects, units, members, task board, and profile
- workspace-oriented page headings and summaries
- wide desktop content area suitable for dashboards, boards, and tables
- responsive behavior for tablet and smaller screens
- consistent page-level status and action areas

### 4.2 Main Frontend Workspaces
The main product workspaces should include:

- business dashboard
- projects workspace
- organizational units workspace
- project members workspace
- task board workspace
- profile workspace
- notification center or panel
- audit or activity review surfaces within the authorized scope

### 4.3 Business Dashboard Design
The dashboard is a business view and should emphasize operational information such as:

- portfolio summary metrics
- unit ownership coverage
- project watchlist
- selected project summary
- personal assigned work
- due soon and overdue alerts

The dashboard should not treat backend or authentication diagnostics as primary content. Those details may still exist for support purposes, but they should live in the profile area, an admin page, or a support-focused utility view.

### 4.4 Profile Workspace Design
The profile workspace should gather user-specific information that does not belong on the business dashboard. Typical profile content should include:

- name, email, and unit association
- role context and access summary
- personal preferences such as display density and landing page
- reminder and notification preferences
- recent personal activity or assigned work summary
- optional support-oriented session context for troubleshooting

### 4.5 Suggested Frontend Modules or Feature Areas
Suggested Angular feature areas are:

- authentication and route protection
- shared application shell
- dashboard and reporting
- organizational unit management
- project management
- milestone management
- project membership management
- task board and task detail
- comments and activity
- profile and preferences
- notifications
- shared UI utilities

### 4.6 Frontend State and Data Flow
The frontend should:

- use Angular services for HTTP communication
- use Angular signals or equivalent reactive state for current workspace data
- compute dashboard and summary view models from API responses or loaded entities
- keep page-local editing state close to the workspace that owns it
- avoid storing sensitive token or security internals in unnecessary view state
- refresh relevant lists and summaries after create, update, archive, or status actions

## 5. Backend Design Approach

### 5.1 Backend Responsibilities
The Spring Boot backend is responsible for:

- exposing REST endpoints
- validating authenticated access
- enforcing role and membership rules
- applying business logic for units, projects, milestones, tasks, and notifications
- persisting application data
- returning DTO-based responses for business views
- recording activity and audit history

### 5.2 Suggested Backend Modules
Suggested backend modules are:

- authentication context and profile module
- organizational unit module
- project module
- milestone module
- project membership module
- task module
- comment module
- notification module
- activity and audit module
- dashboard and reporting module

### 5.3 Layered Backend Structure
The backend should continue to use a layered design:

- controller layer for request handling and HTTP responses
- service layer for business rules
- repository layer for persistence
- security layer for authentication context and authorization decisions
- DTO layer for request and response contracts

### 5.4 Authorization Design Direction
Authorization should continue to use multiple dimensions:

- system role
- project membership
- organizational context where relevant

Project membership remains the clearest operational access boundary. Organizational unit context should enhance reporting and ownership visibility first, and can later become stricter where needed.

## 6. Roles and Access Design

### 6.1 System Roles
The design should support these logical roles:

- Administrator
- Portfolio Manager or PMO User
- Project Manager
- Team Member
- Unit Manager
- Auditor or Read-Only Reviewer

Some of these roles may initially map to the currently available Keycloak roles until distinct enterprise roles are configured.

### 6.2 Access Model
The access model should follow these principles:

- administrators can manage all organizational units, projects, and audit views
- project managers manage projects within their authorized scope
- team members work inside projects they belong to
- unit managers consume read-oriented summaries for unit-owned work when enabled
- portfolio users consume cross-project summary data when enabled
- auditors consume read-only data within granted scope

### 6.3 Placement of Technical Context
Authentication and backend support details are valid system concerns, but the design should place them away from business dashboards. These details should be exposed through profile or support-oriented endpoints and views, not as dominant dashboard content.

## 7. Data Design

### 7.1 Core Entities
The main data entities for the current product direction are:

- User
- UserPreference
- OrganizationalUnit
- Project
- ProjectMilestone
- ProjectMember
- Task
- Comment
- Notification
- ActivityLog

### 7.2 Suggested Entity Relationships
- One user may belong to one organizational unit in the current release.
- One user may have one preference record.
- One user can belong to many projects through ProjectMember.
- One organizational unit can own many projects.
- One project can have many members, milestones, and tasks.
- One task belongs to one project and has one assignee in the current release.
- One task can have many comments and many activity records.
- One user can have many notifications.

### 7.3 Suggested Database Structure

#### User
- id
- keycloak_user_id
- full_name
- email
- organizational_unit_id
- active
- created_at
- updated_at

#### UserPreference
- id
- user_id
- default_landing_page
- display_density
- notify_assignments
- notify_due_soon
- notify_overdue
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
- health
- created_at
- updated_at
- archived_at

#### ProjectMilestone
- id
- project_id
- title
- description
- due_date
- status
- owner_id
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

#### Notification
- id
- user_id
- type
- title
- message
- read
- created_at

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

### 7.4 Enumerations and Controlled Values
Suggested controlled values for the current phase include:

Project status:
- PLANNED
- ACTIVE
- ON_HOLD
- COMPLETED
- ARCHIVED

Project health:
- ON_TRACK
- AT_RISK
- OFF_TRACK

Task status:
- TO_DO
- IN_PROGRESS
- REVIEW
- DONE

Task priority:
- LOW
- MEDIUM
- HIGH

Organizational unit type:
- HEAD_OFFICE
- DEPARTMENT
- BRANCH
- DIVISION
- TEAM

## 8. API Design Approach

### 8.1 API Modules
The backend should organize API responsibilities around these modules:

- /api/me
- /api/me/profile
- /api/me/preferences
- /api/organizational-units
- /api/projects
- /api/projects/{projectId}/members
- /api/projects/{projectId}/milestones
- /api/projects/{projectId}/tasks
- /api/tasks/{taskId}/comments
- /api/tasks/{taskId}/activity
- /api/dashboard
- /api/notifications
- /api/audit

### 8.2 Example Endpoint Responsibilities
Suggested responsibilities include:

- current user and profile retrieval
- update of user preferences
- create, list, update, and deactivate organizational units
- create, list, update, and archive projects
- filter projects by status, health, and unit
- add and remove project members
- create and update milestones
- create, edit, assign, and move tasks
- list board tasks by project
- add and fetch comments
- fetch task activity history
- fetch dashboard and reporting summaries
- fetch, mark, or clear notifications
- fetch audit activity for authorized review

### 8.3 DTO Design Principles
DTOs should:

- validate input clearly
- avoid exposing persistence internals
- keep API contracts stable for frontend work
- include unit, status, and health information where needed for business views
- avoid exposing unnecessary security or token details

## 9. Dashboard and Reporting Design

### 9.1 Dashboard Data Model Direction
Dashboard data may be built from either composed entity lists or dedicated summary endpoints. As the product grows, dedicated summary endpoints will become preferable for performance and clarity.

Likely summary groupings include:

- portfolio overview metrics
- unit coverage summary
- project watchlist
- selected project summary
- personal assigned work summary

### 9.2 Reporting Scope for the Current Phase
The current reporting scope should remain practical:

- unit-level summary counts
- project-level progress and health summary
- personal assigned work summary
- due soon and overdue indicators
- audit visibility for key actions

Advanced analytics and predictive forecasting can remain future work.

## 10. UI and UX Design Direction

### 10.1 Layout Direction
The product should move toward a wider desktop workspace with restrained gutters rather than a thin central column. The purpose is to make room for dashboards, boards, lists, and summary panels without forcing excessive vertical stacking.

### 10.2 Information Hierarchy
The main information hierarchy should be:

- delivery and portfolio information first
- project execution and task detail second
- identity, preferences, and support context in profile or support areas

### 10.3 Enterprise Tone
The UI should feel like an internal COOP operating system for delivery work:

- clear navigation
- dense but readable information grouping
- consistent visual language across dashboard, projects, units, and tasks
- strong use of business terminology such as units, portfolio, delivery, ownership, milestones, and watchlist

## 11. Local Development and Deployment Design

### 11.1 Local Development Services
The local development environment should continue to support:

- Angular frontend
- Spring Boot backend
- PostgreSQL
- Keycloak

### 11.2 Environment Configuration
The application will continue to require environment-specific values such as:

- database URL, username, and password
- Keycloak server URL
- Keycloak realm and client identifiers
- backend API base URL
- frontend application URL

### 11.3 Container and Deployment Direction
The deployment model should continue to package the frontend and backend as separate deployable units. PostgreSQL and Keycloak may remain separate managed services depending on the environment.

Kubernetes remains a future deployment target once the Docker-based workflow is stable.

## 12. Testing Strategy

### 12.1 Backend Testing
The backend should include tests for:

- authorization-sensitive endpoints
- organizational unit management
- project lifecycle updates
- milestone creation and update
- project membership changes
- task creation, assignment, and status movement
- notification creation logic where applicable
- audit record generation for important actions

### 12.2 Frontend Testing
The frontend should include tests for:

- dashboard rendering and derived business metrics
- profile page behavior
- project and milestone forms
- task filters and board behavior
- notification center behavior where applicable

### 12.3 End-to-End Validation
Critical end-to-end flows should include:

- authentication
- dashboard load for an authenticated user
- project creation and unit assignment
- milestone creation
- member management
- task creation and status movement
- comment creation
- notification visibility
- audit activity creation

## 13. CI and Delivery Direction

GitHub Actions should continue to automate quality checks for frontend and backend changes. Over time, the CI pipeline should validate:

- frontend build and tests
- backend build and tests
- Docker image builds
- documentation consistency when important product changes occur

Later phases may publish tagged container images and support deployment automation.

## 14. Implementation Phases

A practical implementation sequence for the next product phase is:

1. align requirements and system design documents
2. widen the application shell and simplify the dashboard content model
3. introduce the dedicated profile page
4. add project lifecycle status and health controls
5. add milestone management
6. add notification center behavior and reminder flows
7. expand reporting and audit review polish

This phased approach keeps the project realistic while still moving it toward an enterprise-quality result.

## 15. Risks and Future Extensions

Potential implementation risks include:

- Keycloak setup complexity for role expansion
- keeping authorization rules consistent across backend and frontend
- balancing enterprise scope with internship time constraints
- avoiding dashboard clutter as features are added

Likely future extensions include:

- file attachments
- custom workflows
- external system integrations
- advanced analytics and forecasting
- formal approval workflows
- real-time collaboration
- broader reporting export capabilities

## 16. Conclusion

This design provides a practical architecture for the next phase of COOP WorkFlow. It keeps the existing technical foundation while shifting the product toward a more coherent enterprise experience: a wider workspace, a cleaner business dashboard, a dedicated profile area, stronger project structure through status and milestones, and a clearer path for notifications, reporting, and audit visibility.
