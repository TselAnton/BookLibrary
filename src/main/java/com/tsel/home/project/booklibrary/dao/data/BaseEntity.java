package com.tsel.home.project.booklibrary.dao.data;

import java.io.Serializable;

public interface BaseEntity<K extends Serializable> extends Serializable {

    K getId();

    void setId(K id);

    @Deprecated(since = "4.0")
    String getKey();
}
