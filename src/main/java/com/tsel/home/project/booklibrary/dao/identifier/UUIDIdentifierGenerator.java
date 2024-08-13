package com.tsel.home.project.booklibrary.dao.identifier;

import java.util.UUID;

public class UUIDIdentifierGenerator implements IdentifierGenerator<UUID> {

    public static final UUIDIdentifierGenerator INSTANCE = new UUIDIdentifierGenerator();

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
