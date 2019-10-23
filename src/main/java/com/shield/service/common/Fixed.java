package com.shield.service.common;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD}) //次注解作用于类和字段上
public @interface Fixed {
    boolean value() default true;
}
