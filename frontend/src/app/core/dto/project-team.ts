import { ProjectRole } from './project-member';

export interface ProjectTeamMember {
  userId: number;
  fullName: string;
  email: string;
  projectRole: ProjectRole;
}

export interface ProjectTeam {
  id: number;
  projectId: number;
  name: string;
  description?: string | null;
  members: ProjectTeamMember[];
  memberCount: number;
  managerCount: number;
}

export interface ProjectTeamRequest {
  name: string;
  description?: string | null;
  memberUserIds: number[];
}
