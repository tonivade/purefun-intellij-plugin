/*
 * Copyright (c) 2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.idea;

import com.intellij.openapi.util.RecursionGuard;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class HigherKindProvider extends PsiAugmentProvider {

  private static final String HIGHER_KIND = "com.github.tonivade.purefun.HigherKind";

  @NotNull
  @Override
  protected <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {
    if (element instanceof PsiClass) {
      PsiClass clazz = (PsiClass) element;
      if (clazz.hasAnnotation(HIGHER_KIND)) {
        if (type == PsiClass.class) {
          return (List<Psi>) CachedValuesManager.getCachedValue(clazz, new ClassHigherKindCachedValue(clazz));
        }
        if (type == PsiMethod.class) {
          return (List<Psi>) CachedValuesManager.getCachedValue(clazz, new MethodHigherKindCachedValue(clazz));
        }
      }
    }
    return Collections.emptyList();
  }
}

class ClassHigherKindCachedValue extends HigherKindCachedValue {
  ClassHigherKindCachedValue(PsiClass clazz) {
    super(clazz, PsiClass.class);
  }
}

class MethodHigherKindCachedValue extends HigherKindCachedValue {
  MethodHigherKindCachedValue(PsiClass clazz) {
    super(clazz, PsiMethod.class);
  }
}

class HigherKindCachedValue implements CachedValueProvider<List<? extends PsiElement>> {

  private final PsiClass clazz;
  private final Class<? extends PsiElement> type;
  private final RecursionGuard<PsiClass> recursionGuard;

  HigherKindCachedValue(PsiClass clazz, Class<? extends PsiElement> type) {
    this.clazz = requireNonNull(clazz);
    this.type = requireNonNull(type);
    this.recursionGuard = RecursionManager.createGuard("purefun." + type.getName());
  }

  @Override
  public Result<List<? extends PsiElement>> compute() {
    return recursionGuard.doPreventingRecursion(clazz, true, this::process);
  }

  public Result<List<? extends PsiElement>> process() {
    return Result.create(HigherKindService.getInstance(clazz.getProject()).process(clazz, type), clazz);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    if (!(obj instanceof HigherKindCachedValue)) {
      return false;
    }
    HigherKindCachedValue other = (HigherKindCachedValue) obj;
    return Objects.equals(this.clazz, other.clazz);
  }
}
