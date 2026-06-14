import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrganizationalUnit } from '../../core/dto/organizational-unit';
import { CreateProjectRequest, Project } from '../../core/dto/project';

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
    organizationalUnitId: number | null;
    startDate: string;
    dueDate: string;
  };
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

    return type.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  projectPhase(project: Project): string {
    if (project.dueDate) {
      const dueDate = new Date(`${project.dueDate}T00:00:00`);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const diffDays = Math.ceil((dueDate.getTime() - today.getTime()) / 86400000);

      if (diffDays < 0) {
        return 'Needs Attention';
      }

      if (diffDays <= 7) {
        return 'Due Soon';
      }
    }

    if (project.startDate) {
      return 'In Delivery';
    }

    return 'Planning';
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
      return 'Select a project to review ownership, timeline, and governance details.';
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

    return this.projectPhase(this.selectedProject);
  }

  ownedProjectCount(): number {
    return this.projects.filter((project) => Boolean(project.organizationalUnitName)).length;
  }

  selectedProjectCountLabel(): string {
    return this.selectedProject ? '1' : '0';
  }
}


