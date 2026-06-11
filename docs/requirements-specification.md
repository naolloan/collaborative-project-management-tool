# Requirements Specification

## Project Title
COOP WorkFlow - Collaborative Project Management Tool

## Document Version
Version 0.3

## 1. Introduction

### 1.1 Purpose
This document defines the requirements for COOP WorkFlow, a web-based collaborative project management platform inspired by Jira and Trello. The system helps teams break work into smaller manageable tasks, assign responsibilities, track progress through a workflow, and monitor project completion.

The document serves as the foundation for development, testing, deployment, and future enhancement of the system.

### 1.2 Project Overview
COOP WorkFlow is intended to support collaborative work in a structured way similar to modern project management platforms such as Jira and Trello. The system will allow users to create projects, define tasks, assign them to team members, update task status, and monitor progress using a visual workflow.

The system is being adapted for a Cooperative Bank of Oromia context. This means the generic project-management workflow shall be extended with bank-oriented concepts such as organizational units, departments, branches, team ownership, stronger audit visibility, and role-based access suitable for an internal enterprise environment.

The application will use an Angular frontend, a Spring Boot backend, a PostgreSQL database, and Keycloak for authentication and role management. Supporting services will run with Docker during local development. The frontend and backend will be maintained as separate codebases or repositories. GitHub will be used for version control, GitHub Actions for continuous integration, and the system will be prepared for deployment with Kubernetes support.

### 1.3 Objectives
The main objectives of the system are:

- Provide a centralized platform for managing project work.
- Allow teams to divide work into clear and manageable tasks.
- Support task assignment and responsibility tracking.
- Enable workflow-based task progression from creation to completion.
- Improve visibility into project status and team activity.
- Provide a maintainable architecture suitable for testing and deployment.
- Support bank-specific organization structure such as departments, branches, and teams.
- Improve accountability through auditable activity records for important project actions.

### 1.4 Scope
The first version of the system will focus on the core workflow of collaborative project management:

- User authentication and access control
- Project creation and management
- Task creation, assignment, and status tracking
- Board-based task visualization
- Basic comments and activity tracking
- Bank organizational unit management
- Assignment of users and projects to organizational units
- Bank-themed internal user interface
- Testing, containerization, and deployment readiness

Advanced features such as sprint management, advanced reporting, notifications, file attachments, third-party integrations, formal multi-step approval workflows, and compliance-grade audit reporting may be considered in later versions.

### 1.5 Definitions

- Project: a workspace that groups related tasks, members, and progress information
- Task: the smallest managed work item within a project
- Workflow: the ordered set of statuses a task passes through from creation to completion
- Role: the level of authority a user has in the system or within a project
- Project membership: the relationship between a user and a project that determines access
- Organizational unit: a bank structure such as a department, branch, division, or team that can own users and projects
- Audit log: a recorded history of important user actions used for accountability and traceability

## 2. Stakeholders

The main stakeholders of the system are:

- End users: team members who create, update, and complete tasks
- Project managers or team leads: users who organize projects and assign work
- System administrator: user responsible for overall platform administration
- Department or branch managers: bank-side leaders who need visibility into work owned by their organizational unit
- Bank operations or digital transformation teams: internal teams that may use the platform to coordinate delivery work
- Compliance or audit stakeholders: interested in traceability of important actions and access boundaries
- Development team: responsible for implementation, testing, deployment, and maintenance
- Academic supervisor or evaluator: interested in the completeness and quality of the project

## 3. Intended Users and Roles

The system will initially support the following user roles:

### 3.1 Administrator
- Manage users in the system
- View all projects
- Support platform-level configuration

### 3.2 Project Manager
- Create and manage projects
- Add team members to projects
- Create, assign, update, and monitor tasks
- Review project progress

### 3.3 Team Member
- View assigned projects and tasks
- Update task status based on work progress
- Add comments to tasks
- Collaborate with other project members

### 3.4 Unit Manager
- View projects owned by their department, branch, or team
- Monitor work progress for their organizational unit
- Coordinate with project managers on delivery status
- Review audit/activity information relevant to their unit

The Unit Manager role may initially be represented using project manager permissions and organizational unit ownership. It can later become a distinct Keycloak role if required.

### 3.5 Initial Permission Boundaries

For the MVP, the role permissions will be interpreted as follows:

- Administrator: manage users and roles in coordination with Keycloak, view all projects, and support system-level administration
- Project Manager: create projects, add or remove project members, create tasks, assign tasks, update project details, and monitor project progress
- Team Member: view projects they belong to, update tasks they are allowed to work on, and add comments to project tasks
- Unit Manager: view and monitor projects connected to their organizational unit when this role is enabled

The MVP will use both system roles and project membership to determine access. A user shall not access a project unless they are an administrator or a member of that project.

