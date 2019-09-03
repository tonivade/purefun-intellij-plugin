/*
 * Copyright (c) 2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purefun.idea.impl;

import com.github.tonivade.purefun.idea.HigherKindService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.impl.file.impl.JavaFileManager;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.impl.light.LightPsiClassBuilder;
import com.intellij.psi.impl.light.LightTypeParameterBuilder;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.intellij.psi.PsiSubstitutor.EMPTY;
import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public class HigherKindServiceImpl implements HigherKindService {

  private final Logger logger = LoggerFactory.getLogger(HigherKindServiceImpl.class);

  private final Project project;

  public HigherKindServiceImpl(Project project) {
    this.project = requireNonNull(project);
  }

  @NotNull
  @Override
  public List<PsiClass> processClass(PsiClass clazz) {
    logger.info("process classes for: {}", clazz.getQualifiedName());
    PsiTypeParameter[] typeParameters = clazz.getTypeParameters();
    if (typeParameters.length > 0) {
      return newGenerator().generateWitness(clazz);
    }
    return emptyList();
  }

  @NotNull
  @Override
  public List<PsiMethod> processMethod(PsiClass clazz) {
    logger.info("process methods for: {}", clazz.getQualifiedName());
    PsiTypeParameter[] typeParameters = clazz.getTypeParameters();
    if (typeParameters.length == 1) {
      return newGenerator().generateHigher1Methods(clazz);
    }
    if (typeParameters.length == 2) {
      return newGenerator().generateHigher2Methods(clazz);
    }
    if (typeParameters.length == 3) {
      return newGenerator().generateHigher3Methods(clazz);
    }
    return emptyList();
  }

  @NotNull
  private HigherKindGenerator newGenerator() {
    return new HigherKindGenerator(project);
  }
}

class HigherKindGenerator {

  private static final String KIND = "com.github.tonivade.purefun.Kind";
  private static final String HIGHER1 = "com.github.tonivade.purefun.Higher1";
  private static final String HIGHER2 = "com.github.tonivade.purefun.Higher2";
  private static final String HIGHER3 = "com.github.tonivade.purefun.Higher3";
  private static final String KIND1 = "kind1";
  private static final String KIND2 = "kind2";
  private static final String KIND3 = "kind3";
  private static final String NARROW_KIND = "narrowK";
  private static final String HIGHER_KIND_TYPE = "hkt";
  private static final String WITNESS = "µ";

  private final PsiElementFactory factory;
  private final PsiManager psiManager;
  private final JavaFileManager fileManager;
  private final GlobalSearchScope searchScope;

  HigherKindGenerator(Project project) {
    this.factory = PsiElementFactory.getInstance(project);
    this.psiManager = PsiManager.getInstance(project);
    this.fileManager = JavaFileManager.getInstance(project);
    this.searchScope = allScope(project);
  }

  @NotNull
  List<PsiMethod> generateHigher1Methods(PsiClass clazz) {
    return asList(
        generateNarrowK1(clazz),
        generateKind1(clazz)
    );
  }

  @NotNull
  List<PsiMethod> generateHigher2Methods(PsiClass clazz) {
    return asList(
        generateNarrowK2(clazz),
        generateNarrowK2Of1(clazz),
        generateKind2(clazz),
        generateKind1Of2(clazz)
    );
  }

  @NotNull
  List<PsiMethod> generateHigher3Methods(PsiClass clazz) {
    return asList(
        generateNarrowK3(clazz),
        generateNarrowK3Of2(clazz),
        generateNarrowK3Of1(clazz),
        generateKind3(clazz),
        generateKind1Of3(clazz),
        generateKind2Of3(clazz)
    );
  }

  @NotNull
  List<PsiClass> generateWitness(PsiClass clazz) {
    LightPsiClassBuilder witness = new LightPsiClassBuilder(clazz, WITNESS);
    witness.setContainingClass(clazz);
    witness.getModifierList().addModifier(PsiModifier.PUBLIC);
    witness.getModifierList().addModifier(PsiModifier.STATIC);
    witness.getModifierList().addModifier(PsiModifier.FINAL);
    witness.getImplementsList().addReference(KIND);
    return singletonList(witness);
  }

  @NotNull
  private PsiMethod generateNarrowK1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK1(clazz);
    narrowK.setMethodReturnType(returnType1(clazz, narrowK.getTypeParameters()));
    narrowK.addParameter(HIGHER_KIND_TYPE, higher1Of(clazz, narrowK.getTypeParameters()));
    return narrowK;
  }

  @NotNull
  private PsiMethod generateNarrowK2(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK2(clazz);
    narrowK.setMethodReturnType(returnType2(clazz, narrowK.getTypeParameters()));
    narrowK.addParameter(HIGHER_KIND_TYPE, higher2Of(clazz, narrowK.getTypeParameters()));
    return narrowK;
  }

  @NotNull
  private PsiMethod generateNarrowK2Of1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK2(clazz);
    narrowK.setMethodReturnType(returnType2(clazz, narrowK.getTypeParameters()));
    narrowK.addParameter(HIGHER_KIND_TYPE, higher2Of1(clazz, narrowK.getTypeParameters()));
    return narrowK;
  }

  @NotNull
  private PsiMethod generateNarrowK3(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK3(clazz);
    narrowK.setMethodReturnType(returnType3(clazz, narrowK.getTypeParameters()));
    narrowK.addParameter(HIGHER_KIND_TYPE, higher3Of(clazz, narrowK.getTypeParameters()));
    return narrowK;
  }

  @NotNull
  private PsiMethod generateNarrowK3Of2(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK3(clazz);
    narrowK.setMethodReturnType(returnType3(clazz, narrowK.getTypeParameters()));
    narrowK.addParameter(HIGHER_KIND_TYPE, higher3Of2(clazz, narrowK.getTypeParameters()));
    return narrowK;
  }

  @NotNull
  private PsiMethod generateNarrowK3Of1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK3(clazz);
    narrowK.setMethodReturnType(returnType3(clazz, narrowK.getTypeParameters()));
    narrowK.addParameter(HIGHER_KIND_TYPE, higher3Of1(clazz, narrowK.getTypeParameters()));
    return narrowK;
  }

  @NotNull
  private LightMethodBuilder createNarrowKMethod(PsiClass clazz) {
    LightMethodBuilder narrowK = new LightMethodBuilder(psiManager, NARROW_KIND);
    narrowK.setContainingClass(clazz);
    narrowK.addModifier(PsiModifier.PUBLIC);
    narrowK.addModifier(PsiModifier.STATIC);
    return narrowK;
  }

  @NotNull
  private PsiMethod generateKind1(PsiClass clazz) {
    return createKindMethod(clazz, KIND1, higher1Of(clazz, clazz.getTypeParameters()));
  }

  @NotNull
  private PsiMethod generateKind2(PsiClass clazz) {
    return createKindMethod(clazz, KIND2, higher2Of(clazz, clazz.getTypeParameters()));
  }

  @NotNull
  private PsiMethod generateKind1Of2(PsiClass clazz) {
    return createKindMethod(clazz, KIND1, higher2Of1(clazz, clazz.getTypeParameters()));
  }

  @NotNull
  private PsiMethod generateKind3(PsiClass clazz) {
    return createKindMethod(clazz, KIND3, higher3Of(clazz, clazz.getTypeParameters()));
  }

  @NotNull
  private PsiMethod generateKind1Of3(PsiClass clazz) {
    return createKindMethod(clazz, KIND1, higher3Of1(clazz, clazz.getTypeParameters()));
  }

  @NotNull
  private PsiMethod generateKind2Of3(PsiClass clazz) {
    return createKindMethod(clazz, KIND2, higher3Of2(clazz, clazz.getTypeParameters()));
  }

  @NotNull
  private PsiClassType returnType1(PsiClass clazz, PsiTypeParameter[] params) {
    PsiSubstitutor substitutor = EMPTY.put(clazz.getTypeParameters()[0], EMPTY.substitute(params[0]));
    return factory.createType(clazz, substitutor);
  }

  @NotNull
  private PsiClassType returnType2(PsiClass clazz, PsiTypeParameter[] params) {
    PsiSubstitutor substitutor = EMPTY
        .put(clazz.getTypeParameters()[0], EMPTY.substitute(params[0]))
        .put(clazz.getTypeParameters()[1], EMPTY.substitute(params[1]));
    return factory.createType(clazz, substitutor);
  }

  @NotNull
  private PsiClassType returnType3(PsiClass clazz, PsiTypeParameter[] params) {
    PsiSubstitutor substitutor = EMPTY
        .put(clazz.getTypeParameters()[0], EMPTY.substitute(params[0]))
        .put(clazz.getTypeParameters()[1], EMPTY.substitute(params[1]))
        .put(clazz.getTypeParameters()[2], EMPTY.substitute(params[2]));
    return factory.createType(clazz, substitutor);
  }

  @NotNull
  private PsiClassType higher1Of(PsiClass clazz, PsiTypeParameter[] params) {
    return higher1Of(witnessOf(clazz), params[0]);
  }

  @NotNull
  private PsiClassType higher2Of(PsiClass clazz, PsiTypeParameter[] params) {
    return higher2Of(witnessOf(clazz), params[0], params[1]);
  }

  @NotNull
  private PsiClassType higher2Of1(PsiClass clazz, PsiTypeParameter[] params) {
    return higher1Of(higher1Of(witnessOf(clazz), params[0]), params[1]);
  }

  @NotNull
  private PsiClassType higher3Of(PsiClass clazz, PsiTypeParameter[] params) {
    return higher3Of(witnessOf(clazz), params[0], params[1], params[2]);
  }

  @NotNull
  private PsiClassType higher3Of2(PsiClass clazz, PsiTypeParameter[] params) {
    return higher2Of(higher1Of(witnessOf(clazz), params[0]), params[1], params[2]);
  }

  @NotNull
  private PsiClassType higher3Of1(PsiClass clazz, PsiTypeParameter[] params) {
    return higher1Of(higher1Of(higher1Of(witnessOf(clazz), params[0]), params[1]), params[2]);
  }

  @NotNull
  private PsiClassType witnessOf(PsiClass clazz) {
    return factory.createTypeByFQClassName(clazz.getQualifiedName() + "." + WITNESS, searchScope);
  }

  @NotNull
  private PsiClassType higher1Of(PsiType witness, PsiTypeParameter param1) {
    PsiClass higher1 = fileManager.findClass(HIGHER1, searchScope);
    PsiSubstitutor substitutor =
        EMPTY
            .put(higher1.getTypeParameters()[0], EMPTY.substitute(witness))
            .put(higher1.getTypeParameters()[1], EMPTY.substitute(param1));
    return factory.createType(higher1, substitutor);
  }

  @NotNull
  private PsiClassType higher2Of(PsiType witness, PsiTypeParameter param1, PsiTypeParameter param2) {
    PsiClass higher2 = fileManager.findClass(HIGHER2, searchScope);
    PsiSubstitutor substitutor =
        EMPTY
            .put(higher2.getTypeParameters()[0], EMPTY.substitute(witness))
            .put(higher2.getTypeParameters()[1], EMPTY.substitute(param1))
            .put(higher2.getTypeParameters()[2], EMPTY.substitute(param2));
    return factory.createType(higher2, substitutor);
  }

  @NotNull
  private PsiClassType higher3Of(PsiType witness, PsiTypeParameter param1, PsiTypeParameter param2, PsiTypeParameter param3) {
    PsiClass higher3 = fileManager.findClass(HIGHER3, searchScope);
    PsiSubstitutor substitutor =
        EMPTY
            .put(higher3.getTypeParameters()[0], EMPTY.substitute(witness))
            .put(higher3.getTypeParameters()[1], EMPTY.substitute(param1))
            .put(higher3.getTypeParameters()[2], EMPTY.substitute(param2))
            .put(higher3.getTypeParameters()[3], EMPTY.substitute(param3));
    return factory.createType(higher3, substitutor);
  }

  @NotNull
  private PsiMethod createKindMethod(PsiClass clazz, String name, PsiClassType returnType) {
    LightMethodBuilder method = new LightMethodBuilder(psiManager, name);
    method.addModifier(PsiModifier.PUBLIC);
    method.setContainingClass(clazz);
    method.setMethodReturnType(returnType);
    return method;
  }

  @NotNull
  private LightMethodBuilder createNarrowK1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);
    narrowK.addTypeParameter(new LightTypeParameterBuilder("A", narrowK, 0));
    return narrowK;
  }

  @NotNull
  private LightMethodBuilder createNarrowK2(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);
    narrowK.addTypeParameter(new LightTypeParameterBuilder("A", narrowK, 0));
    narrowK.addTypeParameter(new LightTypeParameterBuilder("B", narrowK, 1));
    return narrowK;
  }

  @NotNull
  private LightMethodBuilder createNarrowK3(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);
    narrowK.addTypeParameter(new LightTypeParameterBuilder("A", narrowK, 0));
    narrowK.addTypeParameter(new LightTypeParameterBuilder("B", narrowK, 1));
    narrowK.addTypeParameter(new LightTypeParameterBuilder("C", narrowK, 2));
    return narrowK;
  }
}