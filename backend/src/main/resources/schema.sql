alter table if exists projects add column if not exists health varchar(255);
alter table if exists projects add column if not exists organizational_unit_id bigint;
alter table if exists tasks add column if not exists sprint_id bigint;
alter table if exists activity_logs add column if not exists project_id bigint;
alter table if exists activity_logs add column if not exists sprint_id bigint;
alter table if exists activity_logs add column if not exists subject_type varchar(255);
alter table if exists activity_logs add column if not exists subject_name varchar(255);

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