For the bank-specific version, organizational unit assignment shall be added as an additional access and reporting dimension. A project shall belong to an organizational unit, and users may be associated with an organizational unit to support department or branch-level filtering.

## 4. Assumptions and Constraints

### 4.1 Assumptions
- Users will access the system through a web browser.
- The application will have separate frontend and backend codebases or repositories.
- The initial release will support one bank organization context with multiple organizational units.
- Users will have valid credentials to access the platform.
- Keycloak will serve as the source of truth for authentication and role identity.
- The system will represent bank departments, branches, divisions, or teams as organizational units.
- Formal bank compliance approval and production security review are outside the educational MVP unless explicitly required later.

### 4.2 Constraints
- The project is educational and time-bounded.
- The system must be developed with clear understanding of each tool used.
- Docker will be used for containerization.
- Git and GitHub will be used for version control and collaboration.
- GitHub Actions will be used for continuous integration.
- Kubernetes will be considered for deployment orchestration.
- The application must be tested before deployment.
- Spring Boot will be used for backend API and business logic.
- Angular will be used for the frontend user interface.
- PostgreSQL will be used for persistent application data.
- Keycloak will be used for identity and access management, authentication, and role handling.
- Bank-specific customization shall be implemented through configurable organization data where possible rather than hard-coded assumptions.

## 5. Functional Requirements

### 5.1 User Management

#### FR-1 User Registration
The system shall allow administrator-controlled or authorized creation of user accounts.

#### FR-2 User Login
The system shall allow registered users to log in using valid credentials.

#### FR-3 User Logout
The system shall allow authenticated users to log out securely.

#### FR-4 Role Assignment
The system shall support assigning a role to each user.

#### FR-5 Access Control
The system shall restrict system actions based on user role and project membership.

#### FR-5.1 Synchronize Authenticated User Context
The system shall map the authenticated Keycloak user identity and roles to application-level access decisions.

#### FR-5.2 User Organizational Unit Assignment
The system shall allow an authenticated application user to be associated with an organizational unit such as a department, branch, division, or team.

#### FR-5.3 User Unit Visibility
The system shall expose a user's assigned organizational unit in the authenticated user context when available.

### 5.1A Bank Organizational Structure

#### FR-5A.1 Create Organizational Unit
The system shall allow an administrator to create an organizational unit with a name, type, optional description, and active status.

#### FR-5A.2 View Organizational Units
The system shall allow authorized users to view active organizational units for project ownership, filtering, and reporting.

#### FR-5A.3 Update Organizational Unit
The system shall allow an administrator to update organizational unit details.

#### FR-5A.4 Deactivate Organizational Unit
The system shall allow an administrator to deactivate an organizational unit without deleting historical project or task records.

#### FR-5A.5 Organizational Unit Types
The system shall support organizational unit types such as Head Office, Department, Branch, Division, and Team.

#### FR-5A.6 Unit-Based Filtering
The system shall allow projects and dashboards to be filtered by organizational unit.

### 5.2 Project Management

#### FR-6 Create Project
The system shall allow authorized users to create a project with a name, description, optional start and due dates, and an owning organizational unit.

#### FR-7 View Projects
The system shall allow users to view projects they are authorized to access.

#### FR-8 Update Project
The system shall allow authorized users to update project details.

#### FR-9 Archive or Delete Project
The system shall allow authorized users to archive or delete a project.

#### FR-10 Add Members to Project
The system shall allow a project manager or administrator to add team members to a project.

#### FR-10.1 Remove Members from Project
The system shall allow a project manager or administrator to remove a member from a project.

#### FR-10.2 Project Unit Ownership
The system shall associate each project with one owning organizational unit for bank-level visibility and reporting.

#### FR-10.3 Project Unit Update
The system shall allow authorized users to update the owning organizational unit of a project when appropriate.

### 5.3 Task Management

#### FR-11 Create Task
The system shall allow authorized users to create tasks within a project.

#### FR-12 Edit Task
The system shall allow authorized users to edit task details such as title, description, priority, due date, and assignee.

#### FR-13 Delete Task
The system shall allow authorized users to delete tasks when necessary.

#### FR-14 Assign Task
The system shall allow a task to be assigned to one user.

#### FR-15 View Task Details
The system shall allow users to open and view complete task information.

#### FR-16 Set Task Priority
The system shall support task priority levels such as low, medium, and high.

#### FR-17 Set Due Date
The system shall allow tasks to have due dates.

#### FR-17.1 Task Creator Tracking
The system shall record the creator of each task.

### 5.4 Workflow and Status Management

#### FR-18 Default Task Workflow
The system shall provide a default workflow with statuses such as To Do, In Progress, Review, and Done.

#### FR-19 Update Task Status
The system shall allow authorized users to move a task from one workflow status to another.

