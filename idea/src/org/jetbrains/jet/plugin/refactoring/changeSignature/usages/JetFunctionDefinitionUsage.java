/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.refactoring.changeSignature.usages;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;
import org.jetbrains.jet.lexer.JetModifierKeywordToken;
import org.jetbrains.jet.plugin.refactoring.JetRefactoringUtil;
import org.jetbrains.jet.plugin.refactoring.changeSignature.JetChangeInfo;
import org.jetbrains.jet.plugin.refactoring.changeSignature.JetParameterInfo;
import org.jetbrains.jet.plugin.refactoring.changeSignature.JetValVar;

import static org.jetbrains.jet.lang.psi.PsiPackage.JetPsiFactory;

public class JetFunctionDefinitionUsage extends JetUsageInfo<PsiElement> {
    private final boolean isInherited;

    public JetFunctionDefinitionUsage(@NotNull PsiElement function, boolean isInherited) {
        super(function);
        this.isInherited = isInherited;
    }

    public final boolean isInherited() {
        return isInherited;
    }

    @Override
    public boolean processUsage(JetChangeInfo changeInfo, PsiElement element) {
        JetParameterList parameterList;

        JetPsiFactory psiFactory = JetPsiFactory(element.getProject());
        if (element instanceof JetFunction) {
            JetFunction function = (JetFunction) element;
            parameterList = function.getValueParameterList();

            if (changeInfo.isNameChanged()) {
                PsiElement identifier = function.getNameIdentifier();

                if (identifier != null) {
                    identifier.replace(psiFactory.createIdentifier(changeInfo.getNewName()));
                }
            }
            if (changeInfo.isReturnTypeChanged()) {
                function.setTypeReference(null);
                String returnTypeText = changeInfo.getNewReturnTypeText();

                //TODO use ChangeFunctionReturnTypeFix.invoke when JetTypeCodeFragment.getType() is ready
                if (!KotlinBuiltIns.getInstance().getUnitType().toString().equals(returnTypeText)) {
                    function.setTypeReference(JetPsiFactory(function).createType(returnTypeText));
                }
            }
        }
        else {
            parameterList = ((JetClass) element).getPrimaryConstructorParameterList();
        }

        if (changeInfo.isParameterSetOrOrderChanged()) {
            String parametersText = changeInfo.getNewParametersSignature(element, isInherited, 0);
            JetParameterList newParameterList = psiFactory.createParameterList(parametersText);

            if (parameterList != null) {
                parameterList.replace(newParameterList);
            }
            else if (element instanceof JetClass) {
                PsiElement anchor = ((JetClass) element).getTypeParameterList();

                if (anchor == null) {
                    anchor = ((JetClass) element).getNameIdentifier();
                }
                if (anchor != null) {
                    element.addAfter(newParameterList, anchor);
                }
            }
        }
        else if (parameterList != null) {
            int paramIndex = 0;

            for (JetParameter parameter : parameterList.getParameters()) {
                JetParameterInfo parameterInfo = changeInfo.getNewParameters()[paramIndex++];
                changeParameter(changeInfo, element, parameter, parameterInfo);
            }
        }

        if (changeInfo.isVisibilityChanged() && !JetPsiUtil.isLocal((JetDeclaration) element)) {
            changeVisibility(changeInfo, element);
        }

        return true;
    }

    private static void changeVisibility(JetChangeInfo changeInfo, PsiElement element) {
        JetModifierKeywordToken newVisibilityToken = JetRefactoringUtil.getVisibilityToken(changeInfo.getNewVisibility());

        if (element instanceof JetFunction) {
            ((JetFunction)element).addModifier(newVisibilityToken);
        }
        else {
            ((JetClass)element).addPrimaryConstructorModifier(newVisibilityToken);
        }
    }

    private void changeParameter(JetChangeInfo changeInfo, PsiElement element, JetParameter parameter, JetParameterInfo parameterInfo) {
        ASTNode valOrVarAstNode = parameter.getValOrVarNode();
        PsiElement valOrVarNode = valOrVarAstNode != null ? valOrVarAstNode.getPsi() : null;
        JetValVar valOrVar = parameterInfo.getValOrVar();

        JetPsiFactory psiFactory = JetPsiFactory(getProject());
        if (valOrVarNode != null) {
            if (valOrVar == JetValVar.None) {
                valOrVarNode.delete();
            }
            else {
                valOrVarNode.replace(psiFactory.createValOrVarNode(valOrVar.toString()).getPsi());
            }
        }
        else if (valOrVar != JetValVar.None) {
            PsiElement firstChild = parameter.getFirstChild();
            parameter.addBefore(psiFactory.createValOrVarNode(valOrVar.toString()).getPsi(), firstChild);
            parameter.addBefore(psiFactory.createWhiteSpace(), firstChild);
        }

        if (parameterInfo.isTypeChanged()) {
            JetTypeReference newTypeRef = psiFactory.createType(parameterInfo.getTypeText());
            parameter.setTypeReference(newTypeRef);
        }

        PsiElement identifier = parameter.getNameIdentifier();

        if (identifier != null) {
            String newName = parameterInfo.getInheritedName(isInherited, element, changeInfo.getFunctionDescriptor());
            identifier.replace(psiFactory.createIdentifier(newName));
        }
    }
}
