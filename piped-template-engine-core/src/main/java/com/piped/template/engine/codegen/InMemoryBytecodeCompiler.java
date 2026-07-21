package com.piped.template.engine.codegen;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

public final class InMemoryBytecodeCompiler {

    public static boolean isAvailable() {
        return ToolProvider.getSystemJavaCompiler() != null;
    }

    public Class<?> compile(String className, String javaSource) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JavaCompiler is not available in current JRE/JDK environment.");
        }

        String fullClassName = "com.piped.template.engine.codegen.generated." + className;
        JavaFileObject fileObject = new StringJavaFileObject(fullClassName, javaSource);

        Map<String, ByteArrayOutputStream> byteCodeMap = new HashMap<>();
        MemoryJavaFileManager fileManager = new MemoryJavaFileManager(
                compiler.getStandardFileManager(null, null, null), byteCodeMap);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, null, null, Collections.singletonList(fileObject));

        boolean success = task.call();
        if (!success) {
            StringBuilder errorMsg = new StringBuilder("Compilation failed for " + fullClassName + ":\n");
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                errorMsg.append(diagnostic.toString()).append("\n");
            }
            throw new IllegalStateException(errorMsg.toString());
        }

        MemoryClassLoader classLoader = new MemoryClassLoader(byteCodeMap, getClass().getClassLoader());
        return classLoader.loadClass(fullClassName);
    }

    private static class StringJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        StringJavaFileObject(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private static class MemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, ByteArrayOutputStream> byteCodeMap;

        MemoryJavaFileManager(StandardJavaFileManager fileManager, Map<String, ByteArrayOutputStream> byteCodeMap) {
            super(fileManager);
            this.byteCodeMap = byteCodeMap;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byteCodeMap.put(className, baos);
            return new SimpleJavaFileObject(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind) {
                @Override
                public OutputStream openOutputStream() {
                    return baos;
                }
            };
        }
    }

    private static class MemoryClassLoader extends ClassLoader {
        private final Map<String, ByteArrayOutputStream> byteCodeMap;

        MemoryClassLoader(Map<String, ByteArrayOutputStream> byteCodeMap, ClassLoader parent) {
            super(parent);
            this.byteCodeMap = byteCodeMap;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            ByteArrayOutputStream baos = byteCodeMap.get(name);
            if (baos == null) {
                return super.findClass(name);
            }
            byte[] bytes = baos.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
