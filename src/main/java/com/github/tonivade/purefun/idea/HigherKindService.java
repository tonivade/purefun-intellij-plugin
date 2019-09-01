package com.github.tonivade.purefun.idea;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;

import java.util.List;

public interface HigherKindService {

  static HigherKindService getInstance(Project project) {
    return ServiceManager.getService(project, HigherKindService.class);
  }

  List<? extends PsiElement> process(PsiClass clazz, Class<? extends PsiElement> type);
}
