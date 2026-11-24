begin;

alter table public.publicaciones add column reacciones_permitidas text[];

create function public.udice_validar_reacciones_permitidas()
returns trigger language plpgsql set search_path = '' as $$
begin
    if new.reacciones_permitidas is not null and (
        cardinality(new.reacciones_permitidas) = 0
        or cardinality(new.reacciones_permitidas) <> (
            select count(distinct id) from unnest(new.reacciones_permitidas) as ids(id)
        )
        or exists (
            select 1 from unnest(new.reacciones_permitidas) as ids(id)
            where id not in ('ME_GUSTA','UTIL','IMPORTANTE','INTERESANTE')
              and not exists(select 1 from public.tipos_reaccion_udice where tipos_reaccion_udice.id::text = ids.id)
        )
    ) then
        raise exception 'Elige uno o más tipos de reacción válidos, sin repetirlos.';
    end if;
    return new;
end;
$$;

create trigger udice_validar_reacciones_permitidas
before insert or update of reacciones_permitidas on public.publicaciones
for each row execute function public.udice_validar_reacciones_permitidas();

create or replace function public.udice_guardar_reaccion(aviso uuid, tipo text)
returns void language plpgsql security definer set search_path = '' as $$
declare
    usuario uuid := auth.uid();
    opciones text[];
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
    select reacciones_permitidas into opciones from public.publicaciones where id = aviso for share;
    if not found then
        raise exception 'Este aviso no está publicado en Supabase.';
    end if;
    if opciones is not null and not (tipo = any(opciones)) then
        raise exception 'Esta reacción no está habilitada para el aviso.';
    end if;
    insert into public.reacciones (publicacion_id, usuario_id, tipo_reaccion)
    values (aviso, usuario, tipo)
    on conflict (publicacion_id, usuario_id) do update set tipo_reaccion = excluded.tipo_reaccion;
end;
$$;

notify pgrst, 'reload schema';
commit;
