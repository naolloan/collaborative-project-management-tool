import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile-page.component.html'
})
export class ProfilePageComponent {
  @Input() authenticated = false;
  @Input() displayName: string | null | undefined;
  @Input() username: string | null | undefined;
  @Input() email: string | null | undefined;
  @Input() userId: number | null | undefined;
  @Input() systemRole: string | null | undefined;
  @Input() roles: string[] = [];
  @Input() primaryRoleLabel = '';
  @Input() assignedTaskCount = 0;
  @Input() assignedToDoTasks = 0;
  @Input() assignedInProgressTasks = 0;
  @Input() assignedReviewTasks = 0;
  @Input() assignedDoneTasks = 0;
  @Input() overdueAssignedTasks = 0;
  @Input() highPriorityAssignedTasks = 0;
  @Input() projectCount = 0;
  @Input() unitCount = 0;
  @Input() projectMemberCount = 0;
  @Input() selectedProjectName: string | null | undefined;
  @Input() selectedProjectDescription: string | null | undefined;
  @Input() selectedProjectUnitName = 'No active project context';
  @Input() selectedProjectTimeline = 'Timeline not yet defined';
  @Input() deliveryPressureLabel = 'Personal workload is stable';
  @Input() portfolioCoverageLabel = 'No portfolio coverage loaded';
  @Input() nextLandingSuggestion = 'Dashboard';
  @Input() backendStatus = '';
  @Input() sessionStatus = '';

  @Output() refreshProfile = new EventEmitter<void>();

  initials(value: string | null | undefined): string {
    if (!value) {
      return 'CW';
    }

    const parts = value.split(/\s+/).filter(Boolean).slice(0, 2);
    if (parts.length === 0) {
      return 'CW';
    }

    return parts.map((part) => part.charAt(0).toUpperCase()).join('');
  }

  formatRoleLabel(value: string | null | undefined): string {
    if (!value) {
      return 'Access pending';
    }

    return value.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  preferenceSummary(): string {
    return this.assignedTaskCount > 0
      ? 'Task-focused landing / Compact delivery rhythm'
      : 'Dashboard landing / Standard delivery rhythm';
  }
}
