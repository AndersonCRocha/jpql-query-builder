package io.github.andersoncrocha.jpqlquerybuilder.mapper;

import io.github.andersoncrocha.jpqlquerybuilder.annotations.TupleProperty;
import io.github.andersoncrocha.jpqlquerybuilder.utils.ReflectionUtils;
import io.github.andersoncrocha.jpqlquerybuilder.utils.StringUtils;

import javax.lang.model.type.NullType;
import javax.persistence.Tuple;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class AliasToPojoResultMapper {

  private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<>();

  static {
    PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
    PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
    PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
    PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
    PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
    PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
    PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
    PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
    PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
  }

  private AliasToPojoResultMapper() {
    throw new UnsupportedOperationException("Utility class.");
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> wrap(Class<T> c) {
    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }

  public static <T> Function<Tuple, T> from(Class<T> resultType) {
    return tuple -> AliasToPojoResultMapper.getPojoFromTuple(tuple, resultType);
  }

  private static <T> T getPojoFromTuple(Tuple tuple, Class<T> resultType) {
    try {
      Constructor<T> declaredConstructor = resultType.getDeclaredConstructor();
      declaredConstructor.setAccessible(true);
      T pojo = declaredConstructor.newInstance();
      List<Method> setterMethods = ReflectionUtils.getSetterMethods(resultType);

      for (Method setter : setterMethods) {
        setter.setAccessible(true);
        String propertyName = ReflectionUtils.extractPropertyNameByAccessorMethod(setter);
        Class<?> parameterType = ReflectionUtils.extractParameterTypeByAccessorMethod(setter);

        TupleProperty annotation = ReflectionUtils.getAnnotationFromProperty(
          resultType, propertyName, TupleProperty.class
        );

        if (Objects.nonNull(annotation)) {
          if (StringUtils.isNotBlank(annotation.value())) {
            propertyName = annotation.value();
          }

          if (!annotation.type().isAssignableFrom(NullType.class)) {
            parameterType = annotation.type();
          }
        }

        try {
          setter.invoke(pojo, tuple.get(propertyName, wrap(parameterType)));
        } catch (IllegalArgumentException exception) {
          if (!exception.getMessage().startsWith("Unknown alias [")) {
            throw exception;
          }
        }
      }

      return pojo;
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      String exceptionMessage = "Cannot construct an instance from %s class. Exception message: %s";
      throw new RuntimeException(String.format(exceptionMessage, resultType.getName(), e.getMessage()));
    }
  }

}
