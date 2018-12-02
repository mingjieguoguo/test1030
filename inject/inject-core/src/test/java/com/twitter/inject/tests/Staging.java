package com.twitter.inject.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * HELPER ANNOTATION FOR TESTS.
 *
 * @note Not to be included in any published test:jar.
 */
@BindingAnnotation
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Staging {
}
