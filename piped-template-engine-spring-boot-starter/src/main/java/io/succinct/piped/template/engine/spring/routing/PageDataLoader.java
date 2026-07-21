package io.succinct.piped.template.engine.spring.routing;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@FunctionalInterface
public interface PageDataLoader {
    Map<String, Object> load(HttpServletRequest request);
}
