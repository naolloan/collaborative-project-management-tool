export type ProjectRole = 'MANAGER' | 'MEMBER';

export interface ProjectMember {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  projectRole: ProjectRole;
  joinedAt: string;
}

export interface AddProjectMemberRequest {
  email: string;
  projectRole: ProjectRole;
}
