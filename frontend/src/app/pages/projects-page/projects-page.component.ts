import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrganizationalUnit } from '../../core/dto/organizational-unit';
import { CreateProjectRequest, Project, ProjectLifecycleStatus } from '../../core/dto/project';

type HealthTone = 'good' | 'warning' | 'danger' | 'neutral';

@Component({
  selector: 'app-projects-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './projects-page.component.html'
})
export class ProjectsPageComponent {
  @Input({ required: true }) projects: Project[] = [];
  @Input({ required: true }) organizationalUnits: OrganizationalUnit[] = [];
  @Input({ required: true }) selectedProjectId: number | undefined;
  @Input({ required: true }) selectedProject: Project | undefined;
  @Input({ required: true }) newProject!: CreateProjectRequest;
  @Input({ required: true }) editProject!: {
    name: string;
    description: string;
    teamIds: number[];
    startDate: string;
    dueDate: string;
    status: ProjectLifecycleStatus;
  };
  @Input({ required: true }) projectLifecycleStatuses: ProjectLifecycleStatus[] = [];
  @Input({ required: true }) creatingProject = false;
  @Input({ required: true }) updatingProject = false;
  @Input({ required: true }) archivingProject = false;

  @Output() createProject = new EventEmitter<void>();
  @Output() refreshProjects = new EventEmitter<void>();
  @Output() selectProject = new EventEmitter<Project>();
  @Output() updateProject = new EventEmitter<void>();
  @Output() archiveProject = new EventEmitter<void>();

  formatUnitType(type: string | null | undefined): string {
    if (!type) {
      return 'Unassigned';
    }

    return this.formatLabel(type);
  }

  formatProjectStatus(status: string | null | undefined): string {
    if (!status) {
      return 'Planned';
    }

    return this.formatLabel(status);
  }

  formatProjectHealth(health: string | null | undefined): string {
    if (!health) {
      return 'On Track';
    }

    return this.formatLabel(health);
  }

  projectHealthTone(project: Project | undefined): HealthTone {
    const normalized = (project?.health || '').trim().toUpperCase();
    switch (normalized) {
      case 'ON_TRACK':
        return 'good';
      case 'AT_RISK':
        return 'warning';
      case 'OFF_TRACK':
      case 'BLOCKED':
        return 'danger';
      default:
        return 'neutral';
    }
  }

  projectPhase(status: string | null | undefined): string {
    const normalizedStatus = (status || '').trim().toUpperCase();
    switch (normalizedStatus) {
      case 'PLANNED':
        return 'Planning';
      case 'ACTIVE':
        return 'In Delivery';
      case 'ON_HOLD':
        return 'On Hold';
      case 'COMPLETED':
        return 'Completed';
      case 'ARCHIVED':
        return 'Archived';
      default:
        return 'Planning';
    }
  }

  projectSchedule(project: Project): string {
    if (project.startDate && project.dueDate) {
      return `${project.startDate} - ${project.dueDate}`;
    }

    if (project.dueDate) {
      return `Due ${project.dueDate}`;
    }

    if (project.startDate) {
      return `Started ${project.startDate}`;
    }

    return 'Schedule not defined';
  }

  selectedProjectSummary(): string {
    if (!this.selectedProject) {
      return 'Select a project to review team alignment, timeline, and governance details.';
    }

    return this.selectedProject.description || 'No project summary has been added yet.';
  }

  selectedProjectSchedule(): string {
    if (!this.selectedProject) {
      return 'Schedule not defined';
    }

    return this.projectSchedule(this.selectedProject);
  }

  selectedProjectPhase(): string {
    if (!this.selectedProject) {
      return 'Planning';
    }

    return this.projectPhase(this.selectedProject.status);
  }

  ownedProjectCount(): number {
    return this.projects.filter((project) => (project.teams?.length ?? 0) > 0).length;
  }

  selectedProjectCountLabel(): string {
    return this.selectedProject ? '1' : '0';
  }

  activeDeliveryCount(): number {
    return this.projects.filter((project) => project.status === 'ACTIVE').length;
  }

  teamUnits(): OrganizationalUnit[] {
    return this.organizationalUnits.filter((unit) => unit.type === 'TEAM');
  }

  projectTeamSummary(project: Project | undefined): string {
    if (!project || !project.teams || project.teams.length === 0) {
      return 'No teams assigned';
    }

    return project.teams.map((team) => team.name).join(', ');
  }

  selectedProjectTeamCount(): number {
    return this.selectedProject?.teams?.length ?? 0;
  }

  private formatLabel(value: string): string {
    return value.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }
}
