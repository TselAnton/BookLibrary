package com.tsel.home.project.booklibrary.dao.schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Имена и прежние имена задаются стандартной аннотацией Gson SerializedName. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StorageField {

    String defaultJson() default "";

    Class<? extends DefaultValueProvider> provider() default NoDefaultValue.class;

    NullPolicy onNull() default NullPolicy.KEEP;
}
