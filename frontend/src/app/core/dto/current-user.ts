export interface CurrentUser {
  id: number;
  systemRole: string;
  username: string | null;
  email: string | null;
  fullName: string | null;
  roles: string[];
}
