package com.github.itechbear.googlestylecompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.cidr.lang.OCLanguage;
import com.jetbrains.cidr.lang.psi.OCTypeElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dell on 3/10/16.
 */

public class GoogleStyleVariableNameCompletionContributor extends CompletionContributor {
  public GoogleStyleVariableNameCompletionContributor() {
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(LeafPsiElement.class).withLanguage(OCLanguage.getInstance()),
        new VariableCompletionProvider()
    );
  }

  public static class VariableCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
      PsiElement psiElement = completionParameters.getOriginalPosition();
      if (null == psiElement) {
        return;
      }
      PsiElement parent = psiElement.getParent();
      if (null == parent) {
        return;
      }
      PsiElement prevSibling = parent.getPrevSibling();
      if (null == prevSibling || !(prevSibling instanceof PsiWhiteSpace)) {
        return;
      }
      PsiElement prevPrevSibling = prevSibling.getPrevSibling();
      if (null == prevPrevSibling || !(prevPrevSibling instanceof OCTypeElement)) {
        return;
      }
      String typeText = prevPrevSibling.getText();
      if (null == typeText || typeText.isEmpty()) {
        return;
      }
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(Character.toLowerCase(typeText.charAt(0)));
      for (int i = 1; i < typeText.length(); ++i) {
        char c = typeText.charAt(i);
        if (Character.isUpperCase(c)) {
          stringBuilder.append("_").append(Character.toLowerCase(c));
        } else {
          stringBuilder.append(c);
        }
      }
      LookupElement lookupElement = new GoogleStyleVariableNameElement(stringBuilder.toString());
      completionResultSet.addElement(lookupElement);
    }
  }
}