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

  @Override
  public List<PsiClass> processClass(PsiClass clazz) {
    logger.info("process classes for: {}", clazz.getQualifiedName());
    PsiTypeParameter[] typeParameters = clazz.getTypeParameters();
    if (typeParameters.length > 0) {
      return newGenerator(project).generateWitness(clazz);
    }
    return emptyList();
  }

  @Override
  public List<PsiMethod> processMethod(PsiClass clazz) {
    logger.info("process methods for: {}", clazz.getQualifiedName());
    PsiTypeParameter[] typeParameters = clazz.getTypeParameters();
    if (typeParameters.length == 1) {
      return newGenerator(project).generateHigher1Methods(clazz);
    }
    if (typeParameters.length == 2) {
      return newGenerator(project).generateHigher2Methods(clazz);
    }
    if (typeParameters.length == 3) {
      return newGenerator(project).generateHigher3Methods(clazz);
    }
    return emptyList();
  }

  @NotNull
  private static HigherKindGenerator newGenerator(Project project) {
    return new HigherKindGenerator(
        PsiElementFactory.getInstance(project),
        PsiManager.getInstance(project),
        JavaFileManager.getInstance(project),
        allScope(project)
    );
  }
}

class HigherKindGenerator {

  private static final String KIND = "com.github.tonivade.purefun.Kind";
  private static final String HIGHER1 = "com.github.tonivade.purefun.Higher1";
  private static final String HIGHER2 = "com.github.tonivade.purefun.Higher2";
  private static final String HIGHER3 = "com.github.tonivade.purefun.Higher3";
  private static final String KIND_1 = "kind1";
  private static final String KIND_2 = "kind2";
  private static final String KIND_3 = "kind3";
  private static final String NARROW_K = "narrowK";
  private static final String HKT = "hkt";

  private final PsiElementFactory factory;
  private final PsiManager psiManager;
  private final JavaFileManager fileManager;
  private final GlobalSearchScope searchScope;

  HigherKindGenerator(PsiElementFactory factory,
                      PsiManager psiManager,
                      JavaFileManager fileManager,
                      GlobalSearchScope searchScope) {
    this.factory = requireNonNull(factory);
    this.psiManager = requireNonNull(psiManager);
    this.fileManager = requireNonNull(fileManager);
    this.searchScope = requireNonNull(searchScope);
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
    LightPsiClassBuilder witness = new LightPsiClassBuilder(clazz, "µ");
    witness.setContainingClass(clazz);
    witness.getModifierList().addModifier(PsiModifier.PUBLIC);
    witness.getModifierList().addModifier(PsiModifier.STATIC);
    witness.getModifierList().addModifier(PsiModifier.FINAL);
    witness.getImplementsList().addReference(KIND);
    return singletonList(witness);
  }

  private PsiMethod generateNarrowK1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    narrowK.addTypeParameter(param1);

