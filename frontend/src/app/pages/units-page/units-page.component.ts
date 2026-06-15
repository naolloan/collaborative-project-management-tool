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

  formatRole(role: string | null | undefined): string {
    return role ? role.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()) : 'Member';
  }

  selectedTeamRecord(): ProjectTeam | undefined {
    return this.projectTeams.find((team) => team.id === this.editTeam.id);
  }

  projectManagerCount(): number {
    return this.projectMembers.filter((member) => member.projectRole === 'MANAGER').length;
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
}
