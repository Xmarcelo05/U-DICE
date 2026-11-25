begin;

create function public.udice_buscar_publicaciones(
    texto text, categoria_filtro text, facultad_filtro text, desde timestamptz,
    hasta timestamptz, cursor_fecha timestamptz, cursor_id uuid
)
returns table (id uuid, titulo text, extracto text, categoria text, facultad text, fecha timestamptz)
language sql stable security invoker set search_path = ''
as $$
    select p.id, p.titulo::text, left(p.descripcion, 240), p.categoria::text, p.facultad::text, p.fecha
    from public.publicaciones p
    where (categoria_filtro is null or p.categoria = categoria_filtro)
      and (facultad_filtro is null or p.facultad = facultad_filtro)
      and (desde is null or p.fecha >= desde)
      and (hasta is null or p.fecha < hasta)
      and (cursor_fecha is null or (p.fecha, p.id) < (cursor_fecha, cursor_id))
      and not exists (
          select 1
          from regexp_split_to_table(translate(lower(trim(left(coalesce(texto, ''), 120))), 'áéíóúüñ', 'aeiouun'), '[[:space:]]+') as term(value)
          where strpos(translate(lower(p.titulo || ' ' || p.descripcion), 'áéíóúüñ', 'aeiouun'), term.value) = 0
      )
    order by p.fecha desc, p.id desc
    limit 20;
$$;

revoke all on function public.udice_buscar_publicaciones(text,text,text,timestamptz,timestamptz,timestamptz,uuid) from public, anon;
grant execute on function public.udice_buscar_publicaciones(text,text,text,timestamptz,timestamptz,timestamptz,uuid) to authenticated;

notify pgrst, 'reload schema';
commit;
