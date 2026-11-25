begin;

create index seguimientos_publicador_seguidor_idx on public.seguimientos (publicador_id, seguidor_id);

drop function public.udice_buscar_personas(text,uuid);
create function public.udice_buscar_personas(texto text, despues uuid)
returns table (id uuid, nombre text, facultad text, avatar_path text, total_seguidores bigint)
language sql stable security definer set search_path = '' as $$
    select u.id, u.nombre, u.facultad, u.avatar_path,
           (select count(*) from public.seguimientos s where s.publicador_id = u.id)
    from public.perfiles_udice u
    where auth.uid() is not null and u.rol = 'ASO'
      and (despues is null or u.id > despues)
      and strpos(translate(lower(u.nombre), 'áéíóúüñ', 'aeiouun'),
                 translate(lower(trim(left(coalesce(texto, ''), 120))), 'áéíóúüñ', 'aeiouun')) > 0
    order by u.id limit 20;
$$;

drop function public.udice_perfil_publicador(uuid);
create function public.udice_perfil_publicador(destino uuid)
returns table (id uuid, nombre text, facultad text, avatar_path text, siguiendo boolean, propia boolean, total_seguidores bigint)
language sql stable security definer set search_path = '' as $$
    select u.id, u.nombre, u.facultad, u.avatar_path,
           exists (select 1 from public.seguimientos s where s.seguidor_id = auth.uid() and s.publicador_id = u.id),
           u.id = auth.uid(),
           (select count(*) from public.seguimientos s where s.publicador_id = u.id)
    from public.perfiles_udice u
    where auth.uid() is not null and u.id = destino and u.rol = 'ASO';
$$;

create function public.udice_mis_totales_seguimiento()
returns jsonb language sql stable security definer set search_path = '' as $$
    select jsonb_build_object(
        'siguiendo', (select count(*) from public.seguimientos s where s.seguidor_id = (select auth.uid())),
        'seguidores', (select count(*) from public.seguimientos s where s.publicador_id = (select auth.uid()))
    );
$$;

create function public.udice_mis_conexiones(seguidores boolean, despues uuid)
returns table (id uuid, nombre text, avatar_path text)
language sql stable security definer set search_path = '' as $$
    select u.id, u.nombre, u.avatar_path
    from (
        select seguidor_id as id from public.seguimientos
        where seguidores and publicador_id = (select auth.uid())
        union all
        select publicador_id as id from public.seguimientos
        where not seguidores and seguidor_id = (select auth.uid())
    ) conexion join public.perfiles_udice u on u.id = conexion.id
    where despues is null or conexion.id > despues
    order by conexion.id limit 20;
$$;

revoke all on function public.udice_buscar_personas(text,uuid), public.udice_perfil_publicador(uuid),
    public.udice_mis_totales_seguimiento(), public.udice_mis_conexiones(boolean,uuid) from public, anon;
grant execute on function public.udice_buscar_personas(text,uuid), public.udice_perfil_publicador(uuid),
    public.udice_mis_totales_seguimiento(), public.udice_mis_conexiones(boolean,uuid) to authenticated;

notify pgrst, 'reload schema';
commit;