    narrowK.setMethodReturnType(returnType1(clazz, param1));
    narrowK.addParameter(HKT, higher1Of(clazz, param1));
    return narrowK;
  }

  private PsiMethod generateNarrowK2(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);

    narrowK.setMethodReturnType(returnType2(clazz, param1, param2));
    narrowK.addParameter(HKT, higher2Of(clazz, param1, param2));
    return narrowK;
  }

  private PsiMethod generateNarrowK2Of1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);

    narrowK.setMethodReturnType(returnType2(clazz, param1, param2));
    narrowK.addParameter(HKT, higher2Of1(clazz, param1, param2));
    return narrowK;
  }

  private PsiMethod generateNarrowK3(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    LightTypeParameterBuilder param3 = new LightTypeParameterBuilder("C", narrowK, 2);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);
    narrowK.addTypeParameter(param3);

    narrowK.setMethodReturnType(returnType3(clazz, param1, param2, param3));
    narrowK.addParameter(HKT, higher3Of(clazz, param1, param2, param3));
    return narrowK;
  }

  private PsiMethod generateNarrowK3Of2(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    LightTypeParameterBuilder param3 = new LightTypeParameterBuilder("C", narrowK, 2);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);
    narrowK.addTypeParameter(param3);

    narrowK.setMethodReturnType(returnType3(clazz, param1, param2, param3));
    narrowK.addParameter(HKT, higher3Of2(clazz, param1, param2, param3));
    return narrowK;
  }

  private PsiMethod generateNarrowK3Of1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowKMethod(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    LightTypeParameterBuilder param3 = new LightTypeParameterBuilder("C", narrowK, 2);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);
    narrowK.addTypeParameter(param3);

    narrowK.setMethodReturnType(returnType3(clazz, param1, param2, param3));
    narrowK.addParameter(HKT, higher3Of1(clazz, param1, param2, param3));
    return narrowK;
  }

  private LightMethodBuilder createNarrowKMethod(PsiClass clazz) {
    LightMethodBuilder narrowK = new LightMethodBuilder(psiManager, NARROW_K);
    narrowK.setContainingClass(clazz);
    narrowK.addModifier(PsiModifier.PUBLIC);
    narrowK.addModifier(PsiModifier.STATIC);
    return narrowK;
  }

  private PsiMethod generateKind1(PsiClass clazz) {
    return createKindMethod(clazz, KIND_1, higher1Of(witnessOf(clazz), clazz.getTypeParameters()[0]));
  }

  private PsiMethod generateKind2(PsiClass clazz) {
    return createKindMethod(clazz, KIND_2,
        higher2Of(clazz, clazz.getTypeParameters()[0], clazz.getTypeParameters()[1]));
  }

  private PsiMethod generateKind1Of2(PsiClass clazz) {
    return createKindMethod(clazz, KIND_1,
        higher2Of1(clazz, clazz.getTypeParameters()[0], clazz.getTypeParameters()[1]));
  }

  private PsiMethod generateKind3(PsiClass clazz) {
    return createKindMethod(clazz, KIND_3,
        higher3Of(clazz, clazz.getTypeParameters()[0], clazz.getTypeParameters()[1], clazz.getTypeParameters()[2]));
  }

  private PsiMethod generateKind1Of3(PsiClass clazz) {
    return createKindMethod(clazz, KIND_1,
        higher3Of1(clazz, clazz.getTypeParameters()[0], clazz.getTypeParameters()[1], clazz.getTypeParameters()[2]));
  }

  private PsiMethod generateKind2Of3(PsiClass clazz) {
    return createKindMethod(clazz, KIND_2,
        higher3Of2(clazz, clazz.getTypeParameters()[0], clazz.getTypeParameters()[1], clazz.getTypeParameters()[2]));
  }

  private PsiClassType returnType1(PsiClass clazz, PsiTypeParameter param1) {
    PsiSubstitutor substitutor = EMPTY.put(clazz.getTypeParameters()[0], EMPTY.substitute(param1));
    return factory.createType(clazz, substitutor);
  }

  private PsiClassType returnType2(PsiClass clazz, PsiTypeParameter param1, PsiTypeParameter param2) {
    PsiSubstitutor substitutor = EMPTY
        .put(clazz.getTypeParameters()[0], EMPTY.substitute(param1))
        .put(clazz.getTypeParameters()[1], EMPTY.substitute(param2));
    return factory.createType(clazz, substitutor);
  }

  private PsiClassType returnType3(PsiClass clazz, PsiTypeParameter param1, PsiTypeParameter param2, PsiTypeParameter param3) {
    PsiSubstitutor substitutor = EMPTY
        .put(clazz.getTypeParameters()[0], EMPTY.substitute(param1))
        .put(clazz.getTypeParameters()[1], EMPTY.substitute(param2))
        .put(clazz.getTypeParameters()[2], EMPTY.substitute(param3));
    return factory.createType(clazz, substitutor);
  }

  private PsiClassType higher1Of(PsiClass clazz, PsiTypeParameter param1) {
    return higher1Of(witnessOf(clazz), param1);
  }

  private PsiClassType higher2Of(PsiClass clazz, PsiTypeParameter param1, PsiTypeParameter param2) {
    return higher2Of(witnessOf(clazz), param1, param2);
  }

  private PsiClassType higher2Of1(PsiClass clazz, PsiTypeParameter param1, PsiTypeParameter param2) {
    return higher1Of(higher1Of(witnessOf(clazz), param1), param2);
  }

  private PsiClassType higher3Of(PsiClass clazz, PsiTypeParameter type1, PsiTypeParameter type2, PsiTypeParameter type3) {
    return higher3Of(witnessOf(clazz), type1, type2, type3);
  }

  private PsiClassType higher3Of2(PsiClass clazz, PsiTypeParameter type1, PsiTypeParameter type2, PsiTypeParameter type3) {
    return higher2Of(higher1Of(witnessOf(clazz), type1), type2, type3);
  }

  private PsiClassType higher3Of1(PsiClass clazz, PsiTypeParameter type1, PsiTypeParameter type2, PsiTypeParameter type3) {
    return higher1Of(higher1Of(higher1Of(witnessOf(clazz), type1), type2), type3);
  }

  private PsiClassType higher1Of(PsiType witness, PsiTypeParameter type) {
    PsiClass higher1 = fileManager.findClass(HIGHER1, searchScope);
    PsiSubstitutor substitutor =
        EMPTY
            .put(higher1.getTypeParameters()[0], EMPTY.substitute(witness))
            .put(higher1.getTypeParameters()[1], EMPTY.substitute(type));
    return factory.createType(higher1, substitutor);
  }

  private PsiClassType higher2Of(PsiType witness, PsiTypeParameter type1, PsiTypeParameter type2) {
    PsiClass higher2 = fileManager.findClass(HIGHER2, searchScope);
    PsiSubstitutor substitutor =
        EMPTY
            .put(higher2.getTypeParameters()[0], EMPTY.substitute(witness))
            .put(higher2.getTypeParameters()[1], EMPTY.substitute(type1))
            .put(higher2.getTypeParameters()[2], EMPTY.substitute(type2));
    return factory.createType(higher2, substitutor);
  }

  private PsiClassType higher3Of(PsiType witness, PsiTypeParameter type1, PsiTypeParameter type2, PsiTypeParameter type3) {
    PsiClass higher3 = fileManager.findClass(HIGHER3, searchScope);
    PsiSubstitutor substitutor =
        EMPTY
            .put(higher3.getTypeParameters()[0], EMPTY.substitute(witness))
            .put(higher3.getTypeParameters()[1], EMPTY.substitute(type1))
            .put(higher3.getTypeParameters()[2], EMPTY.substitute(type2))
            .put(higher3.getTypeParameters()[3], EMPTY.substitute(type3));
    return factory.createType(higher3, substitutor);
  }

  private PsiMethod createKindMethod(PsiClass clazz, String name, PsiClassType returnType) {
    LightMethodBuilder method = new LightMethodBuilder(psiManager, name);
    method.addModifier(PsiModifier.PUBLIC);
    method.setContainingClass(clazz);
    method.setMethodReturnType(returnType);
    return method;
  }

  private PsiClassType witnessOf(PsiClass clazz) {
    return factory.createTypeByFQClassName(clazz.getQualifiedName() + ".µ", searchScope);
  }
}