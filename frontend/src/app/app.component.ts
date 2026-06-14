import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewEncapsulation, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { KeycloakProfile } from 'keycloak-js';
import { AuthService } from './auth/auth.service';
import { ApiService } from './core/api.service';
import { Activity } from './core/dto/activity';
import { Comment } from './core/dto/comment';
import { CurrentUser } from './core/dto/current-user';
import { OrganizationalUnit, OrganizationalUnitType } from './core/dto/organizational-unit';
import { ProjectMember, ProjectRole } from './core/dto/project-member';
import { CreateProjectRequest, Project } from './core/dto/project';
import { CreateTaskRequest, Task, TaskPriority, TaskStatus } from './core/dto/task';
import { DashboardPageComponent } from './pages/dashboard-page/dashboard-page.component';
import { ProjectsPageComponent } from './pages/projects-page/projects-page.component';
import { UnitsPageComponent } from './pages/units-page/units-page.component';
import { ProfilePageComponent } from './pages/profile-page/profile-page.component';

type WorkspaceView = 'dashboard' | 'projects' | 'units' | 'members' | 'tasks' | 'profile';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, DashboardPageComponent, ProjectsPageComponent, UnitsPageComponent, ProfilePageComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  encapsulation: ViewEncapsulation.None
})
export class AppComponent implements OnInit {
  projectName = 'COOP WorkFlow';
  authenticated = signal(false);
  initializing = signal(true);
  profile = signal<KeycloakProfile | undefined>(undefined);
  currentUser = signal<CurrentUser | undefined>(undefined);
  projects = signal<Project[]>([]);
  organizationalUnits = signal<OrganizationalUnit[]>([]);
  projectMembers = signal<ProjectMember[]>([]);
  selectedProjectId = signal<number | undefined>(undefined);
  selectedTaskId = signal<number | undefined>(undefined);
  tasks = signal<Task[]>([]);
  comments = signal<Comment[]>([]);
  activities = signal<Activity[]>([]);
  activeWorkspace = signal<WorkspaceView>('dashboard');
  apiStatus = signal('Not checked');
  projectStatus = signal('Not loaded');
  unitStatus = signal('Not loaded');
  memberStatus = signal('No project selected');
  taskStatus = signal('No project selected');
  commentStatus = signal('No task selected');
  activityStatus = signal('No task selected');
  creatingProject = signal(false);
  updatingProject = signal(false);
  archivingProject = signal(false);
  creatingUnit = signal(false);
  updatingUnit = signal(false);
  deactivatingUnit = signal(false);
  addingMember = signal(false);
  creatingTask = signal(false);
  updatingTask = signal(false);
  deletingTask = signal(false);
  addingComment = signal(false);
  error = signal<string | undefined>(undefined);
  readonly workflowColumns: { status: TaskStatus; title: string }[] = [
    { status: 'TO_DO', title: 'To Do' },
    { status: 'IN_PROGRESS', title: 'In Progress' },
    { status: 'REVIEW', title: 'Review' },
    { status: 'DONE', title: 'Done' }
  ];
  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
  readonly projectRoles: ProjectRole[] = ['MEMBER', 'MANAGER'];
  readonly organizationalUnitTypes: OrganizationalUnitType[] = ['HEAD_OFFICE', 'DEPARTMENT', 'BRANCH', 'DIVISION', 'TEAM'];
  readonly workspaceNavItems: { view: WorkspaceView; label: string; helper: string }[] = [
    { view: 'dashboard', label: 'Dashboard', helper: 'Overview' },
    { view: 'projects', label: 'Projects', helper: 'Planning' },
    { view: 'units', label: 'Units', helper: 'Bank structure' },
    { view: 'members', label: 'Members', helper: 'Access' },
    { view: 'tasks', label: 'Task Board', helper: 'Delivery' },
    { view: 'profile', label: 'Profile', helper: 'Account' }
  ];
  newProject: CreateProjectRequest = {
    name: '',
    description: '',
    organizationalUnitId: null,
    startDate: '',
    dueDate: ''
  };
  editProject = {
    name: '',
    description: '',
    organizationalUnitId: null as number | null,
    startDate: '',
    dueDate: ''
  };
  newUnit = {
    name: '',
    type: 'DEPARTMENT' as OrganizationalUnitType,
    description: ''
  };
  editUnit = {
    id: null as number | null,
    name: '',
    type: 'DEPARTMENT' as OrganizationalUnitType,
    description: ''
  };
  newTask: CreateTaskRequest = {
    title: '',
    description: '',
    priority: 'MEDIUM',
    assigneeId: null,
    dueDate: ''
  };
  newMember = {
    email: '',
    projectRole: 'MEMBER' as ProjectRole
  };
  taskFilters = {
    search: '',
    priority: '' as TaskPriority | '',
    assigneeId: null as number | null
  };
  editTask = {
    title: '',
    description: '',
    priority: 'MEDIUM' as TaskPriority,
    assigneeId: null as number | null,
    dueDate: ''
  };
  newComment = '';

