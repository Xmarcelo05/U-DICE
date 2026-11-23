begin;

create table public.udice_profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text not null,
    nombre text not null,
    rol text not null default 'LECTOR' check (rol in ('LECTOR', 'ASO', 'ADMIN')),
    facultad text check (facultad in (
        'CIENCIAS_VIDA_TECNOLOGIAS', 'CIENCIAS_SALUD', 'INGENIERIA_INDUSTRIA_CONSTRUCCION',
        'CIENCIAS_ADMINISTRATIVAS_CONTABLES_COMERCIO', 'EDUCACION_TURISMO_ARTES_HUMANIDADES',
        'CIENCIAS_SOCIALES_DERECHO_BIENESTAR', 'BIENESTAR_UNIVERSITARIO', 'INSTITUTO_IDIOMAS'
    )),
    carrera text,
    check (rol <> 'ASO' or facultad is not null)
);

alter table public.udice_profiles enable row level security;
revoke all on public.udice_profiles from public, anon, authenticated;
grant select on public.udice_profiles to authenticated;
grant select on public.udice_profiles to service_role;

create function public.udice_is_admin()
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.udice_profiles
        where id = (select auth.uid()) and rol = 'ADMIN'
    );
$$;

revoke all on function public.udice_is_admin() from public, anon;
grant execute on function public.udice_is_admin() to authenticated;

create policy udice_profiles_read on public.udice_profiles
    for select to authenticated using (
        id = (select auth.uid()) or (rol = 'ASO' and (select public.udice_is_admin()))
    );

create function public.udice_on_auth_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    faculty text := upper(left(btrim(new.raw_user_meta_data ->> 'facultad'), 100));
    target_role text := 'LECTOR';
begin
    if faculty is not null and faculty not in (
        'CIENCIAS_VIDA_TECNOLOGIAS', 'CIENCIAS_SALUD', 'INGENIERIA_INDUSTRIA_CONSTRUCCION',
        'CIENCIAS_ADMINISTRATIVAS_CONTABLES_COMERCIO', 'EDUCACION_TURISMO_ARTES_HUMANIDADES',
        'CIENCIAS_SOCIALES_DERECHO_BIENESTAR', 'BIENESTAR_UNIVERSITARIO', 'INSTITUTO_IDIOMAS'
    ) then
        faculty := null;
    end if;

    -- App metadata is written by the Auth Admin API. Public signup metadata cannot assign roles.
    if new.raw_app_meta_data ->> 'udice_role' = 'ASO' and faculty is not null then
        target_role := 'ASO';
    end if;

    if new.email is not null then
        insert into public.udice_profiles (id, email, nombre, rol, facultad, carrera)
        values (
            new.id, new.email,
            coalesce(nullif(left(btrim(new.raw_user_meta_data ->> 'nombre'), 120), ''), new.email),
            target_role, faculty, nullif(left(btrim(new.raw_user_meta_data ->> 'carrera'), 120), '')
        );
    end if;
    return new;
end;
$$;

revoke all on function public.udice_on_auth_user() from public, anon, authenticated;
create trigger udice_profile_on_signup
    after insert on auth.users
    for each row execute function public.udice_on_auth_user();

-- Existing Auth users are provisioned as readers. Assign privileged roles explicitly.
insert into public.udice_profiles (id, email, nombre)
select id, email, coalesce(nullif(left(btrim(raw_user_meta_data ->> 'nombre'), 120), ''), email)
from auth.users where email is not null
on conflict (id) do nothing;

create table public.udice_reaction_types (
    id uuid primary key default gen_random_uuid(),
    label text not null check (label = btrim(label) and char_length(label) between 1 and 30),
    emoji text not null unique check (emoji = btrim(emoji) and char_length(emoji) between 1 and 20),
    created_at timestamptz not null default now(),
    check (lower(label) not in ('me gusta', 'útil', 'importante', 'interesante')),
    check (emoji not in ('❤️', '💡', '⚠️', '👏'))
);

create unique index udice_reaction_label_unique on public.udice_reaction_types (lower(label));
alter table public.udice_reaction_types enable row level security;
revoke all on public.udice_reaction_types from public, anon, authenticated;
grant select on public.udice_reaction_types to authenticated;
grant insert (label, emoji) on public.udice_reaction_types to authenticated;

create policy udice_reaction_types_read on public.udice_reaction_types
    for select to authenticated using (
        exists (select 1 from public.udice_profiles p where p.id = (select auth.uid()))
    );

create policy udice_reaction_types_create on public.udice_reaction_types
    for insert to authenticated with check ((select public.udice_is_admin()));

-- Legacy tables can contain password hashes. The app uses Auth for credentials.
do $$
begin
    if to_regclass('public.admin_users') is not null then
        execute 'revoke all on public.admin_users from public, anon, authenticated';
    end if;
    if to_regclass('public.facultades_cuentas') is not null then
        execute 'revoke all on public.facultades_cuentas from public, anon, authenticated';
    end if;
end;
$$;

commit;
