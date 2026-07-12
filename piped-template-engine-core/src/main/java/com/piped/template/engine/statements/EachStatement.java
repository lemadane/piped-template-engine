package com.piped.template.engine.statements;

public record EachStatement(
      String itemName,
      String keyName,
      String valueName,
      String collectionExpression,
      boolean mapLoop) {
}