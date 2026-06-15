# Requirements Specification

## Project Title
COOP WorkFlow - Collaborative Project Management Tool

## Document Version
Version 0.4

## 1. Introduction

### 1.1 Purpose
This document defines the product and system requirements for COOP WorkFlow, an internal collaborative project and delivery management platform for the Cooperative Bank of Oromia. The platform is intended to support portfolio oversight, project execution, organizational ownership, team collaboration, and auditable delivery tracking.

The document serves as the baseline for implementation, testing, deployment, and future enhancement of the system.

### 1.2 Product Vision
COOP WorkFlow should feel like a serious internal enterprise platform rather than a classroom prototype. It should help portfolio leaders, project managers, unit leaders, and team members work from the same system of record for delivery planning and execution.

The product vision combines five ideas:

- portfolio visibility for leaders
- project, sprint, and task execution for delivery teams
- COOP organizational ownership through units such as departments, branches, divisions, and teams
- personal work management for each authenticated user
- auditable collaboration for accountability and traceability

The main dashboard should present delivery information, project health, deadlines, ownership, and work progress. Technical or support-oriented details such as backend status, authentication mechanics, or raw role diagnostics should not dominate the business dashboard. User identity, role context, preferences, and support-facing information should instead be available in a dedicated profile area.

### 1.3 Objectives
The main objectives of the system are:

- provide a centralized internal platform for managing project work across COOP
- allow teams to break work into manageable sprints, tasks, and milestones
- map projects to collaborating delivery teams within COOP organizational structure
- give managers visibility into delivery health, workload, and timeline pressure
- support personal work tracking for each user
- improve accountability through activity history and audit records
- present information in a polished enterprise user experience suitable for repeated daily use
- keep the architecture maintainable and extensible for future enterprise features

### 1.4 Scope and Release Direction
The target scope for the next product phase includes:

- a business-focused dashboard
- a full-width or wider enterprise workspace shell
- a dedicated user profile page
- organizational unit management
- project lifecycle management
- automatic project health and milestone tracking
- sprint planning and sprint tracking
- project member management
- task board, task details, comments, and activity history
- basic in-app notifications and reminders
- unit-level and portfolio-level reporting views
- auditable records for important actions

The following capabilities are important but remain outside the near-term implementation target unless time and complexity allow:

- file attachments and document storage
- custom workflows per project
- formal approval chains and sign-off workflows
- real-time collaboration updates
- advanced analytics and forecasting
- integration with HR, enterprise directory, or banking systems
- mobile application support

### 1.5 Definitions

- Portfolio dashboard: a summary view that shows project health, ownership, workload, and delivery alerts across multiple projects
- Project: a workspace that groups related teams, sprints, tasks, members, milestones, and progress information
- Sprint: a time-boxed delivery cycle inside a project that groups related tasks toward a short-term goal
- Milestone: a planned checkpoint or deliverable within a project timeline
- Task: the smallest managed work item within a project
- Workflow: the ordered set of statuses that a task passes through from creation to completion
- Organizational unit: a COOP structure such as a head office group, department, branch, division, or team
- Project health: a business-facing indicator such as On Track, At Risk, or Off Track, computed from delivery conditions rather than manually selected
- Profile page: a user-focused area that shows identity, unit association, preferences, and personal context
- Notification center: an in-app area for assignment alerts, reminders, and relevant updates
- Audit log: an append-only record of important actions for accountability and traceability

## 2. Stakeholders

The main stakeholders of the system are:

- team members who create, update, and complete work
- project managers who plan and coordinate delivery
- unit managers who monitor work owned by their department, branch, division, or team
- portfolio managers or PMO staff who monitor delivery across multiple projects
- system administrators who manage platform access and configuration
- audit or compliance reviewers who need visibility into important actions
- IT support or implementation staff responsible for deployment and troubleshooting
- the development team responsible for implementation, testing, and maintenance
- the academic supervisor or evaluator interested in the completeness and quality of the project

## 3. Intended Users and Roles

The system will initially support the following user roles and usage patterns.

### 3.1 Administrator
- manage organization data and platform-level configuration
- view all projects and audit information
- support user and role administration in coordination with Keycloak

