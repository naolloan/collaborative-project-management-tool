import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrganizationalUnit } from '../../core/dto/organizational-unit';
import { Project } from '../../core/dto/project';
import { Sprint } from '../../core/dto/sprint';
import { Task, TaskStatus } from '../../core/dto/task';

type HealthTone = 'good' | 'warning' | 'danger' | 'neutral';
type ChartTone = 'good' | 'warning' | 'danger' | 'neutral';

interface PortfolioWatchItem {
  project: Project;
  teamLabel: string;
  timeline: string;
  healthLabel: string;
  healthTone: HealthTone;
}

interface UnitCoverageItem {
  unit: OrganizationalUnit;
  projectCount: number;
  coveragePercent: number;
}

interface SprintTrendItem {
  label: string;
  value: number;
  priority: string;
  status: string;
  completedTaskCount: number;
  totalTaskCount: number;
}

interface ChartLegendItem {
  label: string;
  value: number;
  percent: number;
  tone: ChartTone;
}

interface SelectedStatusMetric {
  label: string;
  value: string;
  tone: ChartTone;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard-page.component.html'
})
export class DashboardPageComponent implements OnChanges {
  @Input() authenticated = false;
  @Input() selectedProjectName: string | null | undefined;
  @Input() selectedProjectId: number | undefined;
  @Input() selectedSprintId: number | undefined;
  @Input() selectedTaskId: number | undefined;
  @Input() totalTasks = 0;
  @Input() toDoTasks = 0;
  @Input() inProgressTasks = 0;
  @Input() reviewTasks = 0;
  @Input() completedTasks = 0;
  @Input() highPriorityTasks = 0;
  @Input() overdueTasks = 0;
  @Input() completionPercentage = 0;
  @Input() tasks: Task[] = [];
  @Input() assignedTasks: Task[] = [];
  @Input() assignedToDoTasks = 0;
  @Input() assignedInProgressTasks = 0;
  @Input() assignedReviewTasks = 0;
  @Input() assignedDoneTasks = 0;
  @Input() projectCount = 0;
  @Input() unitCount = 0;
  @Input() memberCount = 0;
  @Input() projects: Project[] = [];
  @Input() organizationalUnits: OrganizationalUnit[] = [];
  @Input() sprints: Sprint[] = [];
  @Input() projectProgressPercentage = 0;

  @Output() selectTask = new EventEmitter<Task>();

  alignedProjectCount = 0;
  unassignedProjectCount = 0;
  dueSoonProjectCount = 0;
  attentionProjectCount = 0;
  portfolioAlignmentRate = 0;
  portfolioWatchlist: PortfolioWatchItem[] = [];
  unitCoverage: UnitCoverageItem[] = [];
  selectedProjectChartId: number | 'ALL' = 'ALL';
  selectedSprintChartId: number | 'ALL' = 'ALL';
  selectedTaskChartId: number | 'ALL' = 'ALL';
  private lastFocusProjectId: number | undefined;
  private lastFocusSprintId: number | undefined;
  private lastFocusTaskId: number | undefined;

  ngOnChanges(): void {
    this.rebuildPortfolioView();
    this.syncDashboardSelections();
  }

  formatStatus(status: TaskStatus | string): string {
    return status.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
  }

  formatUnitType(type: string | undefined | null): string {
    return type ? this.formatStatus(type) : 'Unassigned';
  }

  selectedProject(): Project | undefined {
    return this.projects.find((project) => project.id === this.selectedProjectId);
  }

  selectedProjectOwnerLabel(): string {
    const project = this.selectedProject();
    return project ? this.projectOwnerLabel(project) : 'No teams assigned';
  }

  selectedProjectTimeline(): string {
    const project = this.selectedProject();
    return project ? this.projectTimeline(project) : 'Timeline not yet defined';
  }

  selectedProjectHealthLabel(): string {
    const project = this.selectedProject();
    return project ? this.projectHealthLabel(project) : 'Select a project to load delivery context';
  }

