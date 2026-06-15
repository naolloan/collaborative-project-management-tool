export type SprintStatus = 'PLANNED' | 'ACTIVE' | 'COMPLETED';
export type SprintPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface Sprint {
  id: number;
  projectId: number;
  name: string;
  goal?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  status: SprintStatus;
  priority: SprintPriority;
  totalTaskCount: number;
  completedTaskCount: number;
}

export interface CreateSprintRequest {
  name: string;
  goal?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  status: SprintStatus;
  priority: SprintPriority;
}

export interface UpdateSprintRequest extends CreateSprintRequest {
}
