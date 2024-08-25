package com.tsel.home.project.booklibrary.converter;

public interface Converter<E, D> {

    D convert(E entity);

    @Deprecated(since = "4.0")
    String buildEntityKeyByDTO(D dto);
}
