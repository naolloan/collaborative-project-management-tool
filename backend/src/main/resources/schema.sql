alter table if exists projects add column if not exists health varchar(255);
alter table if exists projects add column if not exists organizational_unit_id bigint;
alter table if exists tasks add column if not exists sprint_id bigint;
alter table if exists activity_logs add column if not exists project_id bigint;
alter table if exists activity_logs add column if not exists sprint_id bigint;
alter table if exists activity_logs add column if not exists subject_type varchar(255);
alter table if exists activity_logs add column if not exists subject_name varchar(255);
alter table if exists activity_logs alter column task_id drop not null;
alter table if exists projects drop constraint if exists projects_status_check;
alter table if exists activity_logs drop constraint if exists activity_logs_action_type_check;

alter table if exists projects
    add constraint projects_status_check
    check (status in ('PLANNED', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED'));

alter table if exists activity_logs
    add constraint activity_logs_action_type_check
    check (action_type in (
        'PROJECT_CREATED',
        'PROJECT_UPDATED',
        'PROJECT_ARCHIVED',
        'PROJECT_MEMBER_ADDED',
        'SPRINT_CREATED',
        'SPRINT_UPDATED',
        'SPRINT_STATUS_CHANGED',
        'TASK_CREATED',
        'TASK_DELETED',
        'TASK_UPDATED',
        'TASK_ASSIGNED',
        'TASK_STATUS_CHANGED',
        'COMMENT_ADDED'
    ));

create table if not exists project_teams (
    project_id bigint not null,
    team_id bigint not null,
    primary key (project_id, team_id)
);

insert into project_teams (project_id, team_id)
select projects.id, projects.organizational_unit_id
from projects
join organizational_units on organizational_units.id = projects.organizational_unit_id
where projects.organizational_unit_id is not null
  and organizational_units.type = 'TEAM'
on conflict do nothing;

create table if not exists sprints (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    project_id bigint not null,
    name varchar(255) not null,
    goal text,
    start_date date,
    end_date date,
    status varchar(255) not null
);

alter table if exists sprints add column if not exists priority varchar(255);

update projects
set health = 'ON_TRACK'
where health is null;

update sprints
set priority = 'MEDIUM'
where priority is null;

update activity_logs activity
set project_id = tasks.project_id
from tasks
where activity.task_id = tasks.id
  and activity.project_id is null;

update activity_logs activity
set sprint_id = tasks.sprint_id
from tasks
where activity.task_id = tasks.id
  and activity.sprint_id is null;

update activity_logs
set subject_type = 'TASK'
where subject_type is null;

update activity_logs activity
set subject_name = tasks.title
from tasks
where activity.task_id = tasks.id
  and activity.subject_name is null;
