import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Task, TaskStatus } from '../../core/dto/task';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-page.component.html'
})
export class DashboardPageComponent {
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

  @Output() selectTask = new EventEmitter<Task>();

  formatStatus(status: TaskStatus | string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }
}
