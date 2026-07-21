package io.succinct.piped.template.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttemptRecoverTest {
    private final TemplateEngine engine = new TemplateEngine();

    @Test
    @DisplayName("Renders attempt block normally if no error occurs")
    void rendersAttemptNormally() {
        String template = """
            |attempt|
                <p>Hello World</p>
            |recover|
                <p>Error fallback</p>
            |/attempt|
            """;

        String html = engine.renderString(template, Map.of());
        assertTrue(html.contains("Hello World"));
        assertFalse(html.contains("Error fallback"));
    }

    @Test
    @DisplayName("Rolls back partial output and renders recover block when exception occurs")
    void rollsBackAndRecovers() {
        String template = """
            <div>
                |attempt|
                    <p>Partial text before error</p>
                    |1 / 0|
                |recover as err|
                    <div class="error">Failed: |err|</div>
                |/attempt|
            </div>
            """;

        String html = engine.renderString(template, Map.of());
        assertFalse(html.contains("Partial text before error"));
        assertTrue(html.contains("Failed:"));
        assertTrue(html.contains("/ by zero") || html.contains("by zero") || html.contains("ArithmeticException"));
    }
}
