begin;

-- Estas tablas heredadas ya no forman parte de la API de la app.
-- Conserva sus datos y restringe el acceso de los clientes.
alter table if exists public.admin_users enable row level security;
alter table if exists public.facultades_cuentas enable row level security;
alter table if exists public.usuarios enable row level security;
alter table if exists public.preferencias_usuarios enable row level security;

do $$
declare
    tabla text;
begin
    foreach tabla in array array['admin_users', 'facultades_cuentas', 'usuarios', 'preferencias_usuarios']
    loop
        if to_regclass(format('public.%I', tabla)) is not null then
            execute format('revoke all privileges on table public.%I from public, anon, authenticated', tabla);
        end if;
    end loop;
end;
$$;

commit;