#### FR-20 Board View
The system shall display project tasks in a board view grouped by workflow status.

#### FR-21 Task Movement
The system shall support moving tasks across workflow columns through an intuitive interface.

#### FR-21.1 Fixed Workflow for MVP
The MVP shall use a fixed workflow of To Do, In Progress, Review, and Done for all projects.

#### FR-21.2 Valid Workflow Transitions
The system shall allow movement only between logically valid workflow stages for the MVP. At minimum, the system shall support To Do to In Progress, In Progress to Review, Review to Done, and movement back to a previous stage when rework is required.

### 5.5 Collaboration Features

#### FR-22 Comment on Task
The system shall allow project members to add comments to a task.

#### FR-23 View Task Activity
The system shall maintain and display a basic activity history for tasks, including creation, assignment, status changes, and comments.

#### FR-23.1 Audit Timestamps
The system shall record creation and last update timestamps for projects, tasks, and comments.

#### FR-23.2 Bank Audit Events
The system shall record important project-management actions for accountability, including project creation, project update, project archive, member addition or removal, task creation, task update, task status change, task assignment change, comment creation, and organizational unit changes.

#### FR-23.3 Audit Event Details
Audit records shall include the actor, action type, target resource, timestamp, and enough contextual information to understand what changed.

#### FR-23.4 Audit Visibility
Administrators shall be able to view audit information across the system. Project managers shall be able to view audit information for projects they manage. Unit-level audit visibility may be added for unit managers.

### 5.6 Search and Filtering

#### FR-24 Search Tasks
The system shall allow users to search for tasks within a project.

#### FR-25 Filter Tasks
The system shall allow users to filter tasks by status, assignee, and priority.

### 5.7 Dashboard and Progress Tracking

#### FR-26 Project Summary
The system shall provide a project summary showing total tasks, completed tasks, pending tasks, and tasks in progress.

#### FR-27 User Task View
The system shall allow a user to view tasks assigned specifically to them.

#### FR-27.1 Unit Dashboard
The system shall provide a unit-level dashboard or filter that shows projects and task summary information for a selected organizational unit.

### 5.8 Notifications and Reminders

#### FR-28 Basic In-App Alerts
The system may provide basic alerts for important actions such as assignment changes or approaching due dates.

This requirement is desirable but may be deferred if time is limited.

## 6. Non-Functional Requirements

### 6.1 Usability
- The interface should be simple and intuitive for first-time users.
- Users should be able to perform common task operations with minimal steps.
- The board and task views should present information clearly.

### 6.2 Performance
- The system should load main pages within an acceptable response time under normal usage.
- Task updates and status changes should appear without unnecessary delay.

### 6.3 Reliability
- The system should preserve data consistency for projects, tasks, assignments, and status updates.
- Basic error handling should be provided for invalid or failed operations.
- The system should preserve referential integrity between users, projects, memberships, tasks, comments, and activity records.

### 6.4 Security
- Authentication shall be required for protected resources.
- User credentials shall be handled through Keycloak rather than custom application-side password management.
- Authorization shall prevent unauthorized access to projects and tasks.
- Input validation shall be implemented on both frontend and backend where appropriate.
- Authentication and role management shall be integrated with Keycloak.
- Administrator-only actions shall be required for managing organizational units.
- Audit-related records shall not be editable by normal application users.
- Access control shall consider system role, project membership, and organizational unit context where applicable.

### 6.5 Maintainability
- Frontend and backend shall be separated into different codebases or clearly separated directories.
- The codebase should follow a clean and understandable structure.
- The system should support future extension with additional features.
- API contracts between frontend and backend should be documented and stable enough for independent development.

### 6.6 Testability
- The application shall include automated tests for critical features.
- Backend endpoints should be testable independently.
- Frontend behavior should be validated through appropriate testing approaches.

### 6.7 Portability and Deployment
- The application shall run in Docker containers.
- The application should be deployable in a container-based environment.
- The deployment process should be compatible with Kubernetes-based orchestration.

## 7. External Interface Requirements

### 7.1 User Interface
The system shall provide a web interface with, at minimum, the following screens:

- Login page
- Project list page
- Project details page
- Organizational unit list or management page
- Task board page
- Task details or edit form
- User-specific task view
- Project member management view
- Unit-level project or dashboard filter

### 7.2 Backend API
The system shall expose backend endpoints for:

- Authentication
- User management
- Organizational unit management
- Project management
- Task management
- Comments and activity retrieval
- Dashboard or summary data

The backend API shall be implemented using Spring Boot and secured using Keycloak-based authentication and authorization.

The backend shall organize API responsibilities into modules for authentication context handling, organizational unit management, project management, task management, comments, activity/audit records, and reporting or summary retrieval.

### 7.3 Database
The system shall use a persistent database to store:

