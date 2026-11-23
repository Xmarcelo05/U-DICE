-- Renombra las tablas de la integración; conserva sus filas, permisos y políticas.
begin;

alter table public.udice_profiles rename to perfiles_udice;
alter table public.udice_reaction_types rename to tipos_reaccion_udice;

-- PostgreSQL conserva las dependencias de políticas y triggers al renombrar,
-- pero los cuerpos textuales de estas funciones requieren los nombres nuevos.
create or replace function public.udice_is_admin()
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.perfiles_udice
        where id = (select auth.uid()) and rol = 'ADMIN'
    );
$$;

create or replace function public.udice_on_auth_user()
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
        insert into public.perfiles_udice (id, email, nombre, rol, facultad, carrera)
        values (
            new.id, new.email,
            coalesce(nullif(left(btrim(new.raw_user_meta_data ->> 'nombre'), 120), ''), new.email),
            target_role, faculty, nullif(left(btrim(new.raw_user_meta_data ->> 'carrera'), 120), '')
        );
    end if;
    return new;
end;
$$;

notify pgrst, 'reload schema';

commit;