### 3.2 Portfolio Manager or PMO User
- view portfolio-wide project summaries
- review project health, team alignment gaps, and upcoming deadlines
- access unit and portfolio reporting views

### 3.3 Project Manager
- create and manage projects
- assign collaborating teams and project status
- manage project milestones, sprints, and membership
- create, assign, update, and monitor sprint-scoped tasks

### 3.4 Team Member
- view authorized projects and tasks
- update task status
- comment on tasks and collaborate with project members
- review personally assigned work and reminders

### 3.5 Unit Manager
- view projects involving their organizational unit or team
- monitor progress, deadlines, sprint delivery, and health indicators for that unit
- review summary and audit information relevant to team-aligned work

### 3.6 Auditor or Read-Only Reviewer
- view audit and activity information within allowed scope
- review delivery history without editing operational data

### 3.7 Initial Permission Boundaries
For the near-term product scope, permissions will be interpreted as follows:

- administrators can manage all units, projects, and audit visibility
- project managers can manage projects they are authorized to manage
- team members can view and work within projects they belong to
- unit managers can view unit-owned summaries and project information when granted that visibility
- portfolio managers can view cross-project summaries and reports when granted that visibility
- audit or read-only users can review data without changing it

Some of these roles may initially be represented by existing administrator or project manager permissions until distinct Keycloak roles are configured.

## 4. Assumptions and Constraints

### 4.1 Assumptions
- users will access the system through a web browser
- the application will serve one enterprise organization context for COOP in the initial release
- Keycloak will remain the source of truth for authentication and global role identity
- the application will use organizational units to model COOP structure
- users will need both business-facing views and personal context views
- a polished internal UX is important because the system is intended for repeated daily use

### 4.2 Constraints
- the project is educational and time-bounded
- the system must remain implementable incrementally
- Angular will be used for the frontend
- Spring Boot will be used for backend API and business logic
- PostgreSQL will be used for persistent application data
- Keycloak will be used for authentication and role management
- Docker and GitHub Actions will remain part of the delivery workflow
- enterprise-facing information should not be mixed carelessly with technical diagnostics on the main dashboard

## 5. Functional Requirements

### 5.1 Authentication, Identity, and Profile

#### FR-1 Authenticated Access
The system shall require authentication before granting access to protected application features.

#### FR-2 User Login and Logout
The system shall allow authenticated users to sign in and sign out securely through Keycloak.

#### FR-3 Role Synchronization
The system shall map authenticated user identity and roles from Keycloak into application-level access decisions.

#### FR-4 Access Control
The system shall restrict actions based on system role, project membership, and organizational context where applicable.

#### FR-5 Profile Page
The system shall provide a dedicated profile page where the user can view identity details, organizational unit, role context, and personal workspace information.

#### FR-6 User Preferences
The system shall allow users to manage a limited set of application preferences such as landing view, display density, and notification preferences.

#### FR-7 Personal Context Summary
The system shall provide a personal context summary that can include assigned work, recent activity, and reminders relevant to the current user.

#### FR-8 Technical Context Placement
The system shall keep technical session or support-oriented information outside the main business dashboard and surface it only in the profile page or support-oriented admin areas when needed.

### 5.2 Organizational Structure Management

#### FR-9 Create Organizational Unit
The system shall allow an administrator to create an organizational unit with a name, type, optional description, and active status.

#### FR-10 View Organizational Units
The system shall allow authorized users to view active organizational units for project ownership, filtering, and reporting.

#### FR-11 Update Organizational Unit
The system shall allow an administrator to update organizational unit details.

#### FR-12 Deactivate Organizational Unit
The system shall allow an administrator to deactivate an organizational unit without deleting historical project or task records.

#### FR-13 Organizational Unit Types
The system shall support organizational unit types such as Head Office, Department, Branch, Division, and Team.

#### FR-14 User Organizational Unit Assignment
The system shall allow an application user to be associated with one organizational unit in the initial release.

#### FR-15 Unit-Based Filtering
The system shall allow projects, summaries, and dashboards to be filtered or grouped by organizational unit.

