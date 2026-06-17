import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProjectMember } from '../../core/dto/project-member';
import { ProjectTeam } from '../../core/dto/project-team';
import { Project } from '../../core/dto/project';

@Component({
  selector: 'app-teams-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './units-page.component.html'
})
export class TeamsPageComponent {
  @Input({ required: true }) selectedProject: Project | undefined;
  @Input({ required: true }) selectedProjectId: number | undefined;
  @Input({ required: true }) projectTeams: ProjectTeam[] = [];
  @Input({ required: true }) projectMembers: ProjectMember[] = [];
  @Input({ required: true }) teamStatus = 'No project selected';
  @Input({ required: true }) canManage = false;
  @Input({ required: true }) creatingTeam = false;
  @Input({ required: true }) updatingTeam = false;
  @Input({ required: true }) newTeam!: {
    name: string;
    description: string;
    memberUserIds: number[];
  };
  @Input({ required: true }) editTeam!: {
    id: number | null;
    name: string;
    description: string;
    memberUserIds: number[];
  };

  @Output() refreshTeams = new EventEmitter<void>();
  @Output() createTeam = new EventEmitter<void>();
  @Output() selectTeam = new EventEmitter<ProjectTeam>();
  @Output() updateTeam = new EventEmitter<void>();

  teamSearch = '';
  teamRoleFilter: 'MANAGER' | 'MEMBER' | '' = '';
  createMemberSearch = '';
  editMemberSearch = '';
  createMemberRoleFilter: 'MANAGER' | 'MEMBER' | '' = '';
  editMemberRoleFilter: 'MANAGER' | 'MEMBER' | '' = '';

  formatRole(role: string | null | undefined): string {
    return role ? role.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()) : 'Member';
  }

  selectedTeamRecord(): ProjectTeam | undefined {
    return this.projectTeams.find((team) => team.id === this.editTeam.id);
  }

  projectManagerCount(): number {
    return this.projectMembers.filter((member) => member.projectRole === 'MANAGER').length;
  }

  filteredProjectTeams(): ProjectTeam[] {
    const search = this.teamSearch.trim().toLowerCase();
    if (!search) {
      return this.projectTeams;
    }

    return this.projectTeams.filter((team) => {
      const memberNames = team.members.map((member) => `${member.fullName} ${member.email}`).join(' ').toLowerCase();
      const matchesSearch = !search
        || team.name.toLowerCase().includes(search)
        || (team.description ?? '').toLowerCase().includes(search)
        || memberNames.includes(search);
      const matchesRole = !this.teamRoleFilter || team.members.some((member) => member.projectRole === this.teamRoleFilter);

      return matchesSearch && matchesRole;
    });
  }

  filteredProjectMembers(scope: 'create' | 'edit'): ProjectMember[] {
    const search = (scope === 'create' ? this.createMemberSearch : this.editMemberSearch).trim().toLowerCase();
    const roleFilter = scope === 'create' ? this.createMemberRoleFilter : this.editMemberRoleFilter;

    return this.projectMembers.filter((member) => (
      (!search
        || member.fullName.toLowerCase().includes(search)
        || member.email.toLowerCase().includes(search))
      && (!roleFilter || member.projectRole === roleFilter)
    ));
  }

  availableProjectMembers(scope: 'create' | 'edit'): ProjectMember[] {
    return this.filteredProjectMembers(scope).filter((member) => !this.isMemberSelectedForScope(scope, member.userId));
  }

  selectedProjectMembers(scope: 'create' | 'edit'): ProjectMember[] {
    const selectedIds = new Set(scope === 'create' ? this.newTeam.memberUserIds : this.editTeam.memberUserIds);
    return this.projectMembers.filter((member) => selectedIds.has(member.userId));
  }

  isMemberSelectedForScope(scope: 'create' | 'edit', userId: number): boolean {
    return this.isMemberSelected(scope === 'create' ? this.newTeam.memberUserIds : this.editTeam.memberUserIds, userId);
  }

  toggleMemberSelection(scope: 'create' | 'edit', userId: number): void {
    if (scope === 'create') {
      this.newTeam.memberUserIds = this.toggleSelectedId(this.newTeam.memberUserIds, userId);
      return;
    }

    this.editTeam.memberUserIds = this.toggleSelectedId(this.editTeam.memberUserIds, userId);
  }

  removeSelectedMember(scope: 'create' | 'edit', userId: number): void {
    if (scope === 'create') {
      this.newTeam.memberUserIds = this.newTeam.memberUserIds.filter((memberId) => memberId !== userId);
      return;
    }

    this.editTeam.memberUserIds = this.editTeam.memberUserIds.filter((memberId) => memberId !== userId);
  }

  isMemberSelected(memberUserIds: number[], userId: number): boolean {
    return memberUserIds.includes(userId);
  }

  toggleMember(target: { memberUserIds: number[] }, userId: number, checked: boolean): void {
    if (checked) {
      if (!target.memberUserIds.includes(userId)) {
        target.memberUserIds = [...target.memberUserIds, userId];
      }
      return;
    }

    target.memberUserIds = target.memberUserIds.filter((memberId) => memberId !== userId);
  }

  teamSummary(team: ProjectTeam | undefined): string {
    if (!team) {
      return 'Select a team to inspect its delivery makeup and permissions.';
    }

    if (team.members.length === 0) {
      return 'No members have been assigned to this team yet.';
    }

    return `${team.memberCount} member${team.memberCount === 1 ? '' : 's'} with ${team.managerCount} manager${team.managerCount === 1 ? '' : 's'} in the team.`;
  }

  private toggleSelectedId(selectedIds: number[], targetId: number): number[] {
    if (selectedIds.includes(targetId)) {
      return selectedIds.filter((id) => id !== targetId);
    }

    return [...selectedIds, targetId];
  }
}
