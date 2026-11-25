begin;

-- PostgREST solicita este campo calculado junto a cada aviso.
create function public.udice_avatar_autor(aviso public.publicaciones)
returns text
language sql stable security definer set search_path = ''
as $$
    select perfil.avatar_path
    from public.publicaciones p
    join public.perfiles_udice perfil on perfil.id = p.autor_id
    where p.id = aviso.id;
$$;

revoke all on function public.udice_avatar_autor(public.publicaciones) from public, anon;
grant execute on function public.udice_avatar_autor(public.publicaciones) to authenticated;

notify pgrst, 'reload schema';
commit;
