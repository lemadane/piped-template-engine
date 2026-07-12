package com.piped.template.engine.exceptions;

public class TemplateSyntaxException extends RuntimeException {
   public TemplateSyntaxException(String message) {
      super(message);
   }

   public TemplateSyntaxException(String message, Throwable cause) {
      super(message, cause);
   }
}