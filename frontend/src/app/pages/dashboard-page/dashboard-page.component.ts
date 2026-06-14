import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { OrganizationalUnit } from '../../core/dto/organizational-unit';
import { Project } from '../../core/dto/project';
import { Task, TaskStatus } from '../../core/dto/task';

type HealthTone = 'good' | 'warning' | 'danger' | 'neutral';

interface PortfolioWatchItem {
  project: Project;
  ownerLabel: string;
  timeline: string;
  healthLabel: string;
  healthTone: HealthTone;
}

interface UnitCoverageItem {
  unit: OrganizationalUnit;
  projectCount: number;
  coveragePercent: number;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-page.component.html'
})
export class DashboardPageComponent implements OnChanges {
  @Input() authenticated = false;
  @Input() username: string | null | undefined;
  @Input() email: string | null | undefined;
  @Input() fullName: string | null | undefined;
  @Input() userId: number | null | undefined;
  @Input() roles: string[] = [];
  @Input() selectedProjectName: string | null | undefined;
  @Input() selectedProjectId: number | undefined;
  @Input() selectedTaskId: number | undefined;
  @Input() totalTasks = 0;
  @Input() toDoTasks = 0;
  @Input() inProgressTasks = 0;
  @Input() reviewTasks = 0;
  @Input() completedTasks = 0;
  @Input() highPriorityTasks = 0;
  @Input() overdueTasks = 0;
  @Input() completionPercentage = 0;
  @Input() assignedTasks: Task[] = [];
  @Input() assignedToDoTasks = 0;
  @Input() assignedInProgressTasks = 0;
  @Input() assignedReviewTasks = 0;
  @Input() assignedDoneTasks = 0;
  @Input() projectCount = 0;
  @Input() unitCount = 0;
  @Input() memberCount = 0;
  @Input() activeRolesLabel = '';
  @Input() projects: Project[] = [];
  @Input() organizationalUnits: OrganizationalUnit[] = [];

  @Output() selectTask = new EventEmitter<Task>();

  alignedProjectCount = 0;
  unassignedProjectCount = 0;
  dueSoonProjectCount = 0;
  attentionProjectCount = 0;
  portfolioAlignmentRate = 0;
  portfolioWatchlist: PortfolioWatchItem[] = [];
  unitCoverage: UnitCoverageItem[] = [];

  ngOnChanges(): void {
    this.rebuildPortfolioView();
  }

