package com.github.itechbear.googlestylecompletion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupValueWithPriority;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dell on 3/10/16.
 */
public class GoogleStyleSuggestionElement extends LookupElement implements LookupValueWithPriority {
  private final String name;

  public GoogleStyleSuggestionElement(@NotNull final String name) {
    this.name = name;
  }

  @NotNull
  @Override
  public String getLookupString() {
    return name;
  }

  @Override
  public int getPriority() {
    return HIGHER;
  }
}
