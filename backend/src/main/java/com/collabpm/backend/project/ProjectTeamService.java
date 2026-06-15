package com.collabpm.backend.project;

import com.collabpm.backend.activity.ActivityService;
import com.collabpm.backend.organization.model.OrganizationalUnit;
import com.collabpm.backend.organization.model.OrganizationalUnitType;
import com.collabpm.backend.organization.repository.OrganizationalUnitRepository;
import com.collabpm.backend.project.dto.CreateProjectTeamRequest;
import com.collabpm.backend.project.dto.ProjectTeamMemberResponse;
import com.collabpm.backend.project.dto.ProjectTeamResponse;
import com.collabpm.backend.project.dto.UpdateProjectTeamRequest;
import com.collabpm.backend.project.model.Project;
import com.collabpm.backend.project.model.ProjectMember;
import com.collabpm.backend.project.model.ProjectRole;
import com.collabpm.backend.project.model.ProjectTeamMember;
import com.collabpm.backend.project.repository.ProjectMemberRepository;
import com.collabpm.backend.project.repository.ProjectRepository;
import com.collabpm.backend.project.repository.ProjectTeamMemberRepository;
import com.collabpm.backend.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectTeamService {

    private final ProjectRepository projectRepository;
    private final OrganizationalUnitRepository organizationalUnitRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectTeamMemberRepository projectTeamMemberRepository;
    private final ProjectAccessService projectAccessService;
    private final ActivityService activityService;

    public ProjectTeamService(
        ProjectRepository projectRepository,
        OrganizationalUnitRepository organizationalUnitRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectTeamMemberRepository projectTeamMemberRepository,
        ProjectAccessService projectAccessService,
        ActivityService activityService
    ) {
        this.projectRepository = projectRepository;
        this.organizationalUnitRepository = organizationalUnitRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectTeamMemberRepository = projectTeamMemberRepository;
        this.projectAccessService = projectAccessService;
        this.activityService = activityService;
    }

    @Transactional(readOnly = true)
    public List<ProjectTeamResponse> listProjectTeams(Long projectId, Authentication authentication) {
        Project project = ensureProjectExists(projectId);
        projectAccessService.ensureCanViewProject(projectId, authentication);

        List<ProjectTeamMember> memberships = projectTeamMemberRepository.findByProjectIdOrderByTeamNameAscUserFullNameAsc(projectId);
        Map<Long, List<ProjectTeamMember>> membershipsByTeam = memberships.stream()
            .collect(LinkedHashMap::new, (map, membership) -> map.computeIfAbsent(membership.getTeam().getId(), ignored -> new ArrayList<>()).add(membership), Map::putAll);

        Map<Long, ProjectRole> projectRolesByUser = projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId).stream()
            .collect(LinkedHashMap::new, (map, member) -> map.put(member.getUser().getId(), member.getProjectRole()), Map::putAll);

        return project.getTeams().stream()
            .filter((team) -> team.getType() == OrganizationalUnitType.TEAM && team.isActive())
            .sorted(Comparator.comparing(OrganizationalUnit::getName, String.CASE_INSENSITIVE_ORDER))
            .map((team) -> toResponse(projectId, team, membershipsByTeam.getOrDefault(team.getId(), List.of()), projectRolesByUser))
            .toList();
    }

    @Transactional
    public ProjectTeamResponse createProjectTeam(
        Long projectId,
        CreateProjectTeamRequest request,
        Authentication authentication
    ) {
        Project project = ensureProjectExists(projectId);
        User actor = projectAccessService.ensureCanManageProject(projectId, authentication);
        String name = normalizeName(request.name());

        if (organizationalUnitRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Team name already exists");
        }

        OrganizationalUnit team = organizationalUnitRepository.save(new OrganizationalUnit(
            name,
            OrganizationalUnitType.TEAM,
            normalizeDescription(request.description())
        ));

        project.getTeams().add(team);
        if (project.getOrganizationalUnit() == null) {
            project.setOrganizationalUnit(team);
        }
        projectRepository.save(project);

        syncTeamMembers(project, team, request.memberUserIds());
        activityService.recordProjectUpdated(project, actor);

        return buildTeamResponse(projectId, team);
    }

    @Transactional
    public ProjectTeamResponse updateProjectTeam(
        Long projectId,
        Long teamId,
        UpdateProjectTeamRequest request,
        Authentication authentication
    ) {
        Project project = ensureProjectExists(projectId);
        User actor = projectAccessService.ensureCanManageProject(projectId, authentication);
        OrganizationalUnit team = ensureTeamBelongsToProject(project, teamId);
        String name = normalizeName(request.name());

        if (!team.getName().equalsIgnoreCase(name) && organizationalUnitRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Team name already exists");
        }

        team.setName(name);
        team.setDescription(normalizeDescription(request.description()));
        syncTeamMembers(project, team, request.memberUserIds());
        activityService.recordProjectUpdated(project, actor);

        return buildTeamResponse(projectId, team);
    }

    private Project ensureProjectExists(Long projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private OrganizationalUnit ensureTeamBelongsToProject(Project project, Long teamId) {
        return project.getTeams().stream()
            .filter((team) -> team.getId().equals(teamId) && team.getType() == OrganizationalUnitType.TEAM && team.isActive())
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project team not found"));
    }

    private void syncTeamMembers(Project project, OrganizationalUnit team, Collection<Long> memberUserIds) {
        Set<Long> targetUserIds = new LinkedHashSet<>();
        if (memberUserIds != null) {
            memberUserIds.stream()
                .filter((memberUserId) -> memberUserId != null)
                .forEach(targetUserIds::add);
        }

        Map<Long, ProjectMember> projectMembersByUser = projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(project.getId()).stream()
            .collect(LinkedHashMap::new, (map, member) -> map.put(member.getUser().getId(), member), Map::putAll);

        for (Long userId : targetUserIds) {
            if (!projectMembersByUser.containsKey(userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team members must already belong to the project");
            }
        }

        List<ProjectTeamMember> existingMemberships = projectTeamMemberRepository.findByProjectIdAndTeamIdOrderByUserFullNameAsc(project.getId(), team.getId());
        Set<Long> existingUserIds = existingMemberships.stream()
            .map((membership) -> membership.getUser().getId())
            .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<ProjectTeamMember> membershipsToRemove = existingMemberships.stream()
            .filter((membership) -> !targetUserIds.contains(membership.getUser().getId()))
            .toList();
        if (!membershipsToRemove.isEmpty()) {
            projectTeamMemberRepository.deleteAll(membershipsToRemove);
        }

        List<ProjectTeamMember> membershipsToAdd = targetUserIds.stream()
            .filter((userId) -> !existingUserIds.contains(userId))
            .map(projectMembersByUser::get)
            .map((member) -> new ProjectTeamMember(project, team, member.getUser()))
            .toList();
        if (!membershipsToAdd.isEmpty()) {
            projectTeamMemberRepository.saveAll(membershipsToAdd);
        }
    }

    private ProjectTeamResponse buildTeamResponse(Long projectId, OrganizationalUnit team) {
        List<ProjectTeamMember> memberships = projectTeamMemberRepository.findByProjectIdAndTeamIdOrderByUserFullNameAsc(projectId, team.getId());
        Map<Long, ProjectRole> projectRolesByUser = projectMemberRepository.findByProjectIdOrderByJoinedAtAsc(projectId).stream()
            .collect(LinkedHashMap::new, (map, member) -> map.put(member.getUser().getId(), member.getProjectRole()), Map::putAll);
        return toResponse(projectId, team, memberships, projectRolesByUser);
    }

    private ProjectTeamResponse toResponse(
        Long projectId,
        OrganizationalUnit team,
        List<ProjectTeamMember> memberships,
        Map<Long, ProjectRole> projectRolesByUser
    ) {
        List<ProjectTeamMemberResponse> members = memberships.stream()
            .map((membership) -> new ProjectTeamMemberResponse(
                membership.getUser().getId(),
                membership.getUser().getFullName(),
                membership.getUser().getEmail(),
                projectRolesByUser.getOrDefault(membership.getUser().getId(), ProjectRole.MEMBER).name()
            ))
            .toList();

        long managerCount = members.stream()
            .filter((member) -> member.projectRole().equals(ProjectRole.MANAGER.name()))
            .count();

        return new ProjectTeamResponse(
            team.getId(),
            projectId,
            team.getName(),
            team.getDescription(),
            members,
            members.size(),
            managerCount
        );
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
