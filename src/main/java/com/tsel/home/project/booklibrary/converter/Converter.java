package com.tsel.home.project.booklibrary.converter;

public interface Converter<E, D> {

    D convert(E entity);

    String buildEntityKeyByDTO(D dto);
}
