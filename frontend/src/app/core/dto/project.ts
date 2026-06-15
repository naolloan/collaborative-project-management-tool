export type ProjectLifecycleStatus = 'PLANNED' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';
export type ProjectHealth = 'ON_TRACK' | 'AT_RISK' | 'OFF_TRACK' | 'BLOCKED';

export interface ProjectTeamSummary {
  id: number;
  name: string;
  type: string;
}

export interface Project {
  id: number;
  name: string;
  description?: string | null;
  teams: ProjectTeamSummary[];
  startDate?: string | null;
  dueDate?: string | null;
  status: ProjectLifecycleStatus;
  health: ProjectHealth;
}

export interface CreateProjectRequest {
  name: string;
  description?: string | null;
  teamIds?: number[] | null;
  startDate?: string | null;
  dueDate?: string | null;
  status: ProjectLifecycleStatus;
}

export interface UpdateProjectRequest extends CreateProjectRequest {
}
