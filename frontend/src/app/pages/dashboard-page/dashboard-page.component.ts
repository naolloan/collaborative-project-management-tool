import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrganizationalUnit } from '../../core/dto/organizational-unit';
import { Project } from '../../core/dto/project';
import { Sprint } from '../../core/dto/sprint';
import { Task, TaskStatus } from '../../core/dto/task';

type HealthTone = 'good' | 'warning' | 'danger' | 'neutral';
type ChartTone = 'good' | 'warning' | 'danger' | 'neutral';
type SprintAnalyticsView = 'progress' | 'lifecycle' | 'tasks';

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
  activeSprintAnalyticsView: SprintAnalyticsView = 'progress';
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
      { label: 'Progress', value: `${this.projectProgressValue(project)}%`, tone: this.progressMetricTone(this.projectProgressValue(project)) },
      { label: 'Health', value: this.projectHealthLabel(project), tone: this.projectHealthTone(project) },
      {
        label: 'Teams',
        value: project.teams?.length ? `${project.teams.length} assigned` : 'Missing team coverage',
        tone: project.teams?.length ? 'good' : 'danger'
      },
      { label: 'Schedule', value: this.projectScheduleLabel(project), tone: this.projectScheduleTone(project) }
    ];
  }

  selectedSprintStatusMetrics(): SelectedStatusMetric[] {
    const sprint = this.selectedSprintChart();
    if (!sprint) {
      return [];
    }

    const completion = this.sprintProgressPercentage(sprint);

    return [
      { label: 'Health', value: this.sprintDeliveryLabel(sprint), tone: this.sprintDeliveryTone(sprint) },
      {
        label: 'Completion',
        value: `${completion}%`,
        tone: completion >= 100 ? 'good' : completion >= 50 ? 'warning' : 'danger'
      },
      {
        label: 'Priority',
        value: this.formatStatus(sprint.priority),
        tone: this.sprintPriorityTone(sprint.priority)
      }
    ];
  }

  selectedTaskStatusMetrics(): SelectedStatusMetric[] {
    const task = this.selectedTaskChart();
    if (!task) {
      return [];
    }

    return [
      { label: 'Delivery', value: this.taskDeliveryLabel(task), tone: this.taskDeliveryTone(task) },
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
    return this.taskStatusChartFromTasks(this.tasks);
  }

  sprintStatusChart(): ChartLegendItem[] {
    return this.sprintProgressChartFromSprints(this.sprints);
  }

  projectStatusChart(): ChartLegendItem[] {
    return this.projectProgressChartFromProjects(this.projects);
  }

  projectLifecycleChart(): ChartLegendItem[] {
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

  sprintLifecycleChart(): ChartLegendItem[] {
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

  projectStatusCenterLabel(): string {
    const project = this.selectedProjectChart();
    return project ? `${this.projectProgressValue(project)}%` : String(this.projectCount);
  }

  sprintStatusCenterLabel(): string {
    const sprint = this.selectedSprintChart();
    return sprint ? `${this.sprintProgressPercentage(sprint)}%` : String(this.sprints.length);
  }

  taskStatusCenterLabel(): string {
    const task = this.selectedTaskChart();
    return task ? this.formatStatus(task.status) : String(this.taskScopeTasks().length);
  }

  projectStatusChartForSelection(): ChartLegendItem[] {
    const project = this.selectedProjectChart();
    if (!project) {
      return this.projectStatusChart();
    }

    return this.projectProgressChartForProject(project);
  }

  projectStatusConicForSelection(): string {
    return this.conicGradient(this.projectStatusChartForSelection());
  }

  projectLifecycleChartForSelection(): ChartLegendItem[] {
    const project = this.selectedProjectChart();
    if (!project) {
      return this.projectLifecycleChart();
    }

    return [
      { label: 'Planned', value: project.status === 'PLANNED' ? 1 : 0, percent: project.status === 'PLANNED' ? 100 : 0, tone: 'neutral' },
      { label: 'Active', value: project.status === 'ACTIVE' ? 1 : 0, percent: project.status === 'ACTIVE' ? 100 : 0, tone: 'good' },
      { label: 'On hold', value: project.status === 'ON_HOLD' ? 1 : 0, percent: project.status === 'ON_HOLD' ? 100 : 0, tone: 'warning' },
      { label: 'Completed', value: project.status === 'COMPLETED' ? 1 : 0, percent: project.status === 'COMPLETED' ? 100 : 0, tone: 'good' },
      { label: 'Archived', value: project.status === 'ARCHIVED' ? 1 : 0, percent: project.status === 'ARCHIVED' ? 100 : 0, tone: 'neutral' }
    ];
  }

  projectLifecycleConicForSelection(): string {
    return this.conicGradient(this.projectLifecycleChartForSelection());
  }

  sprintStatusChartForSelection(): ChartLegendItem[] {
    const sprint = this.selectedSprintChart();
    if (!sprint) {
      return this.sprintStatusChart();
    }

    return this.sprintProgressChartForSprint(sprint);
  }

  sprintStatusConicForSelection(): string {
    return this.conicGradient(this.sprintStatusChartForSelection());
  }

  sprintLifecycleChartForSelection(): ChartLegendItem[] {
    const sprint = this.selectedSprintChart();
    if (!sprint) {
      return this.sprintLifecycleChart();
    }

    return [
      { label: 'Planned', value: sprint.status === 'PLANNED' ? 1 : 0, percent: sprint.status === 'PLANNED' ? 100 : 0, tone: 'neutral' },
      { label: 'Active', value: sprint.status === 'ACTIVE' ? 1 : 0, percent: sprint.status === 'ACTIVE' ? 100 : 0, tone: 'good' },
      { label: 'Completed', value: sprint.status === 'COMPLETED' ? 1 : 0, percent: sprint.status === 'COMPLETED' ? 100 : 0, tone: 'good' }
    ];
  }

  sprintLifecycleConicForSelection(): string {
    return this.conicGradient(this.sprintLifecycleChartForSelection());
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

  setSprintAnalyticsView(view: SprintAnalyticsView): void {
    this.activeSprintAnalyticsView = view;
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
    const count = (label: string) => tasks.filter((task) => this.taskDeliveryLabel(task) === label).length;
    const percent = (value: number) => total === 0 ? 0 : Math.round((value / total) * 100);

    return [
      { label: 'Completed', value: count('Completed'), percent: percent(count('Completed')), tone: 'good' },
      { label: 'In execution', value: count('In execution'), percent: percent(count('In execution')), tone: 'warning' },
      { label: 'Overdue', value: count('Overdue'), percent: percent(count('Overdue')), tone: 'danger' },
      { label: 'Unassigned', value: count('Unassigned'), percent: percent(count('Unassigned')), tone: 'warning' },
      { label: 'Ready to start', value: count('Ready to start'), percent: percent(count('Ready to start')), tone: 'neutral' }
    ];
  }

  private projectProgressChartFromProjects(projects: Project[]): ChartLegendItem[] {
    const total = projects.length;
    const count = (label: string) => projects.filter((project) => this.projectProgressBandLabel(project) === label).length;
    const percent = (value: number) => total === 0 ? 0 : Math.round((value / total) * 100);

    return [
      { label: 'Not started', value: count('Not started'), percent: percent(count('Not started')), tone: 'neutral' },
      { label: 'Early', value: count('Early'), percent: percent(count('Early')), tone: 'warning' },
      { label: 'Midway', value: count('Midway'), percent: percent(count('Midway')), tone: 'warning' },
      { label: 'Near completion', value: count('Near completion'), percent: percent(count('Near completion')), tone: 'good' },
      { label: 'Delivered', value: count('Delivered'), percent: percent(count('Delivered')), tone: 'good' }
    ];
  }

  private projectProgressChartForProject(project: Project): ChartLegendItem[] {
    const completed = this.projectProgressValue(project);
    const remaining = Math.max(100 - completed, 0);

    return [
      { label: 'Completed sprint scope', value: completed, percent: completed, tone: 'good' },
      { label: 'Remaining sprint scope', value: remaining, percent: remaining, tone: remaining > 0 ? 'warning' : 'neutral' }
    ];
  }

  private sprintProgressChartFromSprints(sprints: Sprint[]): ChartLegendItem[] {
    const total = sprints.length;
    const count = (label: string) => sprints.filter((sprint) => this.sprintProgressBandLabel(sprint) === label).length;
    const percent = (value: number) => total === 0 ? 0 : Math.round((value / total) * 100);

    return [
      { label: 'Not started', value: count('Not started'), percent: percent(count('Not started')), tone: 'neutral' },
      { label: 'Early', value: count('Early'), percent: percent(count('Early')), tone: 'warning' },
      { label: 'Midway', value: count('Midway'), percent: percent(count('Midway')), tone: 'warning' },
      { label: 'Near completion', value: count('Near completion'), percent: percent(count('Near completion')), tone: 'good' },
      { label: 'Delivered', value: count('Delivered'), percent: percent(count('Delivered')), tone: 'good' }
    ];
  }

  private sprintProgressChartForSprint(sprint: Sprint): ChartLegendItem[] {
    const completed = this.sprintProgressPercentage(sprint);
    const remaining = Math.max(100 - completed, 0);

    return [
      { label: 'Completed task scope', value: completed, percent: completed, tone: 'good' },
      { label: 'Remaining task scope', value: remaining, percent: remaining, tone: remaining > 0 ? 'warning' : 'neutral' }
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

  private projectDeliveryLabel(project: Project): string {
    if (this.isClosedProject(project)) {
      return 'Closed out';
    }

    if (!this.hasOwningUnit(project)) {
      return 'Missing teams';
    }

    if (!project.startDate || !project.dueDate) {
      return 'Planning window';
    }

    if (this.needsAttention(project)) {
      return 'Needs support';
    }

    return 'On track';
  }

  private projectProgressValue(project: Project): number {
    if (this.selectedProjectId === project.id) {
      return Math.max(0, Math.min(100, Math.round(this.projectProgressPercentage)));
    }

    if (project.progressPercentage == null) {
      return this.isClosedProject(project) ? 100 : 0;
    }

    return Math.max(0, Math.min(100, Math.round(project.progressPercentage)));
  }

  private projectProgressBandLabel(project: Project): string {
    const progress = this.projectProgressValue(project);
    if (progress >= 100) {
      return 'Delivered';
    }
    if (progress >= 75) {
      return 'Near completion';
    }
    if (progress >= 35) {
      return 'Midway';
    }
    if (progress > 0) {
      return 'Early';
    }

    return 'Not started';
  }

  private progressMetricTone(progress: number): ChartTone {
    if (progress >= 100) {
      return 'good';
    }

    if (progress > 0) {
      return 'warning';
    }

    return 'neutral';
  }

  private projectScheduleLabel(project: Project): string {
    if (this.isClosedProject(project)) {
      return 'Closed delivery';
    }

    const remainingDays = this.daysUntil(project.dueDate);
    if (remainingDays !== null && remainingDays < 0) {
      return `Overdue by ${Math.abs(remainingDays)} day${Math.abs(remainingDays) === 1 ? '' : 's'}`;
    }

    if (remainingDays !== null && remainingDays <= 14) {
      return `Due in ${remainingDays} day${remainingDays === 1 ? '' : 's'}`;
    }

    return this.projectTimeline(project);
  }

  private projectScheduleTone(project: Project): ChartTone {
    if (this.isClosedProject(project)) {
      return 'good';
    }

    if (this.isOverdue(project)) {
      return 'danger';
    }

    if (this.isDueSoon(project) || !project.startDate || !project.dueDate) {
      return 'warning';
    }

    return 'neutral';
  }

  private sprintDeliveryLabel(sprint: Sprint): string {
    if (this.isSprintDelivered(sprint)) {
      return 'Delivered';
    }

    if (sprint.totalTaskCount === 0) {
      return 'Empty scope';
    }

    if (sprint.status === 'PLANNED' || !sprint.startDate || !sprint.endDate) {
      return 'Setup needed';
    }

    if (this.isSprintHighPressure(sprint)) {
      return 'High pressure';
    }

    return 'In delivery';
  }

  private sprintProgressPercentage(sprint: Sprint): number {
    if (sprint.totalTaskCount <= 0) {
      return sprint.status === 'COMPLETED' ? 100 : 0;
    }

    return Math.round((sprint.completedTaskCount / sprint.totalTaskCount) * 100);
  }

  private sprintProgressBandLabel(sprint: Sprint): string {
    const progress = this.sprintProgressPercentage(sprint);
    if (progress >= 100) {
      return 'Delivered';
    }
    if (progress >= 75) {
      return 'Near completion';
    }
    if (progress >= 35) {
      return 'Midway';
    }
    if (progress > 0) {
      return 'Early';
    }

    return 'Not started';
  }

  private sprintDeliveryTone(sprint: Sprint): ChartTone {
    switch (this.sprintDeliveryLabel(sprint)) {
      case 'Delivered':
      case 'In delivery':
        return 'good';
      case 'High pressure':
        return 'danger';
      case 'Setup needed':
        return 'warning';
      default:
        return 'neutral';
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

  private isSprintDelivered(sprint: Sprint): boolean {
    return sprint.status === 'COMPLETED' || (sprint.totalTaskCount > 0 && sprint.completedTaskCount >= sprint.totalTaskCount);
  }

  private isSprintHighPressure(sprint: Sprint): boolean {
    if (this.isSprintDelivered(sprint)) {
      return false;
    }

    const completion = sprint.totalTaskCount === 0 ? 0 : Math.round((sprint.completedTaskCount / sprint.totalTaskCount) * 100);
    const remainingDays = this.daysUntil(sprint.endDate);
    const elevatedPriority = sprint.priority === 'HIGH' || sprint.priority === 'CRITICAL';

    if (remainingDays !== null && remainingDays < 0) {
      return true;
    }

    if (remainingDays !== null && remainingDays <= 3 && completion < 75) {
      return true;
    }

    return elevatedPriority && completion < 60;
  }

  private taskDeliveryLabel(task: Task): string {
    if (task.status === 'DONE') {
      return 'Completed';
    }

    if (this.isTaskOverdue(task)) {
      return 'Overdue';
    }

    if (!this.isTaskAssigned(task)) {
      return 'Unassigned';
    }

    if (task.status === 'IN_PROGRESS' || task.status === 'REVIEW') {
      return 'In execution';
    }

    return 'Ready to start';
  }

  private taskDeliveryTone(task: Task): ChartTone {
    switch (this.taskDeliveryLabel(task)) {
      case 'Completed':
        return 'good';
      case 'Overdue':
        return 'danger';
      case 'Unassigned':
      case 'In execution':
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

  private isTaskAssigned(task: Task): boolean {
    return Boolean(task.assigneeId || task.assigneeName);
  }

  private isTaskOverdue(task: Task): boolean {
    const remainingDays = this.daysUntil(task.dueDate);
    return task.status !== 'DONE' && remainingDays !== null && remainingDays < 0;
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