  selectedSprint(): Sprint | undefined {
    if (this.sprints.length === 0) {
      return undefined;
    }

    return this.sprints.find((sprint) => sprint.status === 'ACTIVE') ?? this.sprints[0];
  }

  selectedTask(): Task | undefined {
    return this.tasks.find((task) => task.id === this.selectedTaskId);
  }

  selectedProjectStatusMetrics(): SelectedStatusMetric[] {
    const project = this.selectedProjectChart();
    if (!project) {
      return [];
    }

    return [
      { label: 'Health', value: this.projectHealthLabel(project), tone: this.projectHealthTone(project) },
      { label: 'Teams', value: String(project.teams?.length ?? 0), tone: project.teams?.length ? 'good' : 'warning' },
      { label: 'Timeline', value: this.projectTimeline(project), tone: 'neutral' }
    ];
  }

  selectedSprintStatusMetrics(): SelectedStatusMetric[] {
    const sprint = this.selectedSprintChart();
    if (!sprint) {
      return [];
    }

    return [
      { label: 'Priority', value: this.formatStatus(sprint.priority), tone: this.sprintPriorityTone(sprint.priority) },
      {
        label: 'Completion',
        value: `${sprint.totalTaskCount === 0 ? 0 : Math.round((sprint.completedTaskCount / sprint.totalTaskCount) * 100)}%`,
        tone: sprint.completedTaskCount === sprint.totalTaskCount && sprint.totalTaskCount > 0 ? 'good' : 'warning'
      },
      {
        label: 'Tasks',
        value: `${sprint.completedTaskCount}/${sprint.totalTaskCount}`,
        tone: sprint.totalTaskCount > 0 ? 'good' : 'neutral'
      }
    ];
  }

  selectedTaskStatusMetrics(): SelectedStatusMetric[] {
    const task = this.selectedTaskChart();
    if (!task) {
      return [];
    }

    return [
      { label: 'Priority', value: this.formatStatus(task.priority), tone: this.taskPriorityTone(task.priority) },
      { label: 'Assignee', value: task.assigneeName || 'Unassigned', tone: task.assigneeName ? 'good' : 'warning' }
    ];
  }

  sprintProgressTrend(): SprintTrendItem[] {
    if (this.sprints.length === 0) {
      return [];
    }

    return this.sprints.map((sprint, index) => ({
      label: sprint.name || `Sprint ${index + 1}`,
      value: sprint.totalTaskCount === 0 ? 0 : Math.round((sprint.completedTaskCount / sprint.totalTaskCount) * 100),
      priority: this.formatStatus(sprint.priority),
      status: this.formatStatus(sprint.status),
      completedTaskCount: sprint.completedTaskCount,
      totalTaskCount: sprint.totalTaskCount
    }));
  }

  backlogTaskCount(): number {
    const sprintTaskCount = this.sprints.reduce((sum, sprint) => sum + sprint.totalTaskCount, 0);
    return Math.max(this.totalTasks - sprintTaskCount, 0);
  }

  portfolioReadinessChart(): ChartLegendItem[] {
    return [
      {
        label: 'Team aligned',
        value: this.alignedProjectCount,
        percent: this.projectPercent(this.alignedProjectCount),
        tone: 'good'
      },
      {
        label: 'Needs attention',
        value: this.attentionProjectCount,
        percent: this.projectPercent(this.attentionProjectCount),
        tone: 'warning'
      },
      {
        label: 'Without teams',
        value: this.unassignedProjectCount,
        percent: this.projectPercent(this.unassignedProjectCount),
        tone: 'danger'
      }
    ];
  }

  portfolioReadinessConic(): string {
    return this.conicGradient(this.portfolioReadinessChart());
  }

