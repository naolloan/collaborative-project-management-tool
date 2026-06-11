export interface Project {
  id: number;
  name: string;
  description?: string | null;
  organizationalUnitId?: number | null;
  organizationalUnitName?: string | null;
  organizationalUnitType?: string | null;
  startDate?: string | null;
  dueDate?: string | null;
  status: string;
}

export interface CreateProjectRequest {
  name: string;
  description?: string | null;
  organizationalUnitId?: number | null;
  startDate?: string | null;
  dueDate?: string | null;
}

export interface UpdateProjectRequest extends CreateProjectRequest {
}
