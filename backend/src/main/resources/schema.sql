do $$
begin
    if exists (
        select 1
        from information_schema.tables
        where table_schema = 'public'
          and table_name = 'projects'
    ) then
        if not exists (
            select 1
            from information_schema.columns
            where table_schema = 'public'
              and table_name = 'projects'
              and column_name = 'health'
        ) then
            alter table projects add column health varchar(255);
        end if;

        update projects
        set health = 'ON_TRACK'
        where health is null;
    end if;
end
$$;
