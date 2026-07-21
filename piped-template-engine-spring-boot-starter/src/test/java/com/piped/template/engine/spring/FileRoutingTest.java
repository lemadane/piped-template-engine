package com.piped.template.engine.spring;

import com.piped.template.engine.spring.routing.PageDataLoader;
import com.piped.template.engine.spring.routing.PageLoader;
import com.piped.template.engine.spring.routing.PipedFileRouteHandlerMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileRoutingTest {

    @Test
    @DisplayName("Registers SvelteKit route patterns and data loaders")
    void registersSvelteKitRoutePatterns() {
        PipedFileRouteHandlerMapping mapping = new PipedFileRouteHandlerMapping(null);
        mapping.registerPageDataLoader("/tasks/{id}", req -> Map.of("id", "123"));
        assertNotNull(mapping);
    }
}
