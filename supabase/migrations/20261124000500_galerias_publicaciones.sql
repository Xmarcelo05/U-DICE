begin;

alter table public.publicaciones add column additional_image_urls text[];
alter table public.publicaciones add constraint publicaciones_hasta_cuatro_imagenes check (
    additional_image_urls is null or cardinality(additional_image_urls) = 0 or (
        cardinality(additional_image_urls) <= 3 and
        array_ndims(additional_image_urls) = 1 and
        array_position(additional_image_urls, null) is null and
        array_position(additional_image_urls, '') is null and
        image_url is not null and image_url <> '' and video_url is null
    )
);

notify pgrst, 'reload schema';
commit;
