package com.github.itechbear.googlestylecompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.ide.actions.CopyReferenceAction;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import com.jetbrains.cidr.lang.OCFileTypeHelpers;
import com.jetbrains.cidr.lang.OCLanguage;
import com.jetbrains.cidr.lang.parser.OCKeywordElementType;
import com.jetbrains.cidr.lang.parser.OCPreprocessorDirectiveElementType;
import com.jetbrains.cidr.lang.psi.OCCppNamespace;
import com.jetbrains.cidr.lang.psi.OCStruct;
import com.jetbrains.cidr.lang.psi.OCTypeElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by dell on 3/10/16.
 */

public class GoogleStyleNameCompletionContributor extends CompletionContributor {
  public GoogleStyleNameCompletionContributor() {
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(LeafPsiElement.class).withLanguage(OCLanguage.getInstance()),
        new VariableCompletionProvider()
    );
  }

  /**
   * Form a variable name from a type.
   * @param type
   * @return
   */
  public static String getVarableFromType(final String type, final boolean isMember) {
    final String[] tokens = type.split("[^\\w_]+");
    String lastToken = null;
    for (int i = tokens.length - 1; i >= 0; --i) {
      if (!tokens[i].isEmpty()) {
        lastToken = tokens[i];
        break;
      }
    }
    if (null == lastToken) {
      return "";
    }
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(Character.toLowerCase(lastToken.charAt(0)));
    for (int i = 1; i < lastToken.length(); ++i) {
      char c = lastToken.charAt(i);
      if (Character.isUpperCase(c)) {
        stringBuilder.append("_").append(Character.toLowerCase(c));
      } else {
        stringBuilder.append(c);
      }
    }
    if (isMember) {
      stringBuilder.append("_");
    }
    return stringBuilder.toString();
  }

  public static void suggestVariableNames(@NotNull final PsiElement parent,
                                          @NotNull final PsiElement prevPrevSibling,
                                          @NotNull CompletionResultSet completionResultSet) {
    final String type = prevPrevSibling.getText();
    if (null == type || type.isEmpty()) {
      return;
    }
    boolean isMember = false;
    final PsiElement grandPa = parent.getParent();
    if (null != grandPa) {
      final PsiElement ancestor = grandPa.getParent();
      if (null != ancestor) {
        isMember = ancestor instanceof OCStruct;
      }
    }
    final String variableName = getVarableFromType(type, isMember);
    LookupElement lookupElement = new GoogleStyleSuggestionElement(variableName);
    completionResultSet.addElement(lookupElement);
  }

  public static void suggestFileGuards(@NotNull final PsiElement self,
                                       @NotNull CompletionResultSet completionResultSet) {
    final PsiFile psiFile = self.getContainingFile();
    if (null == psiFile) {
      return;
    }
    final String relativePath = CopyReferenceAction.elementToFqn(psiFile);
    if (null == relativePath) {
      return;
    }
    if (!OCFileTypeHelpers.isHeaderFile(relativePath)) {
      return;
    }
    final String headerGuard = relativePath.replaceAll("[^\\w_]", "_").toUpperCase() + "_";
    LookupElement lookupElement = new GoogleStyleSuggestionElement(headerGuard);
    completionResultSet.addElement(lookupElement);
  }

  public static int getNamespaceLevel(@NotNull PsiElement parent) {
    int level = 0;
    parent = parent.getParent();
    while (null != parent && parent instanceof OCCppNamespace) {
      ++level;
      parent = parent.getParent();
    }
    return level;
  }

  public static void suggestNamespaces(@NotNull final PsiElement parent,
                                       @NotNull CompletionResultSet completionResultSet) {
    final PsiFile psiFile = parent.getContainingFile();
    if (null == psiFile) {
      return;
    }
    final String relativePath = CopyReferenceAction.elementToFqn(psiFile);
    if (null == relativePath) {
      return;
    }
    final String[] namespaces = Paths.get(relativePath).getParent().toString().split(File.separator);
    if (namespaces.length == 0) {
      return;
    }
    final int level = getNamespaceLevel(parent);
    if (level >= namespaces.length) {
      return;
    }
    LookupElement lookupElement = new GoogleStyleSuggestionElement(namespaces[level]);
    completionResultSet.addElement(lookupElement);
  }

  public static class VariableCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {
      final PsiElement psiElement = completionParameters.getOriginalPosition();
      if (null == psiElement) {
        return;
      }
      final PsiElement parent = psiElement.getParent();
      if (null == parent) {
        return;
      }
      final PsiElement parentPrevSibling = parent.getPrevSibling();
      if (null == parentPrevSibling || !(parentPrevSibling instanceof PsiWhiteSpace)) {
        return;
      }
      final PsiElement parentPrevPrevSibling = parentPrevSibling.getPrevSibling();
      if (null == parentPrevPrevSibling) {
        return;
      }
      if (parentPrevPrevSibling instanceof OCTypeElement) {
        // Variable declaration
        suggestVariableNames(parent, parentPrevPrevSibling, completionResultSet);
      } else if (parentPrevPrevSibling instanceof LeafPsiElement &&
          ((LeafPsiElement) parentPrevPrevSibling).getElementType() instanceof OCPreprocessorDirectiveElementType) {
        // Header guard
        suggestFileGuards(psiElement, completionResultSet);
      }

      final PsiElement prevElement = psiElement.getPrevSibling();
      if (null != prevElement) {
        final PsiElement prevPrevElement = prevElement.getPrevSibling();
        if (null != prevPrevElement && prevPrevElement instanceof LeafPsiElement) {
          final IElementType type = ((LeafPsiElement) prevPrevElement).getElementType();
          if (type instanceof OCKeywordElementType) {
            final String keyword = ((OCKeywordElementType) type).getName();
            if (keyword.equals("namespace")) {
              suggestNamespaces(parent, completionResultSet);
            }
          }
        }
      }
    }
  }
}