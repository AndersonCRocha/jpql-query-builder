package io.github.andersoncrocha.jpqlquerybuilder.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionUtils {

  private ReflectionUtils() {
    throw new UnsupportedOperationException("Utility class.");
  }

  public static boolean isGetterMethod(Method method) {
    String methodName = method.getName();
    return methodName.startsWith("get") || methodName.startsWith("is");
  }

  public static boolean isSetterMethod(Method method) {
    return method.getName().startsWith("set");
  }

  public static boolean isAccessorMethod(Method method) {
    return isGetterMethod(method) || isSetterMethod(method);
  }

  public static String extractPropertyNameByAccessorMethod(Method method) {
    validateIfAccessorMethod(method);
    String propertyName = method.getName().replaceAll("^set|get|is", "");
    return StringUtils.uncapitalize(propertyName);
  }

  public static Class<?> extractParameterTypeByAccessorMethod(Method method) {
    validateIfAccessorMethod(method);

    if (isGetterMethod(method)) {
      return method.getReturnType();
    }

    return method.getParameterTypes()[0];
  }

  public static List<Method> getSetterMethods(Class<?> clazz) {
    Method[] declaredMethods = clazz.getDeclaredMethods();
    return Arrays.stream(declaredMethods)
      .filter(ReflectionUtils::isSetterMethod)
      .collect(Collectors.toList());
  }

  public static List<Method> getGetterMethods(Class<?> clazz) {
    Method[] declaredMethods = clazz.getDeclaredMethods();
    return Arrays.stream(declaredMethods)
      .filter(ReflectionUtils::isGetterMethod)
      .collect(Collectors.toList());
  }

  public static Method getGetterByPropertyName(Class<?> clazz, String propertyName) {
    return ReflectionUtils.getGetterMethods(clazz)
      .stream()
      .filter(method -> ReflectionUtils.extractPropertyNameByAccessorMethod(method).equals(propertyName))
      .findFirst()
      .orElseThrow(
        () -> new IllegalArgumentException(
          String.format("Getter method not found for property name '%s'", propertyName)
        )
      );
  }

  private static void validateIfAccessorMethod(Method method) {
    String methodName = method.getName();
    if (!isAccessorMethod(method)) {
      String exceptionMessage = "Method '%s' is not an accessor method (getter | setter | is)";
      throw new IllegalArgumentException(String.format(exceptionMessage, methodName));
    }
  }

  public static <T extends Annotation> T getAnnotationFromProperty(
    Class<?> pojoClass, String propertyName, Class<T> annotationClass
  ) {
    Field field = Arrays.stream(pojoClass.getDeclaredFields())
      .filter(declaredField -> declaredField.getName().equals(propertyName))
      .findFirst()
      .orElseThrow(
        () -> new IllegalArgumentException(
          String.format("Property '%s' not found in class '%s'", propertyName, pojoClass)
        )
      );

    Optional<T> fieldAnnotationOptional = Optional.ofNullable(field.getAnnotation(annotationClass));

    return fieldAnnotationOptional.orElseGet(() ->
      Stream.concat(getGetterMethods(pojoClass).stream(), getSetterMethods(pojoClass).stream())
        .filter(method -> ReflectionUtils.extractPropertyNameByAccessorMethod(method).equals(propertyName))
        .filter(method -> method.isAnnotationPresent(annotationClass))
        .findFirst()
        .map(method -> method.getAnnotation(annotationClass))
        .orElse(null)
    );
  }

}
