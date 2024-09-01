package com.tsel.home.project.booklibrary.dao.identifier;

import java.io.Serializable;

@FunctionalInterface
public interface IdentifierGenerator<K extends Serializable> {

    K generate();
}
