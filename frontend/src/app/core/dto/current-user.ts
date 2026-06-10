export interface CurrentUser {
  id: number;
  username: string | null;
  email: string | null;
  fullName: string | null;
  roles: string[];
}
