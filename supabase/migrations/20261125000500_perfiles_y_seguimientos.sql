begin;

create table public.seguimientos (
    seguidor_id uuid not null references public.perfiles_udice(id) on delete cascade,
    publicador_id uuid not null references public.perfiles_udice(id) on delete cascade,
    primary key (seguidor_id, publicador_id),
    check (seguidor_id <> publicador_id)
);
alter table public.seguimientos enable row level security;
revoke all on public.seguimientos from public, anon, authenticated;
grant select on public.seguimientos to authenticated;
create policy udice_leer_seguimientos_propios on public.seguimientos for select to authenticated
    using (seguidor_id = (select auth.uid()));

create function public.udice_seguir_publicador(destino uuid, seguir boolean)
returns boolean language plpgsql security definer set search_path = '' as $$
declare
    usuario uuid := auth.uid();
begin
    if usuario is null or usuario = destino or
       not exists (select 1 from public.perfiles_udice where id = usuario) or
       not exists (select 1 from public.perfiles_udice where id = destino and rol = 'ASO') then
        raise exception 'No se puede seguir a esa cuenta.';
    end if;
    if seguir then
        insert into public.seguimientos values (usuario, destino) on conflict do nothing;
    else
        delete from public.seguimientos where seguidor_id = usuario and publicador_id = destino;
    end if;
    return seguir;
end;
$$;

create function public.udice_buscar_personas(texto text, despues uuid)
returns table (id uuid, nombre text, facultad text, avatar_path text)
language sql stable security definer set search_path = '' as $$
    select u.id, u.nombre, u.facultad, u.avatar_path
    from public.perfiles_udice u
    where auth.uid() is not null and u.rol = 'ASO'
      and (despues is null or u.id > despues)
      and strpos(translate(lower(u.nombre), 'áéíóúüñ', 'aeiouun'),
                 translate(lower(trim(left(coalesce(texto, ''), 120))), 'áéíóúüñ', 'aeiouun')) > 0
    order by u.id limit 20;
$$;

create function public.udice_perfil_publicador(destino uuid)
returns table (id uuid, nombre text, facultad text, avatar_path text, siguiendo boolean, propia boolean)
language sql stable security definer set search_path = '' as $$
    select u.id, u.nombre, u.facultad, u.avatar_path,
           exists (select 1 from public.seguimientos s where s.seguidor_id = auth.uid() and s.publicador_id = u.id),
           u.id = auth.uid()
    from public.perfiles_udice u
    where auth.uid() is not null and u.id = destino and u.rol = 'ASO';
$$;

create function public.udice_avisos_publicador(destino uuid, cursor_fecha timestamptz, cursor_id uuid)
returns table (id uuid, titulo text, categoria text, fecha timestamptz)
language sql stable security invoker set search_path = '' as $$
    select p.id, p.titulo::text, p.categoria::text, p.fecha
    from public.publicaciones p
    where p.autor_id = destino and (cursor_fecha is null or (p.fecha, p.id) < (cursor_fecha, cursor_id))
    order by p.fecha desc, p.id desc limit 20;
$$;

create function public.udice_feed_siguiendo()
returns setof public.publicaciones language sql stable security invoker set search_path = '' as $$
    select p.* from public.publicaciones p
    join public.seguimientos s on s.publicador_id = p.autor_id and s.seguidor_id = auth.uid();
$$;

revoke all on function public.udice_seguir_publicador(uuid,boolean), public.udice_buscar_personas(text,uuid),
    public.udice_perfil_publicador(uuid), public.udice_avisos_publicador(uuid,timestamptz,uuid),
    public.udice_feed_siguiendo() from public, anon;
grant execute on function public.udice_seguir_publicador(uuid,boolean), public.udice_buscar_personas(text,uuid),
    public.udice_perfil_publicador(uuid), public.udice_avisos_publicador(uuid,timestamptz,uuid),
    public.udice_feed_siguiendo() to authenticated;

notify pgrst, 'reload schema';
commit;
