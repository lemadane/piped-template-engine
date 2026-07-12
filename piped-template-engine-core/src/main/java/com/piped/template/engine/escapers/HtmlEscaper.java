package com.piped.template.engine.escapers;

public final class HtmlEscaper {

   public String escape(final Object value) {
      if (value == null) {
         return "";
      }
      final var text = String.valueOf(value);
      final var escaped = new StringBuilder(text.length());
      for (int index = 0; index < text.length(); index++) {
         final var current = text.charAt(index);
         switch (current) {
            case '&' -> escaped.append("&amp;");
            case '<' -> escaped.append("&lt;");
            case '>' -> escaped.append("&gt;");
            case '"' -> escaped.append("&quot;");
            case '\'' -> escaped.append("&#039;");
            default -> escaped.append(current);
         }
      }
      return escaped.toString();
   }
}