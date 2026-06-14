export type ProjectLifecycleStatus = 'PLANNED' | 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED';
export type ProjectHealth = 'ON_TRACK' | 'AT_RISK' | 'OFF_TRACK' | 'BLOCKED';

export interface Project {
  id: number;
  name: string;
  description?: string | null;
  organizationalUnitId?: number | null;
  organizationalUnitName?: string | null;
  organizationalUnitType?: string | null;
  startDate?: string | null;
  dueDate?: string | null;
  status: ProjectLifecycleStatus;
  health: ProjectHealth;
}

export interface CreateProjectRequest {
  name: string;
  description?: string | null;
  organizationalUnitId?: number | null;
  startDate?: string | null;
  dueDate?: string | null;
  status: ProjectLifecycleStatus;
  health: ProjectHealth;
}

export interface UpdateProjectRequest extends CreateProjectRequest {
}
