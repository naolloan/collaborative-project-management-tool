import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Activity } from './dto/activity';
import { Comment, CreateCommentRequest } from './dto/comment';
import { CurrentUser } from './dto/current-user';
import { OrganizationalUnit, OrganizationalUnitRequest } from './dto/organizational-unit';
import { AddProjectMemberRequest, ProjectMember } from './dto/project-member';
import { CreateProjectRequest, Project, UpdateProjectRequest } from './dto/project';
import { CreateSprintRequest, Sprint, UpdateSprintRequest } from './dto/sprint';
import { CreateTaskRequest, Task, TaskStatus, UpdateTaskRequest } from './dto/task';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiBaseUrl = this.resolveApiBaseUrl();

  constructor(private readonly http: HttpClient) {
  }

  getCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${this.apiBaseUrl}/me`);
  }

  listProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiBaseUrl}/projects`);
  }

  listOrganizationalUnits(): Observable<OrganizationalUnit[]> {
    return this.http.get<OrganizationalUnit[]>(`${this.apiBaseUrl}/organizational-units`);
  }

  createOrganizationalUnit(request: OrganizationalUnitRequest): Observable<OrganizationalUnit> {
    return this.http.post<OrganizationalUnit>(`${this.apiBaseUrl}/organizational-units`, request);
  }

  updateOrganizationalUnit(unitId: number, request: OrganizationalUnitRequest): Observable<OrganizationalUnit> {
    return this.http.patch<OrganizationalUnit>(`${this.apiBaseUrl}/organizational-units/${unitId}`, request);
  }

  deactivateOrganizationalUnit(unitId: number): Observable<OrganizationalUnit> {
    return this.http.patch<OrganizationalUnit>(`${this.apiBaseUrl}/organizational-units/${unitId}/deactivate`, {});
  }

  createProject(request: CreateProjectRequest): Observable<Project> {
    return this.http.post<Project>(`${this.apiBaseUrl}/projects`, request);
  }

  updateProject(projectId: number, request: UpdateProjectRequest): Observable<Project> {
    return this.http.patch<Project>(`${this.apiBaseUrl}/projects/${projectId}`, request);
  }

  archiveProject(projectId: number): Observable<Project> {
    return this.http.patch<Project>(`${this.apiBaseUrl}/projects/${projectId}/archive`, {});
  }

  listProjectMembers(projectId: number): Observable<ProjectMember[]> {
    return this.http.get<ProjectMember[]>(`${this.apiBaseUrl}/projects/${projectId}/members`);
  }

  addProjectMember(projectId: number, request: AddProjectMemberRequest): Observable<ProjectMember> {
    return this.http.post<ProjectMember>(`${this.apiBaseUrl}/projects/${projectId}/members`, request);
  }

  listProjectTasks(projectId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiBaseUrl}/projects/${projectId}/tasks`);
  }

  listProjectSprints(projectId: number): Observable<Sprint[]> {
    return this.http.get<Sprint[]>(`${this.apiBaseUrl}/projects/${projectId}/sprints`);
  }

  createSprint(projectId: number, request: CreateSprintRequest): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.apiBaseUrl}/projects/${projectId}/sprints`, request);
  }

  updateSprint(sprintId: number, request: UpdateSprintRequest): Observable<Sprint> {
    return this.http.patch<Sprint>(`${this.apiBaseUrl}/sprints/${sprintId}`, request);
  }

  createTask(projectId: number, request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(`${this.apiBaseUrl}/projects/${projectId}/tasks`, request);
  }

  updateTaskStatus(taskId: number, status: TaskStatus): Observable<Task> {
    return this.http.patch<Task>(`${this.apiBaseUrl}/tasks/${taskId}/status`, { status });
  }

  updateTask(taskId: number, request: UpdateTaskRequest): Observable<Task> {
    return this.http.patch<Task>(`${this.apiBaseUrl}/tasks/${taskId}`, request);
  }

  deleteTask(taskId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/tasks/${taskId}`);
  }

  listTaskComments(taskId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiBaseUrl}/tasks/${taskId}/comments`);
  }

  createComment(taskId: number, request: CreateCommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiBaseUrl}/tasks/${taskId}/comments`, request);
  }

  listTaskActivities(taskId: number): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.apiBaseUrl}/tasks/${taskId}/activities`);
  }

  private resolveApiBaseUrl(): string {
    const developmentPorts = ['4200', '43297'];
    if (developmentPorts.includes(window.location.port)) {
      return 'http://localhost:8080/api';
    }

    return '/api';
  }
}
