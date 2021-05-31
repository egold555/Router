package org.golde.router.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.golde.router.enums.RequestMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {

	String value();
	RequestMethod method() default RequestMethod.GET;
	
}
