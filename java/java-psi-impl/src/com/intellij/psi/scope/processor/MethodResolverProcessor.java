/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi.scope.processor;

import com.intellij.psi.*;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.scope.JavaScopeProcessorEvent;
import com.intellij.psi.scope.PsiConflictResolver;
import com.intellij.psi.scope.conflictResolvers.JavaMethodsConflictResolver;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

public class MethodResolverProcessor extends MethodCandidatesProcessor {
  private boolean myStopAcceptingCandidates = false;

  public MethodResolverProcessor(@NotNull PsiMethodCallExpression place){
    this(place, new PsiConflictResolver[]{new JavaMethodsConflictResolver(place.getArgumentList())});
    setArgumentList(place.getArgumentList());
    obtainTypeArguments(place);
  }

  public MethodResolverProcessor(PsiClass classConstr, @NotNull PsiExpressionList argumentList, @NotNull PsiElement place) {
    super(place, new PsiConflictResolver[]{new JavaMethodsConflictResolver(argumentList)}, new SmartList<CandidateInfo>());
    setIsConstructor(true);
    setAccessClass(classConstr);
    setArgumentList(argumentList);
  }

  public MethodResolverProcessor(@NotNull PsiElement place, @NotNull PsiConflictResolver[] resolvers) {
    super(place, resolvers, new SmartList<CandidateInfo>());
  }

  @Override
  public void handleEvent(Event event, Object associated) {
    if (event == JavaScopeProcessorEvent.CHANGE_LEVEL) {
      if (myHasAccessibleStaticCorrectCandidate) myStopAcceptingCandidates = true;
    }
    super.handleEvent(event, associated);
  }

  @Override
  public boolean execute(@NotNull PsiElement element, ResolveState state) {
    return !myStopAcceptingCandidates && super.execute(element, state);
  }
}
