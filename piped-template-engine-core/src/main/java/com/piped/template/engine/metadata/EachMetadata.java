package com.piped.template.engine.metadata;

public record EachMetadata(
      int index,
      int index0,
      boolean first,
      boolean last,
      boolean even,
      boolean odd) {
   public static EachMetadata of(int index0, int total) {
      final var index = index0 + 1;
      return new EachMetadata(
            index,
            index0,
            index0 == 0,
            index0 == total - 1,
            index % 2 == 0,
            index % 2 != 0);
   }
}