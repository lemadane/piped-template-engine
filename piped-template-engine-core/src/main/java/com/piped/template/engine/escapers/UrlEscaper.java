package com.piped.template.engine.escapers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class UrlEscaper {
   public String escape(Object value) {
      if (value == null) {
         return "";
      }

      return URLEncoder.encode(
            String.valueOf(value),
            StandardCharsets.UTF_8);
   }
}