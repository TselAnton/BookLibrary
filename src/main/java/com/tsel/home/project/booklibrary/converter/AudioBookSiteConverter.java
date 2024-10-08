package com.tsel.home.project.booklibrary.converter;

import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dto.AudioBookSiteDTO;

public class AudioBookSiteConverter implements Converter<AudioBookSite, AudioBookSiteDTO> {

    @Override
    public AudioBookSiteDTO convert(AudioBookSite entity) {
        return AudioBookSiteDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .build();
    }
}
