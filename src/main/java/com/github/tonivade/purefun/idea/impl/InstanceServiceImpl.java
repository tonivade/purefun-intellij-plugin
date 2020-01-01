/*
 * Copyright (c) 2019-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.idea.impl;

import com.github.tonivade.purefun.idea.InstanceService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.impl.light.LightTypeParameterBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.intellij.psi.PsiSubstitutor.EMPTY;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public class InstanceServiceImpl implements InstanceService {

  private final Logger logger = LoggerFactory.getLogger(InstanceServiceImpl.class);

  private final Project project;

  public InstanceServiceImpl(Project project) {
    this.project = requireNonNull(project);
  }

  @Override
  @NotNull
  public List<PsiMethod> processMethod(@NotNull PsiClass clazz) {
    logger.info("process classes for: {}", clazz.getQualifiedName());
    return singletonList(new InstanceGenerator(project).generateMethod(clazz));
  }
}

class InstanceGenerator {

  public static final String INSTANCE = "instance";

  private final PsiElementFactory factory;
  private final PsiManager psiManager;

  InstanceGenerator(Project project) {
    this.factory = PsiElementFactory.getInstance(project);
    this.psiManager = PsiManager.getInstance(project);
  }

  @NotNull
  PsiMethod generateMethod(PsiClass clazz) {
    LightMethodBuilder instance = createMethod(clazz);
    instance.setMethodReturnType(returnType(clazz, instance.getTypeParameters()));
    return instance;
  }

  @NotNull
  private LightMethodBuilder createMethod(PsiClass clazz) {
    LightMethodBuilder instance = new LightMethodBuilder(psiManager, INSTANCE);
    instance.setContainingClass(clazz);
    instance.addModifier(PsiModifier.PUBLIC);
    instance.addModifier(PsiModifier.STATIC);
    for (int i = 0; i < clazz.getTypeParameters().length ; i++) {
      instance.addTypeParameter(new LightTypeParameterBuilder("A" + i, instance, i));
    }
    return instance;
  }

  @NotNull
  private PsiClassType returnType(PsiClass clazz, PsiTypeParameter[] params) {
    PsiSubstitutor substitutor = EMPTY;
    for (int i = 0; i < clazz.getTypeParameters().length ; i++) {
      substitutor = substitutor.put(clazz.getTypeParameters()[i], EMPTY.substitute(params[i]));
    }
    return factory.createType(clazz, substitutor);
  }
}
