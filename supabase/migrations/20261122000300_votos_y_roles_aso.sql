begin;

-- Only server-assigned app metadata is proof of ASO provisioning.
grant update (rol, facultad) on public.perfiles_udice to service_role;
update public.perfiles_udice p
set rol = 'ASO', facultad = u.raw_user_meta_data ->> 'facultad'
from auth.users u
where p.id = u.id and p.rol = 'LECTOR'
  and u.raw_app_meta_data ->> 'udice_role' = 'ASO'
  and u.raw_user_meta_data ->> 'facultad' in (
    'CIENCIAS_VIDA_TECNOLOGIAS','CIENCIAS_SALUD','INGENIERIA_INDUSTRIA_CONSTRUCCION',
    'CIENCIAS_ADMINISTRATIVAS_CONTABLES_COMERCIO','EDUCACION_TURISMO_ARTES_HUMANIDADES',
    'CIENCIAS_SOCIALES_DERECHO_BIENESTAR','BIENESTAR_UNIVERSITARIO','INSTITUTO_IDIOMAS');

alter table public.reacciones alter column tipo_reaccion type text;
alter table public.reacciones enable row level security;
revoke all on public.reacciones from public, anon, authenticated;
grant select on public.reacciones to authenticated;
create policy udice_leer_votos on public.reacciones for select to authenticated using (true);

create function public.udice_guardar_reaccion(aviso uuid, tipo text)
returns void language plpgsql security definer set search_path = '' as $$
declare
    usuario uuid := auth.uid();
begin
    if not exists(select 1 from public.perfiles_udice where id = usuario) then
        raise exception 'Se requiere una sesión activa.';
    end if;
    if tipo is null then
        delete from public.reacciones where publicacion_id = aviso and usuario_id = usuario;
        return;
    end if;
    if tipo not in ('ME_GUSTA','UTIL','IMPORTANTE','INTERESANTE') and
       not exists(select 1 from public.tipos_reaccion_udice where id::text = tipo) then
        raise exception 'Tipo de reacción desconocido.';
    end if;
    if not exists(select 1 from public.publicaciones where id = aviso) then
        raise exception 'Este aviso no está publicado en Supabase.';
    end if;
    insert into public.reacciones (publicacion_id, usuario_id, tipo_reaccion)
    values (aviso, usuario, tipo)
    on conflict (publicacion_id, usuario_id) do update set tipo_reaccion = excluded.tipo_reaccion;
end;
$$;
revoke all on function public.udice_guardar_reaccion(uuid,text) from public, anon;
grant execute on function public.udice_guardar_reaccion(uuid,text) to authenticated;

alter table public.publicaciones enable row level security;
revoke all on public.publicaciones from public, anon, authenticated;
grant select, insert, update, delete on public.publicaciones to authenticated;
create policy udice_leer_avisos on public.publicaciones for select to authenticated using (true);
create policy udice_gestionar_avisos on public.publicaciones for all to authenticated
using (autor_id = (select auth.uid()) and exists (
    select 1 from public.perfiles_udice where id = (select auth.uid()) and rol = 'ASO'))
with check (autor_id = (select auth.uid()) and exists (
    select 1 from public.perfiles_udice where id = (select auth.uid()) and rol = 'ASO'));

notify pgrst, 'reload schema';
commit;