  projectPressureChart(): ChartLegendItem[] {
    const totalTasks = Math.max(this.totalTasks, 1);
    return [
      {
        label: 'Task completion',
        value: this.completedTasks,
        percent: this.completionPercentage,
        tone: 'good'
      },
      {
        label: 'High priority',
        value: this.highPriorityTasks,
        percent: Math.min(100, Math.round((this.highPriorityTasks / totalTasks) * 100)),
        tone: 'warning'
      },
      {
        label: 'Overdue',
        value: this.overdueTasks,
        percent: Math.min(100, Math.round((this.overdueTasks / totalTasks) * 100)),
        tone: 'danger'
      }
    ];
  }

  taskStatusChart(): ChartLegendItem[] {
    return [
      { label: 'To do', value: this.toDoTasks, percent: this.taskStatusPercent(this.toDoTasks), tone: 'neutral' },
      { label: 'In progress', value: this.inProgressTasks, percent: this.taskStatusPercent(this.inProgressTasks), tone: 'warning' },
      { label: 'Review', value: this.reviewTasks, percent: this.taskStatusPercent(this.reviewTasks), tone: 'warning' },
      { label: 'Done', value: this.completedTasks, percent: this.taskStatusPercent(this.completedTasks), tone: 'good' }
    ];
  }

  sprintStatusChart(): ChartLegendItem[] {
    const statuses: { label: string; value: number; tone: ChartTone }[] = [
      { label: 'Planned', value: this.sprintStatusCount('PLANNED'), tone: 'neutral' },
      { label: 'Active', value: this.sprintStatusCount('ACTIVE'), tone: 'good' },
      { label: 'Completed', value: this.sprintStatusCount('COMPLETED'), tone: 'good' }
    ];

    return statuses.map((status) => ({
      ...status,
      percent: this.sprintPercent(status.value)
    }));
  }

  projectStatusChart(): ChartLegendItem[] {
    const statuses: { label: string; value: number; tone: ChartTone }[] = [
      { label: 'Planned', value: this.projectStatusCount('PLANNED'), tone: 'neutral' },
      { label: 'Active', value: this.projectStatusCount('ACTIVE'), tone: 'good' },
      { label: 'On hold', value: this.projectStatusCount('ON_HOLD'), tone: 'warning' },
      { label: 'Completed', value: this.projectStatusCount('COMPLETED'), tone: 'good' },
      { label: 'Archived', value: this.projectStatusCount('ARCHIVED'), tone: 'neutral' }
    ];

    return statuses.map((status) => ({
      ...status,
      percent: this.projectPercent(status.value)
    }));
  }

  chartToneClass(tone: ChartTone): string {
    return `chart-${tone}`;
  }

  projectChartOptions(): { id: number | 'ALL'; label: string }[] {
    return [
      { id: 'ALL', label: 'All projects' },
      ...this.projects.map((project) => ({ id: project.id, label: project.name }))
    ];
  }

  sprintChartOptions(): { id: number | 'ALL'; label: string }[] {
    return [
      { id: 'ALL', label: 'All sprints' },
      ...this.sprints.map((sprint) => ({ id: sprint.id, label: sprint.name }))
    ];
  }

  taskChartOptions(): { id: number | 'ALL'; label: string }[] {
    return [
      { id: 'ALL', label: this.selectedSprintChart() ? 'All tasks in sprint' : 'All tasks' },
      ...this.taskScopeTasks().map((task) => ({ id: task.id, label: task.title }))
    ];
  }

  selectedProjectChartTitle(): string {
    if (this.selectedProjectChartId === 'ALL') {
      return 'All projects';
    }

    return this.selectedProjectChart()?.name || 'Selected project';
  }

  selectedSprintChartTitle(): string {
    if (this.selectedSprintChartId === 'ALL') {
      return 'All sprints';
    }

    return this.selectedSprintChart()?.name || 'Selected sprint';
  }

  selectedTaskChartTitle(): string {
    if (this.selectedTaskChartId === 'ALL') {
      const sprint = this.selectedSprintChart();
      return sprint ? `${sprint.name} tasks` : 'All tasks';
    }

    return this.selectedTaskChart()?.title || 'Selected task';
  }

