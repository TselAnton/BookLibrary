package com.tsel.home.project.booklibrary.dao.identifier;

import java.util.UUID;

public final class UUIDIdentifierGenerator implements IdentifierGenerator<UUID> {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
