begin;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('publicaciones-imagenes', 'publicaciones-imagenes', true, 5242880,
        array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do nothing;

create policy udice_imagenes_subir on storage.objects for insert to authenticated
with check (
    bucket_id = 'publicaciones-imagenes'
    and name ~ ('^' || (select auth.uid())::text || '/[0-9a-f-]{36}\.(jpg|png|webp)$')
    and exists (
        select 1 from public.perfiles_udice
        where id = (select auth.uid()) and rol = 'ASO'
    )
);

create policy udice_imagenes_leer_propias on storage.objects for select to authenticated
using (bucket_id = 'publicaciones-imagenes' and owner_id = (select auth.uid())::text);

create policy udice_imagenes_borrar_propias on storage.objects for delete to authenticated
using (bucket_id = 'publicaciones-imagenes' and owner_id = (select auth.uid())::text);

commit;
