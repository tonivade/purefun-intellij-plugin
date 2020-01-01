/*
 * Copyright (c) 2019-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import java.util.List;

public interface HigherKindService {

  static HigherKindService getInstance(Project project) {
    return ServiceManager.getService(project, HigherKindService.class);
  }

  List<PsiMethod> processMethod(PsiClass clazz);
  List<PsiClass> processClass(PsiClass clazz);
}
