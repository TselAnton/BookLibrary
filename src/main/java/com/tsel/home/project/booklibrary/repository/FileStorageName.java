package com.tsel.home.project.booklibrary.repository;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FileStorageName {

    String value();
}
