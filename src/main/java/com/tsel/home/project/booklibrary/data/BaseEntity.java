package com.tsel.home.project.booklibrary.data;

import java.io.Serializable;

public interface BaseEntity<K extends Serializable> extends Serializable {

    K getKey();
}
