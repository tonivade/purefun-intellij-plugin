/*
 * Copyright (c) 2019-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.List;

public interface InstanceService {

  static InstanceService getInstance(Project project) {
    return ServiceManager.getService(project, InstanceService.class);
  }

  List<PsiMethod> processMethod(PsiClass clazz);
}
