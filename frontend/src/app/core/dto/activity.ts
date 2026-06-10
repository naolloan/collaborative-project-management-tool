export interface Activity {
  id: number;
  taskId: number;
  actorName: string;
  actionType: string;
  oldValue: string | null;
  newValue: string | null;
  message: string;
  createdAt: string;
}
