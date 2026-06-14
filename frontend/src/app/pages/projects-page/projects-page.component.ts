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
}
