package io.github.andersoncrocha.jpqlquerybuilder.annotations;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TupleProperty {

  String value() default "";
  Class<?> type() default NullType.class;

}
