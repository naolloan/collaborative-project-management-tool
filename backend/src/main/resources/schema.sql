alter table if exists projects add column if not exists health varchar(255);

update projects
set health = 'ON_TRACK'
where health is null;