### 5.3 Dashboard and Portfolio Visibility

#### FR-16 Business-Focused Dashboard
The system shall provide a business-focused dashboard that emphasizes delivery information rather than technical integration details.

#### FR-17 Portfolio KPIs
The dashboard shall present high-level portfolio metrics such as number of projects, unit alignment, due soon items, overdue work, and completion trends.

#### FR-18 Unit Ownership Distribution
The dashboard shall show how active projects are distributed across organizational units.

#### FR-19 Project Watchlist
The dashboard shall surface a watchlist of projects that need attention because of missing ownership, due dates, overdue status, or health concerns.

#### FR-20 Personal Work Summary
The dashboard or personal workspace shall show the current user's assigned tasks and immediate work focus.

#### FR-21 Delivery Alerts
The system shall highlight due soon and overdue project or task items in business-facing views.

#### FR-22 Dashboard Content Boundary
The main dashboard shall not display low-level backend, authentication, or support-diagnostic details as primary business content.

#### FR-23 Portfolio and Unit Reporting Views
The system shall provide portfolio-level and unit-level summary views or sections suitable for managerial review.

### 5.4 Project Management

#### FR-24 Create Project
The system shall allow authorized users to create a project with a name, description, optional start and due dates, one or more collaborating teams, and a project status.

#### FR-25 View and Update Project
The system shall allow authorized users to view and update project details.

#### FR-26 Archive Project
The system shall allow authorized users to archive a project without deleting its historical activity.

#### FR-27 Project Lifecycle Status
The system shall support project lifecycle statuses such as Planned, Active, On Hold, Completed, and Archived.

#### FR-28 Project Health Indicator
The system shall support a business-facing project health label such as On Track, At Risk, or Off Track.

#### FR-29 Project Team Alignment
The system shall associate each project with one or more collaborating teams of type Team.

#### FR-30 Automatic Project Health
The system shall compute project health automatically from project dates, sprint state, overdue work, and open high-priority tasks rather than allowing manual editing.

#### FR-31 Project Member Management
The system shall allow a project manager or administrator to add and remove project members and assign their project role.

#### FR-32 Project Milestones
The system shall allow authorized users to create, update, and track milestones within a project.

#### FR-33 Sprint Management
The system shall allow authorized users to create, update, and review sprints within a project, including sprint goal, dates, and sprint status.

#### FR-34 Sprint-Aware Task Planning
The system shall allow tasks to be assigned to a sprint or kept in backlog when they are not yet planned into an active sprint.

#### FR-35 Project Timeline Summary
The system shall present start date, due date, and milestone-related timeline information in project views.

#### FR-36 Project Search and Filtering
The system shall allow users to search and filter projects by name, status, health, and collaborating team.

#### FR-37 Project Summary Metrics
The system shall provide a project summary showing sprint progress, task counts, completion progress, milestone status, and key timeline information.

### 5.5 Task Management and Collaboration

#### FR-38 Create Task
The system shall allow authorized users to create tasks within a project.

#### FR-39 Edit and Delete Task
The system shall allow authorized users to update or delete task details when permitted.

#### FR-40 Assign Task
The system shall allow a task to be assigned to one user in the initial release.

#### FR-41 Task Priority and Due Date
The system shall support task priority levels and optional due dates.

#### FR-42 Task Workflow
The system shall provide a fixed workflow with statuses such as To Do, In Progress, Review, and Done.

#### FR-43 Board View
The system shall display project tasks in a board view grouped by workflow status and support sprint-specific filtering.

#### FR-44 Task Search and Filters
The system shall allow users to search or filter tasks by text, assignee, priority, status, and sprint.

#### FR-45 Task Comments
The system shall allow project members to add comments to a task.

#### FR-46 Task Activity History
The system shall maintain and display activity history for tasks, including creation, assignment, status changes, and comments.

#### FR-47 Personal Task View
The system shall allow a user to view work assigned specifically to them.

### 5.6 Notifications, Audit, and Reporting

#### FR-48 In-App Notification Center
The system shall provide an in-app notification center for relevant user alerts.