- Users
- Organizational units
- Projects
- Project memberships
- Tasks
- Comments
- Activity logs

The persistent database technology for the system shall be PostgreSQL.

## 8. Core Entities

The main data entities expected in the system are:

- User
- OrganizationalUnit
- Project
- ProjectMember
- Task
- Comment
- ActivityLog

Possible important fields include:

- User: id, keycloakUserId, name, email, role, organizationalUnitId
- OrganizationalUnit: id, name, type, description, active, createdAt, updatedAt
- Project: id, name, description, createdBy, organizationalUnitId, startDate, dueDate
- ProjectMember: id, projectId, userId, projectRole, joinedAt
- Task: id, title, description, status, priority, assignee, dueDate, projectId, createdBy
- Comment: id, content, authorId, taskId, createdAt
- ActivityLog: id, actionType, actorId, taskId, oldValue, newValue, createdAt

## 9. Minimum Viable Product Boundary

The MVP should include:

- Authentication and login
- Role-aware access control
- Basic organizational unit management
- Assigning projects to organizational units
- Project creation and listing
- Adding members to projects
- Task creation and editing
- Task assignment
- Task board with status columns
- Status movement across workflow stages
- Basic comments
- Project member management
- Basic project summary
- Basic unit-level filtering or visibility
- Audit activity for key project and task actions
- Automated testing for critical paths
- Docker setup for local and deployment-ready execution
- Keycloak-based authentication and role-aware authorization

The following items are not required for the MVP but may be added later:

- Sprint planning
- Advanced reporting and analytics
- Email notifications
- File attachments
- Real-time updates
- Third-party integrations
- Custom workflows per project
- Multiple assignees per task
- Formal multi-step approval workflows
- Compliance-grade audit export and retention policies
- Integration with internal HR, core banking, or enterprise directory systems

## 10. Use Case Summary

### UC-1 User Logs In
A registered user enters credentials and accesses the system.

### UC-2 Project Manager Creates a Project
A project manager creates a new project and defines its details.

### UC-3 Project Manager Adds Members
A project manager adds team members to a project.

### UC-4 User Creates a Task
An authorized user creates a new task within a project.

### UC-5 Project Manager Assigns a Task
A task is assigned to a project member.

### UC-6 Team Member Updates Task Status
A team member moves a task from one workflow stage to another.

### UC-7 Team Member Adds a Comment
A team member comments on a task for collaboration or clarification.

### UC-8 User Tracks Project Progress
A user views the board and summary information to understand project progress.

### UC-9 Project Manager Manages Membership
A project manager adds or removes members to control who can access project work.

### UC-10 Administrator Manages Organizational Units
An administrator creates, updates, or deactivates organizational units representing bank departments, branches, divisions, or teams.

### UC-11 Project Manager Assigns Project to Unit
A project manager or administrator assigns a project to the organizational unit responsible for the work.

### UC-12 Manager Reviews Unit Work
A manager filters projects and summaries by organizational unit to understand delivery status for that part of the bank.

## 11. Acceptance Criteria for Initial Release

The initial release shall be considered acceptable when:

- Users can authenticate successfully.
- Authorized users can create and manage projects.
- Administrators can create and manage organizational units.
- Projects can be assigned to organizational units.
- Authorized users can create, assign, edit, and track tasks.
- Tasks can move through the defined workflow.
- Team members can view and comment on tasks.
- Project managers can manage project membership.
- Project progress can be viewed through a board and summary.
- Users can filter or identify projects by organizational unit.
- Important project and task actions are recorded in activity or audit history.
- The frontend and backend run as separate services.
- The system can run in Docker containers.
- Automated checks or tests run through GitHub Actions.
- The application is prepared for deployment.

## 12. Risks and Open Questions

The following decisions have been made for the initial release:

- User registration will be admin-controlled through the identity and access management process rather than open public sign-up.
- A task will support one assignee in the MVP to keep task ownership clear and implementation simpler.
- Projects will use a fixed workflow in the MVP: To Do, In Progress, Review, and Done.
- The bank organization model will support one owning organizational unit per project in the MVP.
- Organizational units will be managed inside the application database, while user authentication and global roles remain in Keycloak.
- Activity logging will provide practical audit visibility, but formal compliance-grade audit retention, export, and legal review are future concerns.
- The frontend will use Angular.
- The backend will use Spring Boot.
- The database system will be PostgreSQL.
- Authentication and role management will use Keycloak.
- Notification support will be deferred unless a simple in-app alert mechanism fits comfortably within the schedule.

## 13. Conclusion

This requirements specification defines the initial functional and non-functional expectations for the Collaborative Project Management Tool. It establishes the scope of the first version and provides a clear basis for the upcoming system design, implementation planning, testing strategy, and deployment preparation.
