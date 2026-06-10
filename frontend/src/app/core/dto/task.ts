export type TaskStatus = 'TO_DO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Task {
  id: number;
  projectId: number;
  title: string;
  description?: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  assigneeId?: number | null;
  assigneeName?: string | null;
  createdByName?: string | null;
  dueDate?: string | null;
}

export interface CreateTaskRequest {
  title: string;
  description?: string | null;
  priority: TaskPriority;
  assigneeId?: number | null;
  dueDate?: string | null;
}

export interface UpdateTaskRequest extends CreateTaskRequest {
}