  projectStatusTotalForSelection(): number {
    return this.selectedProjectChart() ? 1 : this.projectCount;
  }

  sprintStatusTotalForSelection(): number {
    return this.selectedSprintChart() ? 1 : this.sprints.length;
  }

  taskStatusTotalForSelection(): number {
    return this.selectedTaskChart() ? 1 : this.taskScopeTasks().length;
  }

  projectStatusChartForSelection(): ChartLegendItem[] {
    const project = this.selectedProjectChart();
    if (!project) {
      return this.projectStatusChart();
    }

    return [
      { label: 'Planned', value: project.status === 'PLANNED' ? 1 : 0, percent: project.status === 'PLANNED' ? 100 : 0, tone: 'neutral' },
      { label: 'Active', value: project.status === 'ACTIVE' ? 1 : 0, percent: project.status === 'ACTIVE' ? 100 : 0, tone: 'good' },
      { label: 'On hold', value: project.status === 'ON_HOLD' ? 1 : 0, percent: project.status === 'ON_HOLD' ? 100 : 0, tone: 'warning' },
      { label: 'Completed', value: project.status === 'COMPLETED' ? 1 : 0, percent: project.status === 'COMPLETED' ? 100 : 0, tone: 'good' },
      { label: 'Archived', value: project.status === 'ARCHIVED' ? 1 : 0, percent: project.status === 'ARCHIVED' ? 100 : 0, tone: 'neutral' }
    ];
  }

  projectStatusConicForSelection(): string {
    return this.conicGradient(this.projectStatusChartForSelection());
  }

  sprintStatusChartForSelection(): ChartLegendItem[] {
    const sprint = this.selectedSprintChart();
    if (!sprint) {
      return this.sprintStatusChart();
    }

    return [
      { label: 'Planned', value: sprint.status === 'PLANNED' ? 1 : 0, percent: sprint.status === 'PLANNED' ? 100 : 0, tone: 'neutral' },
      { label: 'Active', value: sprint.status === 'ACTIVE' ? 1 : 0, percent: sprint.status === 'ACTIVE' ? 100 : 0, tone: 'good' },
      { label: 'Completed', value: sprint.status === 'COMPLETED' ? 1 : 0, percent: sprint.status === 'COMPLETED' ? 100 : 0, tone: 'good' }
    ];
  }

  sprintStatusConicForSelection(): string {
    return this.conicGradient(this.sprintStatusChartForSelection());
  }

  taskStatusChartForSelection(): ChartLegendItem[] {
    const task = this.selectedTaskChart();

    if (task) {
      return this.taskStatusChartFromTasks([task]);
    }

    return this.taskStatusChartFromTasks(this.taskScopeTasks());
  }

  taskStatusConicForSelection(): string {
    return this.conicGradient(this.taskStatusChartForSelection());
  }

  selectedProjectChart(): Project | undefined {
    return this.selectedProjectChartId === 'ALL'
      ? undefined
      : this.projects.find((project) => project.id === this.selectedProjectChartId);
  }

  selectedSprintChart(): Sprint | undefined {
    return this.selectedSprintChartId === 'ALL'
      ? undefined
      : this.sprints.find((sprint) => sprint.id === this.selectedSprintChartId);
  }

  selectedTaskChart(): Task | undefined {
    return this.selectedTaskChartId === 'ALL'
      ? undefined
      : this.taskScopeTasks().find((task) => task.id === this.selectedTaskChartId);
  }

  onSprintChartSelectionChange(): void {
    const scopeTasks = this.taskScopeTasks();
    if (this.selectedTaskChartId === 'ALL') {
      return;
    }

    if (!scopeTasks.some((task) => task.id === this.selectedTaskChartId)) {
      this.selectedTaskChartId = 'ALL';
    }
  }

  private taskStatusPercent(value: number): number {
    return this.totalTasks === 0 ? 0 : Math.round((value / this.totalTasks) * 100);
  }

