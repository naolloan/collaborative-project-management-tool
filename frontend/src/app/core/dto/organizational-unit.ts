export type OrganizationalUnitType = 'HEAD_OFFICE' | 'DEPARTMENT' | 'BRANCH' | 'DIVISION' | 'TEAM';

export interface OrganizationalUnit {
  id: number;
  name: string;
  type: OrganizationalUnitType;
  description?: string | null;
  active: boolean;
}

export interface OrganizationalUnitRequest {
  name: string;
  type: OrganizationalUnitType;
  description?: string | null;
}
