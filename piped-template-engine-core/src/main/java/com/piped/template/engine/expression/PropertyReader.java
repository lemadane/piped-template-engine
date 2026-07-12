package com.piped.template.engine.expression;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import com.piped.template.engine.exceptions.TemplateRenderException;
import com.piped.template.engine.exceptions.TemplateSyntaxException;

public final class PropertyReader {
    public Object readPath(String expression, TemplateContext context) {
        final var path = parsePath(expression);

        if (path.rootName().isBlank()) {
            return null;
        }

        Object current = context.get(path.rootName());

        for (final var segment : path.segments()) {
            if (current == null) {
                return null;
            }

            current = readProperty(
                    current,
                    segment.name(),
                    segment.optional());
        }

        return current;
    }

    private Object readProperty(Object source, String propertyName, boolean optional) {
        if (source instanceof Map<?, ?> map) {
            return map.get(propertyName);
        }

        if (source instanceof Map.Entry<?, ?> entry) {
            return readMapEntryProperty(
                    entry,
                    propertyName,
                    optional);
        }

        final var sourceType = source.getClass();

        final var recordAccessor = findZeroArgumentMethod(sourceType, propertyName);

        if (recordAccessor != null) {
            return invokeMethod(
                    source,
                    recordAccessor,
                    propertyName);
        }

        final var getter = findZeroArgumentMethod(
                sourceType,
                "get" + capitalize(propertyName));

        if (getter != null) {
            return invokeMethod(
                    source,
                    getter,
                    propertyName);
        }

        final var booleanGetter = findZeroArgumentMethod(
                sourceType,
                "is" + capitalize(propertyName));

        if (booleanGetter != null) {
            return invokeMethod(
                    source,
                    booleanGetter,
                    propertyName);
        }

        final var field = findPublicField(sourceType, propertyName);

        if (field != null) {
            return readField(
                    source,
                    field,
                    propertyName);
        }

        if (optional) {
            return null;
        }

        throw new TemplateRenderException(
                "Unknown property '" + propertyName + "' on " + sourceType.getName() + ".");
    }

    private Object readMapEntryProperty(
            Map.Entry<?, ?> entry,
            String propertyName,
            boolean optional) {
        return switch (propertyName) {
            case "key" -> entry.getKey();
            case "value" -> entry.getValue();
            default -> {
                if (optional) {
                    yield null;
                }

                throw new TemplateRenderException(
                        "Unknown property '" + propertyName + "' on Map.Entry.");
            }
        };
    }

    private Path parsePath(String expression) {
        final var trimmedExpression = expression.trim();

        if (trimmedExpression.isBlank()) {
            return new Path(
                    "",
                    java.util.List.of());
        }

        final var segments = new ArrayList<PathSegment>();
        final var current = new StringBuilder();

        String rootName = null;
        boolean nextSegmentOptional = false;
        int index = 0;

        while (index < trimmedExpression.length()) {
            final var currentCharacter = trimmedExpression.charAt(index);

            if (currentCharacter == '.') {
                if (current.isEmpty()) {
                    throw new TemplateSyntaxException("Invalid property path: " + expression);
                }

                if (rootName == null) {
                    rootName = current.toString();
                } else {
                    segments.add(new PathSegment(
                            current.toString(),
                            nextSegmentOptional));
                }

                current.setLength(0);
                nextSegmentOptional = false;
                index++;
                continue;
            }

            if (currentCharacter == '?'
                    && index + 1 < trimmedExpression.length()
                    && trimmedExpression.charAt(index + 1) == '.') {
                if (current.isEmpty()) {
                    throw new TemplateSyntaxException("Invalid optional property path: " + expression);
                }

                if (rootName == null) {
                    rootName = current.toString();
                } else {
                    segments.add(new PathSegment(
                            current.toString(),
                            nextSegmentOptional));
                }

                current.setLength(0);
                nextSegmentOptional = true;
                index += 2;
                continue;
            }

            current.append(currentCharacter);
            index++;
        }

        if (current.isEmpty()) {
            throw new TemplateSyntaxException("Invalid property path: " + expression);
        }

        if (rootName == null) {
            rootName = current.toString();
        } else {
            segments.add(new PathSegment(
                    current.toString(),
                    nextSegmentOptional));
        }

        return new Path(
                rootName,
                java.util.List.copyOf(segments));
    }

    private Method findZeroArgumentMethod(Class<?> sourceType, String methodName) {
        try {
            final var method = sourceType.getMethod(methodName);

            if (method.getParameterCount() == 0) {
                return method;
            }

            return null;
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }

    private Field findPublicField(Class<?> sourceType, String fieldName) {
        try {
            return sourceType.getField(fieldName);
        } catch (NoSuchFieldException exception) {
            return null;
        }
    }

    private Object invokeMethod(Object source, Method method, String propertyName) {
        try {
            method.setAccessible(true);
            return method.invoke(source);
        } catch (ReflectiveOperationException exception) {
            throw new TemplateRenderException(
                    "Failed to read property '" + propertyName + "'.",
                    exception);
        }
    }

    private Object readField(Object source, Field field, String propertyName) {
        try {
            field.setAccessible(true);
            return field.get(source);
        } catch (IllegalAccessException exception) {
            throw new TemplateRenderException(
                    "Failed to read field '" + propertyName + "'.",
                    exception);
        }
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        if (value.length() == 1) {
            return value.toUpperCase();
        }

        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private record Path(
            String rootName,
            java.util.List<PathSegment> segments) {
    }

    private record PathSegment(
            String name,
            boolean optional) {
    }
}