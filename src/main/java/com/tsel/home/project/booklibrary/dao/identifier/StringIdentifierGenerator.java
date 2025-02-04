package com.tsel.home.project.booklibrary.dao.identifier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class StringIdentifierGenerator implements IdentifierGenerator<String> {

    private final String identifier;

    @Override
    public String generate() {
        return identifier;
    }
}
