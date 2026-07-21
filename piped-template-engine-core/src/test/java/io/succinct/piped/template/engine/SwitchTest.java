package io.succinct.piped.template.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import io.succinct.piped.template.engine.exceptions.TemplateSyntaxException;

class SwitchTest {
   private final TemplateEngine engine = new TemplateEngine();

   @Nested
   @DisplayName("Basic switch")
   class BasicSwitchTest {
      @Test
      @DisplayName("Renders matching case")
      void rendersMatchingCase() {
         final var html = engine.renderString(
               """
                     |switch order.status|
                        |case 'paid'|
                           Paid
                        |case 'pending'|
                           Pending
                        |default|
                           Unknown
                     |/switch|
                     """,
               Map.of(
                     "order", Map.of(
                           "status", "paid")));

         final var output = compact(html);

         assertTrue(output.contains("Paid"));
         assertFalse(output.contains("Pending"));
         assertFalse(output.contains("Unknown"));
      }

      @Test
      @DisplayName("Renders default when no case matches")
      void rendersDefaultWhenNoCaseMatches() {
         final var html = engine.renderString(
               """
                     |switch order.status|
                        |case 'paid'|
                           Paid
                        |case 'pending'|
                           Pending
                        |default|
                           Unknown
                     |/switch|
                     """,
               Map.of(
                     "order", Map.of(
                           "status", "cancelled")));

         final var output = compact(html);

         assertTrue(output.contains("Unknown"));
         assertFalse(output.contains("Paid"));
         assertFalse(output.contains("Pending"));
      }

      @Test
      @DisplayName("Renders nothing when no case matches and default is missing")
      void rendersNothingWhenNoCaseMatchesAndDefaultIsMissing() {
         final var html = engine.renderString(
               """
                     |switch order.status|
                        |case 'paid'|
                           Paid
                        |case 'pending'|
                           Pending
                     |/switch|
                     """,
               Map.of(
                     "order", Map.of(
                           "status", "cancelled")));

         final var output = compact(html);

         assertFalse(output.contains("Paid"));
         assertFalse(output.contains("Pending"));
      }
   }

   @Nested
   @DisplayName("Switch break behavior")
   class SwitchBreakBehaviorTest {
      @Test
      @DisplayName("Does not fall through by default")
      void doesNotFallThroughByDefault() {
         final var html = engine.renderString(
               """
                     |switch role|
                        |case 'admin'|
                           Admin
                        |case 'manager'|
                           Manager
                        |case 'user'|
                           User
                        |default|
                           Guest
                     |/switch|
                     """,
               Map.of("role", "admin"));

         final var output = compact(html);

         assertTrue(output.contains("Admin"));
         assertFalse(output.contains("Manager"));
         assertFalse(output.contains("User"));
         assertFalse(output.contains("Guest"));
      }

      @Test
      @DisplayName("Supports explicit fallthrough")
      void supportsExplicitFallthrough() {
         final var html = engine.renderString(
               """
                     |switch role|
                        |case 'admin'|
                           Admin Dashboard
                           |fallthrough|
                        |case 'manager'|
                           Reports
                           |fallthrough|
                        |case 'user'|
                           Account
                        |default|
                           Login
                     |/switch|
                     """,
               Map.of("role", "admin"));

         final var output = compact(html);

         assertTrue(output.contains("Admin Dashboard"));
         assertTrue(output.contains("Reports"));
         assertTrue(output.contains("Account"));
         assertFalse(output.contains("Login"));
      }

      @Test
      @DisplayName("Fallthrough starts from matching case")
      void fallthroughStartsFromMatchingCase() {
         final var html = engine.renderString(
               """
                     |switch role|
                        |case 'admin'|
                           Admin Dashboard
                           |fallthrough|
                        |case 'manager'|
                           Reports
                           |fallthrough|
                        |case 'user'|
                           Account
                        |default|
                           Login
                     |/switch|
                     """,
               Map.of("role", "manager"));

         final var output = compact(html);

         assertFalse(output.contains("Admin Dashboard"));
         assertTrue(output.contains("Reports"));
         assertTrue(output.contains("Account"));
         assertFalse(output.contains("Login"));
      }
   }

   @Nested
   @DisplayName("Case values")
   class CaseValueTest {
      @Test
      @DisplayName("Matches string case value")
      void matchesStringCaseValue() {
         final var html = engine.renderString(
               """
                     |switch status|
                        |case 'active'|
                           Active
                        |default|
                           Inactive
                     |/switch|
                     """,
               Map.of("status", "active"));

         final var output = compact(html);

         assertTrue(output.contains("Active"));
         assertFalse(output.contains("Inactive"));
      }