#### FR-49 Reminder Notifications
The system shall provide basic reminders for assignment changes, due soon items, and overdue work.

#### FR-50 Audit Event Recording
The system shall record important project-management actions for accountability, including project updates, membership changes, milestone changes, task updates, and organizational unit changes.

#### FR-51 Audit Event Details
Audit records shall include actor, action type, target resource, timestamp, and enough context to understand what changed.

#### FR-52 Audit Visibility
Authorized users shall be able to review audit information within their permitted scope.

#### FR-53 Summary Export Direction
The system should support future export of summary data for managerial reporting. This is desirable but may be deferred if time is limited.

## 6. Non-Functional Requirements

### 6.1 Usability and User Experience
- The interface should be clear, consistent, and suitable for repeated daily use.
- The primary workspace should use a wider or full-width layout on larger screens instead of a thin centered layout.
- Business dashboards should prioritize delivery information over technical diagnostics.
- The profile page should be the main place for identity, preferences, and personal context.
- Common project and task operations should require minimal friction.

### 6.2 Performance
- Main pages should load within an acceptable response time under normal internal usage.
- Dashboard summaries and task updates should appear without unnecessary delay.

### 6.3 Reliability
- The system should preserve consistency for projects, milestones, tasks, assignments, comments, notifications, and audit records.
- Failed operations should provide clear feedback.
- Historical records should remain intact when projects or units are archived or deactivated.

### 6.4 Security
- Authentication shall be required for protected resources.
- User credentials shall be handled through Keycloak rather than custom password management.
- Authorization shall prevent unauthorized access to projects, tasks, units, and audit views.
- Input validation shall be implemented on both frontend and backend where appropriate.
- Technical or support-facing details should not be exposed broadly in business-facing screens.

### 6.5 Auditability
- Important business actions shall be recorded with timestamped audit history.
- Audit-related records shall be append-only from normal user workflows.
- The system shall preserve enough context in audit records for later review.

### 6.6 Maintainability
- Frontend and backend shall remain clearly separated.
- The codebase should follow a modular and understandable structure.
- The requirements and design documents should be detailed enough to guide incremental implementation.

### 6.7 Accessibility
- The UI should support keyboard navigation, readable contrast, and understandable labeling.
- Dense information views should remain readable on desktop and smaller screens.

### 6.8 Testability
- Automated tests should exist for critical backend and frontend behavior.
- Permission-sensitive features should be testable independently.
- Dashboard, profile, and project workflows should be verifiable through repeatable checks.

### 6.9 Portability and Deployment
- The application shall run in Docker-based environments.
- The application should remain deployable to Kubernetes-based environments later.

## 7. External Interface Requirements

### 7.1 User Interface
The system shall provide a web interface with, at minimum, the following major screens or workspaces:

- business dashboard
- project workspace
- organizational unit management page
- project member management view
- task board and task detail area
- personal profile page
- personal or assigned work view
- notification center or equivalent panel
- audit or activity review view within the permitted scope

### 7.2 Backend API
The system shall expose backend endpoints or modules for:

- authentication context and current user information
- profile and user preference retrieval or update
- organizational unit management
- project management
- project membership management
- milestone management
- task management
- comments and activity retrieval
- dashboard and reporting summaries
- notifications and audit retrieval

### 7.3 Database
The system shall use a persistent database to store:

- users
- user preferences
- organizational units
- projects
- project milestones
- project memberships
- tasks
- comments
- notifications
- activity or audit logs

The persistent database technology for the system shall be PostgreSQL.

## 8. Core Entities

The main data entities expected in the system are:

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

Possible important fields include:

- User: id, keycloakUserId, fullName, email, organizationalUnitId, active
- UserPreference: userId, defaultLandingPage, displayDensity, notifyAssignments, notifyDueSoon, notifyOverdue
- OrganizationalUnit: id, name, type, description, active, createdAt, updatedAt
- Project: id, name, description, createdBy, startDate, dueDate, status, health, createdAt, updatedAt
- ProjectTeam: projectId, teamId
- Sprint: id, projectId, name, goal, startDate, endDate, status, createdAt, updatedAt
- ProjectMilestone: id, projectId, title, description, dueDate, status, ownerId
- ProjectMember: id, projectId, userId, projectRole, joinedAt
- Task: id, title, description, status, priority, assigneeId, dueDate, sprintId, projectId, createdBy
- Comment: id, taskId, authorId, content, createdAt, updatedAt
- Notification: id, userId, type, title, message, read, createdAt
- ActivityLog: id, actionType, actorId, targetType, targetId, oldValue, newValue, createdAt

