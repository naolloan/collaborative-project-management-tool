export interface Activity {
  id: number;
  projectId: number;
  sprintId?: number | null;
  taskId?: number | null;
  subjectType: string;
  subjectName: string;
  actorName: string;
  actionType: string;
  oldValue: string | null;
  newValue: string | null;
  message: string;
  createdAt: string;
}