  formatStatus(status: TaskStatus | string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  formatUnitType(type: string | undefined | null): string {
    return type ? this.formatStatus(type) : 'Unassigned';
  }

  private rebuildPortfolioView(): void {
    const totalProjects = this.projects.length;

    this.alignedProjectCount = this.projects.filter((project) => this.hasOwningUnit(project)).length;
    this.unassignedProjectCount = totalProjects - this.alignedProjectCount;
    this.dueSoonProjectCount = this.projects.filter((project) => this.isDueSoon(project)).length;
    this.attentionProjectCount = this.projects.filter((project) => this.needsAttention(project)).length;
    this.portfolioAlignmentRate = totalProjects === 0
      ? 0
      : Math.round((this.alignedProjectCount / totalProjects) * 100);

    this.unitCoverage = this.organizationalUnits
      .map((unit) => {
        const projectCount = this.projects.filter((project) => project.organizationalUnitId === unit.id).length;
        return {
          unit,
          projectCount,
          coveragePercent: totalProjects === 0 ? 0 : Math.round((projectCount / totalProjects) * 100)
        };
      })
      .filter((summary) => summary.projectCount > 0)
      .sort((left, right) => right.projectCount - left.projectCount || left.unit.name.localeCompare(right.unit.name));

    const rankedProjects = this.projects
      .map((project) => ({
        project,
        score: this.projectUrgencyScore(project),
        dueRank: this.projectDueRank(project)
      }))
      .sort((left, right) => {
        if (right.score !== left.score) {
          return right.score - left.score;
        }

        if (left.dueRank !== right.dueRank) {
          return left.dueRank - right.dueRank;
        }

        return left.project.name.localeCompare(right.project.name);
      });

    const urgentProjects = rankedProjects.filter((entry) => entry.score > 0).slice(0, 4);
    const fallbackProjects = rankedProjects.filter((entry) => !this.isClosedProject(entry.project)).slice(0, 4);

    this.portfolioWatchlist = (urgentProjects.length > 0 ? urgentProjects : fallbackProjects).map(({ project }) => ({
      project,
      ownerLabel: this.projectOwnerLabel(project),
      timeline: this.projectTimeline(project),
      healthLabel: this.projectHealthLabel(project),
      healthTone: this.projectHealthTone(project)
    }));
  }

  private hasOwningUnit(project: Project): boolean {
    return Boolean(project.organizationalUnitId || project.organizationalUnitName);
  }

  private projectOwnerLabel(project: Project): string {
    if (project.organizationalUnitName) {
      return project.organizationalUnitName;
    }

    if (project.organizationalUnitId) {
      const unit = this.organizationalUnits.find((candidate) => candidate.id === project.organizationalUnitId);
      if (unit) {
        return unit.name;
      }
    }

    return 'No owning unit assigned';
  }

  private projectTimeline(project: Project): string {
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

  private projectHealthLabel(project: Project): string {
    if (this.isClosedProject(project)) {
      return 'Closed out';
    }

    if (!this.hasOwningUnit(project)) {
      return 'Needs owner alignment';
    }

    const remainingDays = this.daysUntil(project.dueDate);
    if (remainingDays !== null && remainingDays < 0) {
      const overdueDays = Math.abs(remainingDays);
      return 'Past due by ' + overdueDays + ' day' + (overdueDays === 1 ? '' : 's');
    }

    if (remainingDays !== null && remainingDays <= 14) {
      return 'Due in ' + remainingDays + ' day' + (remainingDays === 1 ? '' : 's');
    }

    if (!project.dueDate) {
      return 'Schedule not set';
    }

    return 'On track';
  }

  private projectHealthTone(project: Project): HealthTone {
    if (this.isClosedProject(project)) {
      return 'good';
    }

    if (!this.hasOwningUnit(project) || this.isOverdue(project)) {
      return 'danger';
    }

    if (this.isDueSoon(project)) {
      return 'warning';
    }

    if (!project.dueDate) {
      return 'neutral';
    }

    return 'good';
  }

  private projectUrgencyScore(project: Project): number {
    if (this.isClosedProject(project)) {
      return 0;
    }

    let score = 0;

    if (!this.hasOwningUnit(project)) {
      score += 3;
    }

    const remainingDays = this.daysUntil(project.dueDate);
    if (remainingDays !== null && remainingDays < 0) {
      score += 4;
    } else if (remainingDays !== null && remainingDays <= 7) {
      score += 3;
    } else if (remainingDays !== null && remainingDays <= 14) {
      score += 2;
    }

    return score;
  }

  private projectDueRank(project: Project): number {
    const dueDate = this.parseDate(project.dueDate);
    return dueDate ? dueDate.getTime() : Number.MAX_SAFE_INTEGER;
  }

  private needsAttention(project: Project): boolean {
    return !this.isClosedProject(project) && (!this.hasOwningUnit(project) || this.isOverdue(project) || this.isDueSoon(project));
  }

  private isDueSoon(project: Project): boolean {
    const remainingDays = this.daysUntil(project.dueDate);
    return !this.isClosedProject(project) && remainingDays !== null && remainingDays >= 0 && remainingDays <= 14;
  }

  private isOverdue(project: Project): boolean {
    const remainingDays = this.daysUntil(project.dueDate);
    return !this.isClosedProject(project) && remainingDays !== null && remainingDays < 0;
  }

  private isClosedProject(project: Project): boolean {
    const normalizedStatus = (project.status || '').trim().toUpperCase();
    return ['DONE', 'COMPLETED', 'CLOSED', 'ARCHIVED', 'DELIVERED'].includes(normalizedStatus);
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
}
