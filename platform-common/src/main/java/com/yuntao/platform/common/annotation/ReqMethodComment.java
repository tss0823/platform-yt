package com.yuntao.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@java.lang.annotation.Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.TYPE})
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ReqMethodComment {

    String value() default  "";

    String author() default "admin";

    String ver() default "1.0.0";

    String module() default "user";



}
