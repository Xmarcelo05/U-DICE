begin;

alter table public.perfiles_udice add column avatar_path text;
alter table public.perfiles_udice add constraint perfiles_avatar_carpeta_propia
    check (avatar_path is null or avatar_path ~ ('^' || id::text || '/[0-9a-f-]{36}\.(jpg|png|webp)$'));

grant update (avatar_path) on public.perfiles_udice to authenticated;
create policy udice_editar_avatar_propio on public.perfiles_udice for update to authenticated
using (id = (select auth.uid())) with check (id = (select auth.uid()));

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('perfiles-imagenes', 'perfiles-imagenes', true, 5242880,
        array['image/jpeg', 'image/png', 'image/webp']);

create policy udice_avatar_subir on storage.objects for insert to authenticated
with check (
    bucket_id = 'perfiles-imagenes'
    and name ~ ('^' || (select auth.uid())::text || '/[0-9a-f-]{36}\.(jpg|png|webp)$')
);
create policy udice_avatar_listar_propio on storage.objects for select to authenticated
using (bucket_id = 'perfiles-imagenes' and owner_id = (select auth.uid())::text);
create policy udice_avatar_borrar_propio on storage.objects for delete to authenticated
using (bucket_id = 'perfiles-imagenes' and owner_id = (select auth.uid())::text);

notify pgrst, 'reload schema';
commit;
