begin;

alter table public.publicaciones add column video_url text;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('publicaciones-videos', 'publicaciones-videos', true, 20971520, array['video/mp4'])
on conflict (id) do nothing;

create policy udice_videos_subir on storage.objects for insert to authenticated
with check (
    bucket_id = 'publicaciones-videos'
    and name ~ ('^' || (select auth.uid())::text || '/[0-9a-f-]{36}\.mp4$')
    and exists (
        select 1 from public.perfiles_udice where id = (select auth.uid()) and rol = 'ASO'
    )
);

create policy udice_videos_leer_propios on storage.objects for select to authenticated
using (bucket_id = 'publicaciones-videos' and owner_id = (select auth.uid())::text);

create policy udice_videos_borrar_propios on storage.objects for delete to authenticated
using (bucket_id = 'publicaciones-videos' and owner_id = (select auth.uid())::text);

notify pgrst, 'reload schema';
commit;
