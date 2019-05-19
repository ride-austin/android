package com.rideaustin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate @Test methods to link with TC by ID
 * Created by Sergey Petrov on 07/08/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TestCases {
    String[] value();
}