  constructor(
    private readonly authService: AuthService,
    private readonly apiService: ApiService
  ) {
  }

  async ngOnInit(): Promise<void> {
    try {
      const authenticated = await this.authService.initialize();
      this.authenticated.set(authenticated);

      if (authenticated) {
        this.profile.set(await this.authService.loadProfile());
        this.loadCurrentUser();
        this.loadOrganizationalUnits();
        this.loadProjects();
      }
    } catch {
      this.error.set('Could not initialize Keycloak. Check that the collab-pm realm and frontend client exist.');
    } finally {
      this.initializing.set(false);
    }
  }

  login(): void {
    void this.authService.login();
  }

  logout(): void {
    void this.authService.logout();
  }

  setWorkspace(view: WorkspaceView): void {
    this.activeWorkspace.set(view);
  }

  loadCurrentUser(): void {
    this.apiStatus.set('Checking backend...');
    this.error.set(undefined);

    this.apiService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser.set(user);
        this.apiStatus.set('Backend authenticated');
      },
      error: () => {
        this.currentUser.set(undefined);
        this.apiStatus.set('Backend check failed');
        this.error.set('The frontend could not call /api/me. Confirm the backend is running on port 8080 and CORS is enabled.');
      }
    });
  }

  loadProjects(): void {
    if (!this.authenticated()) {
      return;
    }

    this.projectStatus.set('Loading projects...');
    this.error.set(undefined);

    this.apiService.listProjects().subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.projectStatus.set(`${projects.length} active project${projects.length === 1 ? '' : 's'}`);
        if (!this.selectedProjectId() && projects.length > 0) {
          this.selectProject(projects[0]);
        }
      },
      error: () => {
        this.projects.set([]);
        this.projectStatus.set('Project load failed');
        this.error.set('Could not load projects from /api/projects. Confirm the backend is running and you are logged in.');
      }
    });
  }

  loadOrganizationalUnits(): void {
    if (!this.authenticated()) {
      return;
    }

    this.unitStatus.set('Loading units...');
    this.apiService.listOrganizationalUnits().subscribe({
      next: (units) => {
        this.organizationalUnits.set(units);
        this.unitStatus.set(`${units.length} active unit${units.length === 1 ? '' : 's'}`);
        if (!this.newProject.organizationalUnitId && units.length > 0) {
          this.newProject.organizationalUnitId = units[0].id;
        }
      },
      error: () => {
        this.organizationalUnits.set([]);
        this.unitStatus.set('Unit load failed');
        this.error.set('Could not load organizational units.');
      }
    });
  }

  createOrganizationalUnit(): void {
    const name = this.newUnit.name.trim();
    if (!this.canManageOrganizationalUnits()) {
      this.error.set('Only administrators can manage organizational units.');
      return;
    }

    if (!name) {
      this.error.set('Organizational unit name is required.');
      return;
    }

    this.creatingUnit.set(true);
    this.error.set(undefined);

    this.apiService.createOrganizationalUnit({
      name,
      type: this.newUnit.type,
      description: this.cleanOptional(this.newUnit.description)
    }).subscribe({
      next: (unit) => {
        this.newUnit = {
          name: '',
          type: 'DEPARTMENT',
          description: ''
        };
        this.creatingUnit.set(false);
        this.editSelectedUnit(unit);
        this.loadOrganizationalUnits();
      },
      error: () => {
        this.creatingUnit.set(false);
        this.error.set('Could not create the organizational unit. Check for duplicate names.');
      }
    });
  }

  editSelectedUnit(unit: OrganizationalUnit): void {
    this.editUnit = {
      id: unit.id,
      name: unit.name,
      type: unit.type,
      description: unit.description ?? ''
    };
  }

  updateSelectedUnit(): void {
    const unitId = this.editUnit.id;
    const name = this.editUnit.name.trim();

    if (!this.canManageOrganizationalUnits()) {
      this.error.set('Only administrators can manage organizational units.');
      return;
    }

    if (!unitId) {
      this.error.set('Select an organizational unit before saving changes.');
      return;
    }

    if (!name) {
      this.error.set('Organizational unit name is required.');
      return;
    }

    this.updatingUnit.set(true);
    this.error.set(undefined);

    this.apiService.updateOrganizationalUnit(unitId, {
      name,
      type: this.editUnit.type,
      description: this.cleanOptional(this.editUnit.description)
    }).subscribe({
      next: (unit) => {
        this.updatingUnit.set(false);
        this.editSelectedUnit(unit);
        this.loadOrganizationalUnits();
        this.loadProjects();
      },
      error: () => {
        this.updatingUnit.set(false);
        this.error.set('Could not update the organizational unit.');
      }
    });
  }

  deactivateSelectedUnit(): void {
    const unitId = this.editUnit.id;

    if (!this.canManageOrganizationalUnits()) {
      this.error.set('Only administrators can manage organizational units.');
      return;
    }

    if (!unitId) {
      this.error.set('Select an organizational unit before deactivating it.');
      return;
    }

    this.deactivatingUnit.set(true);
    this.error.set(undefined);

    this.apiService.deactivateOrganizationalUnit(unitId).subscribe({
      next: () => {
        this.deactivatingUnit.set(false);
        this.editUnit = {
          id: null,
          name: '',
          type: 'DEPARTMENT',
          description: ''
        };
        this.loadOrganizationalUnits();
        this.loadProjects();
      },
      error: () => {
        this.deactivatingUnit.set(false);
        this.error.set('Could not deactivate the organizational unit.');
      }
    });
  }

  createProject(): void {
    const name = this.newProject.name.trim();
    if (!name) {
      this.error.set('Project name is required.');
      return;
    }

    this.creatingProject.set(true);
    this.error.set(undefined);

    this.apiService.createProject({
      name,
      description: this.cleanOptional(this.newProject.description),
      organizationalUnitId: this.newProject.organizationalUnitId || null,
      startDate: this.cleanOptional(this.newProject.startDate),
      dueDate: this.cleanOptional(this.newProject.dueDate)
    }).subscribe({
      next: () => {
        this.newProject = {
          name: '',
          description: '',
          organizationalUnitId: this.organizationalUnits()[0]?.id ?? null,
          startDate: '',
          dueDate: ''
        };
        this.creatingProject.set(false);
        this.loadProjects();
      },
      error: () => {
        this.creatingProject.set(false);
        this.error.set('Could not create the project. Check the dates and try again.');
      }
    });
  }

  selectProject(project: Project): void {
    this.selectedProjectId.set(project.id);
    this.prepareProjectEdit(project);
    this.selectedTaskId.set(undefined);
    this.projectMembers.set([]);
    this.comments.set([]);
    this.activities.set([]);
    this.newTask.assigneeId = null;
    this.memberStatus.set('Loading members...');
    this.commentStatus.set('No task selected');
    this.activityStatus.set('No task selected');
    this.loadProjectMembers(project.id);
    this.loadTasks(project.id);
  }

  updateSelectedProject(): void {
    const projectId = this.selectedProjectId();
    const name = this.editProject.name.trim();

    if (!projectId) {
      this.error.set('Select a project before saving project changes.');
      return;
    }

    if (!name) {
      this.error.set('Project name is required.');
      return;
    }

    this.updatingProject.set(true);
    this.error.set(undefined);

    this.apiService.updateProject(projectId, {
      name,
      description: this.cleanOptional(this.editProject.description),
      organizationalUnitId: this.editProject.organizationalUnitId || null,
      startDate: this.cleanOptional(this.editProject.startDate),
      dueDate: this.cleanOptional(this.editProject.dueDate)
    }).subscribe({
      next: (project) => {
        this.updatingProject.set(false);
        this.prepareProjectEdit(project);
        this.loadProjects();
      },
      error: () => {
        this.updatingProject.set(false);
        this.error.set('Could not update the project. Check the dates and try again.');
      }
    });
  }

  archiveSelectedProject(): void {
    const projectId = this.selectedProjectId();

    if (!projectId) {
      this.error.set('Select a project before archiving it.');
      return;
    }

    this.archivingProject.set(true);
    this.error.set(undefined);

    this.apiService.archiveProject(projectId).subscribe({
      next: () => {
        this.archivingProject.set(false);
        this.selectedProjectId.set(undefined);
        this.selectedTaskId.set(undefined);
        this.projectMembers.set([]);
        this.tasks.set([]);
        this.comments.set([]);
        this.activities.set([]);
        this.memberStatus.set('No project selected');
        this.taskStatus.set('No project selected');
        this.commentStatus.set('No task selected');
        this.activityStatus.set('No task selected');
        this.loadProjects();
      },
      error: () => {
        this.archivingProject.set(false);
        this.error.set('Could not archive the project. Try again.');
      }
    });
  }

  loadProjectMembers(projectId: number): void {
    this.apiService.listProjectMembers(projectId).subscribe({
      next: (members) => {
        this.projectMembers.set(members);
        this.memberStatus.set(`${members.length} member${members.length === 1 ? '' : 's'}`);
        if (!this.newTask.assigneeId && members.length > 0) {
          this.newTask.assigneeId = members[0].userId;
        }
      },
      error: () => {
        this.projectMembers.set([]);
        this.memberStatus.set('Member load failed');
        this.error.set('Could not load project members.');
      }
    });
  }

  addProjectMember(): void {
    const projectId = this.selectedProjectId();
    const email = this.newMember.email.trim();

    if (!this.canManageProjectMembers()) {
      this.error.set('Only project managers or administrators can add members.');
      return;
    }

    if (!projectId) {
      this.error.set('Select a project before adding members.');
      return;
    }

    if (!email) {
      this.error.set('Member email is required.');
      return;
    }

    this.addingMember.set(true);
    this.error.set(undefined);

    this.apiService.addProjectMember(projectId, {
      email,
      projectRole: this.newMember.projectRole
    }).subscribe({
      next: () => {
        this.newMember = {
          email: '',
          projectRole: 'MEMBER'
        };
        this.addingMember.set(false);
        this.loadProjectMembers(projectId);
      },
      error: () => {
        this.addingMember.set(false);
        this.error.set('Could not add member. The user must log in once before they can be added by email.');
      }
    });
  }

  loadSelectedProjectTasks(): void {
    const projectId = this.selectedProjectId();
    if (!projectId) {
      return;
    }

    this.loadTasks(projectId);
  }

  loadTasks(projectId: number): void {
    this.taskStatus.set('Loading tasks...');
    this.error.set(undefined);

    this.apiService.listProjectTasks(projectId).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.taskStatus.set(`${tasks.length} task${tasks.length === 1 ? '' : 's'}`);
        if (this.selectedTaskId() && !tasks.some((task) => task.id === this.selectedTaskId())) {
          this.selectedTaskId.set(undefined);
          this.comments.set([]);
          this.activities.set([]);
          this.commentStatus.set('No task selected');
          this.activityStatus.set('No task selected');
        }
      },
      error: () => {
        this.tasks.set([]);
        this.taskStatus.set('Task load failed');
        this.error.set('Could not load tasks for the selected project.');
      }
    });
  }

  createTask(): void {
    const projectId = this.selectedProjectId();
    const title = this.newTask.title.trim();

    if (!projectId) {
      this.error.set('Select a project before creating a task.');
      return;
    }

    if (!title) {
      this.error.set('Task title is required.');
      return;
    }

    this.creatingTask.set(true);
    this.error.set(undefined);

    this.apiService.createTask(projectId, {
      title,
      description: this.cleanOptional(this.newTask.description),
      priority: this.newTask.priority,
      assigneeId: this.newTask.assigneeId || null,
      dueDate: this.cleanOptional(this.newTask.dueDate)
    }).subscribe({
      next: (createdTask) => {
        this.newTask = {
          title: '',
          description: '',
          priority: 'MEDIUM',
          assigneeId: this.projectMembers()[0]?.userId ?? null,
          dueDate: ''
        };
        this.creatingTask.set(false);
        this.selectedTaskId.set(createdTask.id);
        this.prepareTaskEdit(createdTask);
        this.comments.set([]);
        this.commentStatus.set('No comments yet');
        this.loadTasks(projectId);
        this.loadActivities(createdTask.id);
      },
      error: () => {
        this.creatingTask.set(false);
        this.error.set('Could not create the task. Check the form and try again.');
      }
    });
  }

  selectTask(task: Task): void {
    this.selectedTaskId.set(task.id);
    this.prepareTaskEdit(task);
    this.loadComments(task.id);
    this.loadActivities(task.id);
  }

  updateSelectedTask(): void {
    const taskId = this.selectedTaskId();
    const projectId = this.selectedProjectId();
    const title = this.editTask.title.trim();

    if (!taskId || !projectId) {
      this.error.set('Select a task before saving changes.');
      return;
    }

    if (!title) {
      this.error.set('Task title is required.');
      return;
    }

    this.updatingTask.set(true);
    this.error.set(undefined);

    this.apiService.updateTask(taskId, {
      title,
      description: this.cleanOptional(this.editTask.description),
      priority: this.editTask.priority,
      assigneeId: this.editTask.assigneeId,
      dueDate: this.cleanOptional(this.editTask.dueDate)
    }).subscribe({
      next: (updatedTask) => {
        this.updatingTask.set(false);
        this.prepareTaskEdit(updatedTask);
        this.loadTasks(projectId);
        this.loadActivities(taskId);
      },
      error: () => {
        this.updatingTask.set(false);
        this.error.set('Could not update the task. Check the form and try again.');
      }
    });
  }

  deleteSelectedTask(): void {
    const taskId = this.selectedTaskId();
    const projectId = this.selectedProjectId();

    if (!taskId || !projectId) {
      this.error.set('Select a task before deleting it.');
      return;
    }

    this.deletingTask.set(true);
    this.error.set(undefined);

    this.apiService.deleteTask(taskId).subscribe({
      next: () => {
        this.deletingTask.set(false);
        this.selectedTaskId.set(undefined);
        this.comments.set([]);
        this.activities.set([]);
        this.commentStatus.set('No task selected');
        this.activityStatus.set('No task selected');
        this.loadTasks(projectId);
      },
      error: () => {
        this.deletingTask.set(false);
        this.error.set('Could not delete the task. Try again.');
      }
    });
  }

  loadComments(taskId: number): void {
    this.commentStatus.set('Loading comments...');
    this.error.set(undefined);

    this.apiService.listTaskComments(taskId).subscribe({
      next: (comments) => {
        this.comments.set(comments);
        this.commentStatus.set(`${comments.length} comment${comments.length === 1 ? '' : 's'}`);
      },
      error: () => {
        this.comments.set([]);
        this.commentStatus.set('Comment load failed');
        this.error.set('Could not load comments for the selected task.');
      }
    });
  }

  addComment(): void {
    const taskId = this.selectedTaskId();
    const content = this.newComment.trim();

    if (!taskId) {
      this.error.set('Select a task before adding a comment.');
      return;
    }

    if (!content) {
      this.error.set('Comment content is required.');
      return;
    }

    this.addingComment.set(true);
    this.error.set(undefined);

    this.apiService.createComment(taskId, { content }).subscribe({
      next: () => {
        this.newComment = '';
        this.addingComment.set(false);
        this.loadComments(taskId);
        this.loadActivities(taskId);
      },
      error: () => {
        this.addingComment.set(false);
        this.error.set('Could not add the comment. Try again.');
      }
    });
  }

  loadActivities(taskId: number): void {
    this.activityStatus.set('Loading activity...');
    this.error.set(undefined);

    this.apiService.listTaskActivities(taskId).subscribe({
      next: (activities) => {
        this.activities.set(activities);
        this.activityStatus.set(`${activities.length} activit${activities.length === 1 ? 'y' : 'ies'}`);
      },
      error: () => {
        this.activities.set([]);
        this.activityStatus.set('Activity load failed');
        this.error.set('Could not load activity history for the selected task.');
      }
    });
  }

  tasksByStatus(status: TaskStatus): Task[] {
    return this.filteredTasks().filter((task) => task.status === status);
  }

  filteredTasks(): Task[] {
    const search = this.taskFilters.search.trim().toLowerCase();
    const priority = this.taskFilters.priority;
    const assigneeId = this.taskFilters.assigneeId;

    return this.tasks().filter((task) => {
      const matchesSearch = !search
        || task.title.toLowerCase().includes(search)
        || (task.description ?? '').toLowerCase().includes(search);
      const matchesPriority = !priority || task.priority === priority;
      const matchesAssignee = assigneeId === null || task.assigneeId === assigneeId;

      return matchesSearch && matchesPriority && matchesAssignee;
    });
  }

  hasTaskFilters(): boolean {
    return Boolean(this.taskFilters.search.trim() || this.taskFilters.priority || this.taskFilters.assigneeId !== null);
  }

  clearTaskFilters(): void {
    this.taskFilters = {
      search: '',
      priority: '',
      assigneeId: null
    };
  }

  taskCountByStatus(status: TaskStatus): number {
    return this.tasks().filter((task) => task.status === status).length;
  }

  completedTaskCount(): number {
    return this.taskCountByStatus('DONE');
  }

  completionPercentage(): number {
    const totalTasks = this.tasks().length;
    if (totalTasks === 0) {
      return 0;
    }

    return Math.round((this.completedTaskCount() / totalTasks) * 100);
  }

  highPriorityTaskCount(): number {
    return this.tasks().filter((task) => task.priority === 'HIGH').length;
  }

  overdueTaskCount(): number {
    return this.tasks().filter((task) => this.isTaskOverdue(task)).length;
  }

  isTaskOverdue(task: Task): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (!task.dueDate || task.status === 'DONE') {
      return false;
    }

    const dueDate = new Date(`${task.dueDate}T00:00:00`);
    return dueDate < today;
  }

  myAssignedTasks(): Task[] {
    const currentUserId = this.currentUser()?.id;
    if (!currentUserId) {
      return [];
    }

    return this.tasks().filter((task) => task.assigneeId === currentUserId);
  }

  myAssignedTaskCountByStatus(status: TaskStatus): number {
    return this.myAssignedTasks().filter((task) => task.status === status).length;
  }

  moveTaskForward(task: Task): void {
    const nextStatus = this.nextStatus(task.status);
    if (!nextStatus) {
      return;
    }

    this.apiService.updateTaskStatus(task.id, nextStatus).subscribe({
      next: () => {
        this.loadSelectedProjectTasks();
        if (task.id === this.selectedTaskId()) {
          this.selectedTaskId.set(task.id);
          this.loadActivities(task.id);
        }
      },
      error: () => this.error.set('Could not update the task status.')
    });
  }

  selectedTask(): Task | undefined {
    return this.tasks().find((task) => task.id === this.selectedTaskId());
  }

  selectedProject(): Project | undefined {
    return this.projects().find((project) => project.id === this.selectedProjectId());
  }

  canManageProjectMembers(): boolean {
    if (this.roles().includes('ADMINISTRATOR')) {
      return true;
    }

    const currentUserEmail = this.currentUser()?.email?.toLowerCase();
    if (!currentUserEmail) {
      return false;
    }

    return this.projectMembers().some((member) =>
      member.email.toLowerCase() === currentUserEmail && member.projectRole === 'MANAGER');
  }

  canManageOrganizationalUnits(): boolean {
    return this.roles().includes('ADMINISTRATOR');
  }

  formatStatus(status: TaskStatus | string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  formatUnitType(type: string | null | undefined): string {
    if (!type) {
      return 'Unassigned';
    }

    return this.formatStatus(type);
  }

  firstNameLastName(): string | undefined {
    const profile = this.profile();
    return [profile?.firstName, profile?.lastName].filter(Boolean).join(' ') || undefined;
  }

  roles(): string[] {
    return this.currentUser()?.roles ?? [];
  }

  primaryRoleLabel(): string {
    const roles = this.roles();
    if (roles.length === 0) {
      return 'Access Pending';
    }

    return roles.map((role) => this.formatStatus(role)).join(' / ');
  }

  currentOperatorLabel(): string {
    return this.currentUser()?.fullName || this.firstNameLastName() || this.profile()?.username || 'Current User';
  }

  portfolioDueSoonProjectCount(): number {
    return this.projects().filter((project) => {
      const remainingDays = this.daysUntil(project.dueDate);
      return !this.isProjectClosed(project) && remainingDays !== null && remainingDays >= 0 && remainingDays <= 14;
    }).length;
  }

  myOverdueAssignedTaskCount(): number {
    return this.myAssignedTasks().filter((task) => this.isTaskOverdue(task)).length;
  }

  myHighPriorityAssignedTaskCount(): number {
    return this.myAssignedTasks().filter((task) => task.priority === 'HIGH').length;
  }

  selectedProjectUnitLabel(): string {
    return this.selectedProject()?.organizationalUnitName || 'No owning unit assigned';
  }

  selectedProjectTimelineLabel(): string {
    const project = this.selectedProject();
    if (!project) {
      return 'Select a project from the workspace to load current focus';
    }

    if (project.startDate && project.dueDate) {
      return this.formatDate(project.startDate) + ' - ' + this.formatDate(project.dueDate);
    }

    if (project.dueDate) {
      return 'Due ' + this.formatDate(project.dueDate);
    }

    if (project.startDate) {
      return 'Started ' + this.formatDate(project.startDate);
    }

    return 'Timeline not yet defined';
  }

  sessionStatusLabel(): string {
    if (this.initializing()) {
      return 'Initializing authentication';
    }

    return this.authenticated() ? 'Authenticated via Keycloak' : 'Awaiting sign-in';
  }

  private cleanOptional(value: string | null | undefined): string | null {
    if (!value || !value.trim()) {
      return null;
    }

    return value.trim();
  }

  private prepareTaskEdit(task: Task): void {
    this.editTask = {
      title: task.title,
      description: task.description ?? '',
      priority: task.priority,
      assigneeId: task.assigneeId ?? null,
      dueDate: task.dueDate ?? ''
    };
  }

  private prepareProjectEdit(project: Project): void {
    this.editProject = {
      name: project.name,
      description: project.description ?? '',
      organizationalUnitId: project.organizationalUnitId ?? null,
      startDate: project.startDate ?? '',
      dueDate: project.dueDate ?? ''
    };
  }

  private daysUntil(dateValue: string | null | undefined): number | null {
    const dueDate = this.parseDate(dateValue);
    if (!dueDate) {
      return null;
    }

    const today = new Date();
    const currentDay = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const targetDay = new Date(dueDate.getFullYear(), dueDate.getMonth(), dueDate.getDate());
    return Math.round((targetDay.getTime() - currentDay.getTime()) / 86400000);
  }

  private parseDate(dateValue: string | null | undefined): Date | null {
    if (!dateValue) {
      return null;
    }

    const dateMatch = /^(\d{4})-(\d{2})-(\d{2})$/.exec(dateValue);
    if (dateMatch) {
      const [, year, month, day] = dateMatch;
      return new Date(Number(year), Number(month) - 1, Number(day));
    }

    const parsed = new Date(dateValue);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  private formatDate(dateValue: string | null | undefined): string {
    const parsed = this.parseDate(dateValue);
    if (!parsed) {
      return 'Date not set';
    }

    return parsed.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  private isProjectClosed(project: Project): boolean {
    const normalizedStatus = (project.status || '').trim().toUpperCase();
    return ['DONE', 'COMPLETED', 'CLOSED', 'ARCHIVED', 'DELIVERED'].includes(normalizedStatus);
  }

  private nextStatus(status: TaskStatus): TaskStatus | undefined {
    switch (status) {
      case 'TO_DO':
        return 'IN_PROGRESS';
      case 'IN_PROGRESS':
        return 'REVIEW';
      case 'REVIEW':
        return 'DONE';
      case 'DONE':
        return undefined;
    }
  }
}