  private projectPercent(value: number): number {
    return this.projectCount === 0 ? 0 : Math.round((value / this.projectCount) * 100);
  }

  private sprintPercent(value: number): number {
    return this.sprints.length === 0 ? 0 : Math.round((value / this.sprints.length) * 100);
  }

  private projectStatusCount(status: Project['status']): number {
    return this.projects.filter((project) => project.status === status).length;
  }

  private sprintStatusCount(status: Sprint['status']): number {
    return this.sprints.filter((sprint) => sprint.status === status).length;
  }

  private syncDashboardSelections(): void {
    const focusProjectId = this.selectedProjectId && this.projects.some((project) => project.id === this.selectedProjectId)
      ? this.selectedProjectId
      : undefined;
    if (focusProjectId !== this.lastFocusProjectId) {
      this.selectedProjectChartId = focusProjectId ?? 'ALL';
    } else if (this.selectedProjectChartId !== 'ALL' && !this.projects.some((project) => project.id === this.selectedProjectChartId)) {
      this.selectedProjectChartId = focusProjectId ?? 'ALL';
    }
    this.lastFocusProjectId = focusProjectId;

    const focusSprintId = this.resolveFocusSprintId();
    if (focusSprintId !== this.lastFocusSprintId) {
      this.selectedSprintChartId = focusSprintId ?? 'ALL';
    } else if (this.selectedSprintChartId !== 'ALL' && !this.sprints.some((sprint) => sprint.id === this.selectedSprintChartId)) {
      this.selectedSprintChartId = focusSprintId ?? 'ALL';
    }
    this.lastFocusSprintId = focusSprintId;

    const scopeTasks = this.taskScopeTasks();
    const focusTaskId = this.selectedTaskId && scopeTasks.some((task) => task.id === this.selectedTaskId)
      ? this.selectedTaskId
      : undefined;
    if (focusTaskId !== this.lastFocusTaskId) {
      this.selectedTaskChartId = focusTaskId ?? 'ALL';
    } else if (this.selectedTaskChartId !== 'ALL' && !scopeTasks.some((task) => task.id === this.selectedTaskChartId)) {
      this.selectedTaskChartId = focusTaskId ?? 'ALL';
    }
    this.lastFocusTaskId = focusTaskId;
  }

  private taskStatusChartFromTasks(tasks: Task[]): ChartLegendItem[] {
    const total = tasks.length;
    const count = (status: TaskStatus) => tasks.filter((task) => task.status === status).length;
    const percent = (value: number) => total === 0 ? 0 : Math.round((value / total) * 100);

    return [
      { label: 'To do', value: count('TO_DO'), percent: percent(count('TO_DO')), tone: 'neutral' },
      { label: 'In progress', value: count('IN_PROGRESS'), percent: percent(count('IN_PROGRESS')), tone: 'warning' },
      { label: 'Review', value: count('REVIEW'), percent: percent(count('REVIEW')), tone: 'warning' },
      { label: 'Done', value: count('DONE'), percent: percent(count('DONE')), tone: 'good' }
    ];
  }

  private taskScopeTasks(): Task[] {
    const sprint = this.selectedSprintChart();
    if (!sprint) {
      return this.tasks;
    }

    return this.tasks.filter((task) => task.sprintId === sprint.id);
  }

  private resolveFocusSprintId(): number | undefined {
    if (this.selectedSprintId && this.sprints.some((sprint) => sprint.id === this.selectedSprintId)) {
      return this.selectedSprintId;
    }

    return this.sprints.find((sprint) => sprint.status === 'ACTIVE')?.id ?? this.sprints[0]?.id;
  }

  private projectStatusTone(status: Project['status']): ChartTone {
    switch (status) {
      case 'ACTIVE':
      case 'COMPLETED':
        return 'good';
      case 'ON_HOLD':
        return 'warning';
      default:
        return 'neutral';
    }
  }

