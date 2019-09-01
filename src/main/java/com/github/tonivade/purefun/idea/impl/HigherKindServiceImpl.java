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

  private static final String KIND = "com.github.tonivade.purefun.Kind";
  private static final String HIGHER1 = "com.github.tonivade.purefun.Higher1";
  private static final String HIGHER2 = "com.github.tonivade.purefun.Higher2";
  private static final String HIGHER3 = "com.github.tonivade.purefun.Higher3";
  private static final String KIND_1 = "kind1";
  private static final String KIND_2 = "kind2";
  private static final String KIND_3 = "kind3";
  private static final String NARROW_K = "narrowK";

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
      return singletonList(generateWitness(clazz));
    }
    return emptyList();
  }

  @Override
  public List<PsiMethod> processMethod(PsiClass clazz) {
    logger.info("process methods for: {}", clazz.getQualifiedName());
    PsiTypeParameter[] typeParameters = clazz.getTypeParameters();
    if (typeParameters.length == 1) {
      return asList(
        generateNarrowK1(clazz),
        generateKind1(clazz)
      );
    }
    if (typeParameters.length == 2) {
      return asList(
        generateNarrowK2(clazz),
        generateNarrowK2Of1(clazz),
        generateKind2(clazz),
        generateKind1Of2(clazz)
      );
    }
    if (typeParameters.length == 3) {
      return asList(
        generateNarrowK3(clazz),
        generateNarrowK3Of2(clazz),
        generateNarrowK3Of1(clazz),
        generateKind3(clazz),
        generateKind1Of3(clazz),
        generateKind2Of3(clazz)
      );
    }
    return emptyList();
  }

  private PsiClass generateWitness(PsiClass clazz) {
    LightPsiClassBuilder witness = new LightPsiClassBuilder(clazz, "µ");
    witness.setContainingClass(clazz);
    witness.getModifierList().addModifier(PsiModifier.PUBLIC);
    witness.getModifierList().addModifier(PsiModifier.STATIC);
    witness.getModifierList().addModifier(PsiModifier.FINAL);
    witness.getImplementsList().addReference(KIND);
    return witness;
  }

  private PsiMethod generateNarrowK1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    narrowK.addTypeParameter(param1);

    narrowK.setMethodReturnType(returnType1(clazz, param1));
    narrowK.addParameter("hkt", higher1Of(clazz, param1));
    return narrowK;
  }

  private PsiMethod generateNarrowK2(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);

    narrowK.setMethodReturnType(returnType2(clazz, param1, param2));
    narrowK.addParameter("hkt", higher2Of(clazz, param1, param2));
    return narrowK;
  }

  private PsiMethod generateNarrowK2Of1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);

    narrowK.setMethodReturnType(returnType2(clazz, param1, param2));
    narrowK.addParameter("hkt", higher2Of1(clazz, param1, param2));
    return narrowK;
  }

  private PsiMethod generateNarrowK3(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    LightTypeParameterBuilder param3 = new LightTypeParameterBuilder("C", narrowK, 2);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);
    narrowK.addTypeParameter(param3);

    narrowK.setMethodReturnType(returnType3(clazz, param1, param2, param3));
    narrowK.addParameter("hkt", higher3Of(clazz, param1, param2, param3));
    return narrowK;
  }

  private PsiMethod generateNarrowK3Of2(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    LightTypeParameterBuilder param3 = new LightTypeParameterBuilder("C", narrowK, 2);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);
    narrowK.addTypeParameter(param3);

    narrowK.setMethodReturnType(returnType3(clazz, param1, param2, param3));
    narrowK.addParameter("hkt", higher3Of2(clazz, param1, param2, param3));
    return narrowK;
  }

  private PsiMethod generateNarrowK3Of1(PsiClass clazz) {
    LightMethodBuilder narrowK = createNarrowK(clazz);

    LightTypeParameterBuilder param1 = new LightTypeParameterBuilder("A", narrowK, 0);
    LightTypeParameterBuilder param2 = new LightTypeParameterBuilder("B", narrowK, 1);
    LightTypeParameterBuilder param3 = new LightTypeParameterBuilder("C", narrowK, 2);
    narrowK.addTypeParameter(param1);
    narrowK.addTypeParameter(param2);
    narrowK.addTypeParameter(param3);

    narrowK.setMethodReturnType(returnType3(clazz, param1, param2, param3));
    narrowK.addParameter("hkt", higher3Of1(clazz, param1, param2, param3));
    return narrowK;
  }

  private LightMethodBuilder createNarrowK(PsiClass clazz) {
    LightMethodBuilder narrowK = new LightMethodBuilder(PsiManager.getInstance(project), NARROW_K);
    narrowK.setContainingClass(clazz);
    narrowK.addModifier(PsiModifier.PUBLIC);
    narrowK.addModifier(PsiModifier.STATIC);
    return narrowK;
  }

  private PsiMethod generateKind1(PsiClass clazz) {
    LightMethodBuilder kind1 = new LightMethodBuilder(PsiManager.getInstance(project), KIND_1);
    kind1.addModifier(PsiModifier.PUBLIC);
    kind1.setContainingClass(clazz);
    kind1.setMethodReturnType(higher1Of(witnessOf(clazz), clazz.getTypeParameters()[0]));
    return kind1;
  }

  private PsiMethod generateKind2(PsiClass clazz) {
    LightMethodBuilder kind2 = new LightMethodBuilder(PsiManager.getInstance(project), KIND_2);
    kind2.addModifier(PsiModifier.PUBLIC);
    kind2.setContainingClass(clazz);
    kind2.setMethodReturnType(higher2Of(witnessOf(clazz), clazz.getTypeParameters()[0], clazz.getTypeParameters()[1]));
    return kind2;
  }

  private PsiMethod generateKind1Of2(PsiClass clazz) {
    LightMethodBuilder kind2 = new LightMethodBuilder(PsiManager.getInstance(project), KIND_1);
    kind2.addModifier(PsiModifier.PUBLIC);
    kind2.setContainingClass(clazz);
    kind2.setMethodReturnType(higher1Of( higher1Of(witnessOf(clazz), clazz.getTypeParameters()[0]), clazz.getTypeParameters()[1]));
    return kind2;
  }

  private PsiMethod generateKind3(PsiClass clazz) {
    LightMethodBuilder kind3 = new LightMethodBuilder(PsiManager.getInstance(project), KIND_3);
    kind3.addModifier(PsiModifier.PUBLIC);
    kind3.setContainingClass(clazz);
    kind3.setMethodReturnType(higher3Of(witnessOf(clazz), clazz.getTypeParameters()[0], clazz.getTypeParameters()[1], clazz.getTypeParameters()[2]));
    return kind3;
  }

  private PsiMethod generateKind1Of3(PsiClass clazz) {
    LightMethodBuilder kind3 = new LightMethodBuilder(PsiManager.getInstance(project), KIND_1);
    kind3.addModifier(PsiModifier.PUBLIC);
    kind3.setContainingClass(clazz);
    kind3.setMethodReturnType(higher1Of(higher1Of(higher1Of(witnessOf(clazz), clazz.getTypeParameters()[0]), clazz.getTypeParameters()[1]), clazz.getTypeParameters()[2]));
    return kind3;
  }

  private PsiMethod generateKind2Of3(PsiClass clazz) {
    LightMethodBuilder kind3 = new LightMethodBuilder(PsiManager.getInstance(project), KIND_2);
    kind3.addModifier(PsiModifier.PUBLIC);
    kind3.setContainingClass(clazz);
    kind3.setMethodReturnType(higher2Of(higher1Of(witnessOf(clazz), clazz.getTypeParameters()[0]), clazz.getTypeParameters()[1], clazz.getTypeParameters()[2]));
    return kind3;
  }

  private PsiClassType returnType1(PsiClass clazz, PsiTypeParameter param1) {
    PsiElementFactory factory = PsiElementFactory.getInstance(project);
    PsiSubstitutor substitutor = EMPTY.put(clazz.getTypeParameters()[0], EMPTY.substitute(param1));
    return factory.createType(clazz, substitutor);
  }

  private PsiClassType returnType2(PsiClass clazz, PsiTypeParameter param1, PsiTypeParameter param2) {
    PsiElementFactory factory = PsiElementFactory.getInstance(project);
    PsiSubstitutor substitutor = EMPTY
        .put(clazz.getTypeParameters()[0], EMPTY.substitute(param1))
        .put(clazz.getTypeParameters()[1], EMPTY.substitute(param2));
    return factory.createType(clazz, substitutor);
  }

  private PsiClassType returnType3(PsiClass clazz, PsiTypeParameter param1, PsiTypeParameter param2, PsiTypeParameter param3) {
    PsiElementFactory factory = PsiElementFactory.getInstance(project);
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
    PsiElementFactory factory = PsiElementFactory.getInstance(project);
    PsiClass higher1 = JavaFileManager.getInstance(project).findClass(HIGHER1, allScope(project));
    PsiSubstitutor substitutor =
      EMPTY
        .put(higher1.getTypeParameters()[0], EMPTY.substitute(witness))
        .put(higher1.getTypeParameters()[1], EMPTY.substitute(type));
    return factory.createType(higher1, substitutor);
  }

  private PsiClassType higher2Of(PsiType witness, PsiTypeParameter type1, PsiTypeParameter type2) {
    PsiElementFactory factory = PsiElementFactory.getInstance(project);
    PsiClass higher2 = JavaFileManager.getInstance(project).findClass(HIGHER2, allScope(project));
    PsiSubstitutor substitutor =
      EMPTY
        .put(higher2.getTypeParameters()[0], EMPTY.substitute(witness))
        .put(higher2.getTypeParameters()[1], EMPTY.substitute(type1))
        .put(higher2.getTypeParameters()[2], EMPTY.substitute(type2));
    return factory.createType(higher2, substitutor);
  }

  private PsiClassType higher3Of(PsiType witness, PsiTypeParameter type1, PsiTypeParameter type2, PsiTypeParameter type3) {
    PsiElementFactory factory = PsiElementFactory.getInstance(project);
    PsiClass higher3 = JavaFileManager.getInstance(project).findClass(HIGHER3, allScope(project));
    PsiSubstitutor substitutor =
      EMPTY
        .put(higher3.getTypeParameters()[0], EMPTY.substitute(witness))
        .put(higher3.getTypeParameters()[1], EMPTY.substitute(type1))
        .put(higher3.getTypeParameters()[2], EMPTY.substitute(type2))
        .put(higher3.getTypeParameters()[3], EMPTY.substitute(type3));
    return factory.createType(higher3, substitutor);
  }

  private PsiClassType witnessOf(PsiClass clazz) {
    PsiElementFactory factory = PsiElementFactory.getInstance(project);
    return factory.createTypeByFQClassName(clazz.getQualifiedName() + ".µ", allScope(project));
  }
}
