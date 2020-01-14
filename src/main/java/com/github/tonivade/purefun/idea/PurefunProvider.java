/*
 * Copyright (c) 2019-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.intellij.psi.util.CachedValuesManager.getCachedValue;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class PurefunProvider extends PsiAugmentProvider {

  private static final String HIGHER_KIND = "com.github.tonivade.purefun.HigherKind";
  private static final String INSTANCE = "com.github.tonivade.purefun.Instance";

  @NotNull
  @Override
  protected <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {
    if (element instanceof PsiClass) {
      PsiClass clazz = (PsiClass) element;
      if (clazz.hasAnnotation(HIGHER_KIND)) {
        if (type == PsiClass.class) {
          return (List<Psi>) getCachedValue(clazz, new ClassAbstractCachedValue(clazz));
        } else if (type == PsiMethod.class) {
          return (List<Psi>) getCachedValue(clazz, new MethodAbstractCachedValue(clazz));
        }
      }
      if (clazz.hasAnnotation(INSTANCE)) {
        if (type == PsiMethod.class) {
          return (List<Psi>) getCachedValue(clazz, new MethodInstanceCachedValue(clazz));
        }
      }
    }
    return emptyList();
  }
}

class ClassAbstractCachedValue extends AbstractCachedValue<PsiClass> {
  ClassAbstractCachedValue(PsiClass clazz) {
    super(clazz, PsiClass.class);
  }

  @NotNull
  @Override
  protected Result<List<PsiClass>> process(PsiClass clazz) {
    return Result.create(HigherKindService.getInstance(clazz.getProject()).processClass(clazz), clazz);
  }
}

class MethodInstanceCachedValue extends AbstractCachedValue<PsiMethod> {

  MethodInstanceCachedValue(PsiClass clazz) {
    super(clazz, PsiMethod.class);
  }

  @Override
  protected Result<List<PsiMethod>> process(PsiClass clazz) {
    return Result.create(InstanceService.getInstance(clazz.getProject()).processMethod(clazz), clazz);
  }
}

class MethodAbstractCachedValue extends AbstractCachedValue<PsiMethod> {
  MethodAbstractCachedValue(PsiClass clazz) {
    super(clazz, PsiMethod.class);
  }

  @NotNull
  @Override
  protected Result<List<PsiMethod>> process(PsiClass clazz) {
    return Result.create(HigherKindService.getInstance(clazz.getProject()).processMethod(clazz), clazz);
  }
}

abstract class AbstractCachedValue<P extends PsiElement> implements CachedValueProvider<List<P>> {

  private final PsiClass clazz;
  private final RecursionGuard<PsiClass> recursionGuard;

  AbstractCachedValue(PsiClass clazz, Class<? extends PsiElement> type) {
    this.clazz = requireNonNull(clazz);
    this.recursionGuard = RecursionManager.createGuard("purefun." + type.getName());
  }

  @Nullable
  @Override
  public Result<List<P>> compute() {
    return recursionGuard.doPreventingRecursion(clazz, true, () -> process(clazz));
  }

  protected abstract Result<List<P>> process(PsiClass clazz);

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    if (!(obj instanceof AbstractCachedValue)) {
      return false;
    }
    AbstractCachedValue other = (AbstractCachedValue) obj;
    return Objects.equals(this.clazz, other.clazz);
  }
}
