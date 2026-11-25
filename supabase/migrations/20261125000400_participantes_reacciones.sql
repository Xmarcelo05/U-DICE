begin;

create function public.udice_detalle_reacciones(
    aviso uuid, incluir_personas boolean, tipo_filtro text, cursor_usuario uuid
)
returns jsonb language sql stable security definer set search_path = ''
as $$
    with catalogo as (
        select * from (values
            ('ME_GUSTA','❤️','Me gusta'), ('UTIL','💡','Útil'),
            ('IMPORTANTE','⚠️','Importante'), ('INTERESANTE','👏','Interesante')
        ) as base(tipo, emoji, label)
        union all
        select id::text, emoji, label from public.tipos_reaccion_udice
    ), visibles as (
        select r.usuario_id, c.tipo, c.emoji, c.label
        from public.publicaciones p
        join public.reacciones r on r.publicacion_id = p.id
        join catalogo c on c.tipo = r.tipo_reaccion
        where auth.uid() is not null and p.id = aviso
          and (p.reacciones_permitidas is null or c.tipo = any(p.reacciones_permitidas))
          and exists (select 1 from public.perfiles_udice perfil where perfil.id = r.usuario_id)
    ), totals as (
        select tipo, emoji, label, count(*) as total from visibles group by tipo, emoji, label
    ), pagina as (
        select v.usuario_id as id, perfil.nombre, perfil.avatar_path, v.emoji
        from visibles v join public.perfiles_udice perfil on perfil.id = v.usuario_id
        where incluir_personas and (tipo_filtro is null or v.tipo = tipo_filtro)
          and (cursor_usuario is null or v.usuario_id > cursor_usuario)
        order by v.usuario_id limit 31
    ), visibles_pagina as (
        select * from pagina order by id limit 30
    )
    select jsonb_build_object(
        'totales', coalesce((select jsonb_agg(to_jsonb(t) order by total desc, tipo) from totals t), '[]'::jsonb),
        'personas', coalesce((select jsonb_agg(to_jsonb(p) order by id) from visibles_pagina p), '[]'::jsonb),
        'siguiente', case when (select count(*) from pagina) > 30
                     then (select id from visibles_pagina order by id desc limit 1) end
    );
$$;

revoke all on function public.udice_detalle_reacciones(uuid,boolean,text,uuid) from public, anon;
grant execute on function public.udice_detalle_reacciones(uuid,boolean,text,uuid) to authenticated;

notify pgrst, 'reload schema';
commit;