## 9. Target Initial Release Boundary

The target initial release for the next phase should include:

- authentication and role-aware access control
- wider enterprise workspace layout
- business-focused dashboard
- dedicated profile page
- organizational unit management
- project creation, editing, archiving, and team assignment
- project member management
- automatic project health tracking
- sprint planning and sprint-aware task management
- project milestones
- task board with search and filters
- task comments and activity history
- personal assigned work summary
- basic in-app notifications
- audit visibility for important actions
- Docker-based local setup and automated checks

The following items are not required for this phase but remain future extensions:

- file attachments
- custom workflows per project
- external integrations
- real-time collaboration updates
- advanced forecasting and analytics
- formal approval chains
- mobile app support

## 10. Use Case Summary

### UC-1 User Signs In
A registered user authenticates through Keycloak and accesses the application.

### UC-2 User Reviews Business Dashboard
An authenticated user reviews portfolio, project, unit, and personal work information from the main dashboard.

### UC-3 User Reviews Personal Profile
A user opens the profile page to view identity, unit association, preferences, and personal context.

### UC-4 Administrator Manages Organizational Units
An administrator creates, updates, or deactivates organizational units.

### UC-5 Project Manager Creates and Updates a Project
A project manager creates a project, assigns collaborating teams, plans sprints, and maintains project details.

### UC-6 Project Manager Tracks Milestones
A project manager creates milestones and reviews their timeline status.

### UC-7 Project Manager Manages Membership
A project manager adds or removes project members to control project access.

### UC-8 User Creates and Updates a Task
An authorized user creates a task and updates it as work progresses.

### UC-9 Team Member Updates Task Status
A team member moves a task through the workflow.

### UC-10 Team Member Adds a Comment
A team member comments on a task for collaboration or clarification.

### UC-11 User Reviews Assigned Work
A user reviews tasks assigned specifically to them.

### UC-12 Manager Reviews Unit or Portfolio Summary
A manager reviews cross-project or unit-level delivery information.

### UC-13 User Reviews Notifications
A user checks reminders or assignment alerts from the notification center.

## 11. Acceptance Criteria for the Next Release

The next release shall be considered acceptable when:

- users can authenticate successfully
- the main dashboard focuses on business delivery information rather than technical diagnostics
- a dedicated profile page is available for user identity and personal context
- administrators can manage organizational units
- authorized users can create, update, and archive projects
- projects can be assigned to collaborating teams and tracked by status and computed health
- project managers can create and maintain sprints under projects
- project milestones can be created and reviewed
- project managers can manage project membership
- authorized users can create, assign, filter, and track tasks
- tasks can move through the defined workflow
- team members can comment on tasks and review activity history
- users can view personal assigned work and basic reminders
- important project and task actions are recorded in audit history
- the UI uses a wider enterprise layout on larger screens
- the frontend and backend run as separate services
- the system can run in Docker-based local development

## 12. Risks and Open Questions

The following decisions currently shape the implementation direction:

- Keycloak will remain the identity source rather than building custom authentication.
- Tasks will support one assignee in the initial release.
- Projects will support one or more collaborating teams in the current release.
- Project health will be computed automatically rather than edited manually.
- Sprints will sit between projects and tasks as the primary short-cycle planning layer.
- The task workflow will remain fixed initially: To Do, In Progress, Review, and Done.
- The business dashboard should remain free of low-level technical status details.
- Identity, role, preference, and support-oriented information should move to a profile or support-facing area.
- Milestone tracking is prioritized ahead of more advanced features such as attachments or approvals.
- Wider full-screen desktop layout is preferred, but the final implementation should still preserve readability and visual quality.