      @Test
      @DisplayName("Matches number case value")
      void matchesNumberCaseValue() {
         final var html = engine.renderString(
               """
                     |switch code|
                        |case 200|
                           OK
                        |case 404|
                           Not Found
                        |default|
                           Error
                     |/switch|
                     """,
               Map.of("code", 200));

         final var output = compact(html);

         assertTrue(output.contains("OK"));
         assertFalse(output.contains("Not Found"));
         assertFalse(output.contains("Error"));
      }

      @Test
      @DisplayName("Matches boolean case value")
      void matchesBooleanCaseValue() {
         final var html = engine.renderString(
               """
                     |switch user.active|
                        |case true|
                           Active
                        |case false|
                           Inactive
                     |/switch|
                     """,
               Map.of(
                     "user", Map.of(
                           "active", true)));

         final var output = compact(html);

         assertTrue(output.contains("Active"));
         assertFalse(output.contains("Inactive"));
      }

      @Test
      @DisplayName("Supports case value from model expression")
      void supportsCaseValueFromModelExpression() {
         final var html = engine.renderString(
               """
                     |switch order.status|
                        |case paidStatus|
                           Paid
                        |default|
                           Other
                     |/switch|
                     """,
               Map.of(
                     "paidStatus", "paid",
                     "order", Map.of(
                           "status", "paid")));

         final var output = compact(html);

         assertTrue(output.contains("Paid"));
         assertFalse(output.contains("Other"));
      }
   }

   @Nested
   @DisplayName("Switch with other blocks")
   class SwitchWithOtherBlocksTest {
      @Test
      @DisplayName("Supports if blocks inside case body")
      void supportsIfBlocksInsideCaseBody() {
         final var html = engine.renderString(
               """
                     |switch user.role|
                        |case 'admin'|
                           |if user.active|
                              Active admin
                           |else|
                              Inactive admin
                           |/if|
                        |default|
                           User
                     |/switch|
                     """,
               Map.of(
                     "user", Map.of(
                           "role", "admin",
                           "active", true)));

         final var output = compact(html);

         assertTrue(output.contains("Active admin"));
         assertFalse(output.contains("Inactive admin"));
         assertFalse(output.contains("User"));
      }

      @Test
      @DisplayName("Supports nested switch blocks")
      void supportsNestedSwitchBlocks() {
         final var html = engine.renderString(
               """
                     |switch user.role|
                        |case 'admin'|
                           |switch user.status|
                              |case 'active'|
                                 Active admin
                              |default|
                                 Inactive admin
                           |/switch|
                        |default|
                           User
                     |/switch|
                     """,
               Map.of(
                     "user", Map.of(
                           "role", "admin",
                           "status", "active")));

         final var output = compact(html);

         assertTrue(output.contains("Active admin"));
         assertFalse(output.contains("Inactive admin"));
         assertFalse(output.contains("User"));
      }
   }

   @Nested
   @DisplayName("Switch syntax errors")
   class SwitchSyntaxErrorTest {
      @Test
      @DisplayName("Throws when switch block is not closed")
      void throwsWhenSwitchBlockIsNotClosed() {
         assertThrows(
               TemplateSyntaxException.class,
               () -> engine.renderString(
                     """
                           |switch role|
                              |case 'admin'|
                                 Admin
                           """,
                     Map.of("role", "admin")));
      }

      @Test
      @DisplayName("Throws when /switch appears without matching switch")
      void throwsWhenClosingSwitchAppearsWithoutMatchingSwitch() {
         assertThrows(
               TemplateSyntaxException.class,
               () -> engine.renderString(
                     """
                           |/switch|
                           """,
                     Map.of()));
      }

      @Test
      @DisplayName("Throws when case appears without matching switch")
      void throwsWhenCaseAppearsWithoutMatchingSwitch() {
         assertThrows(
               TemplateSyntaxException.class,
               () -> engine.renderString(
                     """
                           |case 'admin'|
                              Admin
                           """,
                     Map.of()));
      }

      @Test
      @DisplayName("Throws when default appears without matching switch")
      void throwsWhenDefaultAppearsWithoutMatchingSwitch() {
         assertThrows(
               TemplateSyntaxException.class,
               () -> engine.renderString(
                     """
                           |default|
                              Default
                           """,
                     Map.of()));
      }
   }

   private static String compact(String value) {
      return value
            .replaceAll("\\s+", " ")
            .trim();
   }
}