  private sprintStatusTone(status: Sprint['status']): ChartTone {
    switch (status) {
      case 'ACTIVE':
      case 'COMPLETED':
        return 'good';
      default:
        return 'warning';
    }
  }

  private sprintPriorityTone(priority: Sprint['priority']): ChartTone {
    switch (priority) {
      case 'CRITICAL':
      case 'HIGH':
        return 'danger';
      case 'MEDIUM':
        return 'warning';
      default:
        return 'good';
    }
  }

  private taskStatusTone(status: Task['status']): ChartTone {
    switch (status) {
      case 'DONE':
        return 'good';
      case 'IN_PROGRESS':
      case 'REVIEW':
        return 'warning';
      default:
        return 'neutral';
    }
  }

  private taskPriorityTone(priority: Task['priority']): ChartTone {
    switch (priority) {
      case 'HIGH':
        return 'danger';
      case 'MEDIUM':
        return 'warning';
      default:
        return 'good';
    }
  }

  private conicGradient(items: ChartLegendItem[]): string {
    const palette: Record<ChartTone, string> = {
      good: '#0f9f6e',
      warning: '#f59e0b',
      danger: '#c2413b',
      neutral: '#00aee9'
    };
    const total = items.reduce((sum, item) => sum + item.value, 0);

    if (total === 0) {
      return 'conic-gradient(#dff6ff 0deg 360deg)';
    }

    let cursor = 0;
    const segments = items
      .filter((item) => item.value > 0)
      .map((item) => {
        const start = cursor;
        const size = (item.value / total) * 360;
        cursor += size;
        return `${palette[item.tone]} ${start}deg ${cursor}deg`;
      });

    return `conic-gradient(${segments.join(', ')})`;
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
        const projectCount = this.projects.filter((project) => project.teams?.some((team) => team.id === unit.id)).length;
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
      teamLabel: this.projectOwnerLabel(project),
      timeline: this.projectTimeline(project),
      healthLabel: this.projectHealthLabel(project),
      healthTone: this.projectHealthTone(project)
    }));
  }

  private hasOwningUnit(project: Project): boolean {
    return Boolean(project.teams && project.teams.length > 0);
  }

  private projectOwnerLabel(project: Project): string {
    if (project.teams && project.teams.length > 0) {
      return project.teams.map((team) => team.name).join(', ');
    }

    return 'No teams assigned';
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

    if (project.health) {
      return this.formatStatus(project.health);
    }

    if (!this.hasOwningUnit(project)) {
      return 'Needs team alignment';
    }

    if (!project.dueDate) {
      return 'Schedule not set';
    }

    return 'On Track';
  }

  private projectHealthTone(project: Project): HealthTone {
    if (this.isClosedProject(project)) {
      return 'good';
    }

    const normalized = (project.health || '').trim().toUpperCase();
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

  private projectUrgencyScore(project: Project): number {
    if (this.isClosedProject(project)) {
      return 0;
    }

    let score = 0;

    if (!this.hasOwningUnit(project)) {
      score += 3;
    }

    const normalizedHealth = (project.health || '').trim().toUpperCase();
    if (normalizedHealth === 'OFF_TRACK' || normalizedHealth === 'BLOCKED') {
      score += 4;
    } else if (normalizedHealth === 'AT_RISK') {
      score += 2;
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
    const normalizedHealth = (project.health || '').trim().toUpperCase();
      return !this.isClosedProject(project)
      && (!this.hasOwningUnit(project)
        || this.isOverdue(project)
        || this.isDueSoon(project)
        || normalizedHealth === 'AT_RISK'
        || normalizedHealth === 'OFF_TRACK'
        || normalizedHealth === 'BLOCKED');
  }

  projectPrimaryTeamType(project: Project): string | null {
    return project.teams && project.teams.length > 0 ? project.teams[0].type : null;
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
    return ['COMPLETED', 'ARCHIVED', 'CLOSED', 'DELIVERED', 'DONE'].includes(normalizedStatus);
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
