alter table if exists projects add column if not exists health varchar(255);
alter table if exists projects add column if not exists organizational_unit_id bigint;
alter table if exists tasks add column if not exists sprint_id bigint;

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

update projects
set health = 'ON_TRACK'
where health is null;
