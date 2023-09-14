/*******************************************************************************
 * Copyright (c) 2023 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.family.configuration;

import fr.obeo.dsl.designer.sample.flow.FlowPackage;
import fr.obeo.dsl.designer.sample.flow.Named;

import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.EcoreValidator;

/**
 * @author arichard
 */
public class FlowValidator implements EValidator {

    public static final String INVALID_NAME_ERROR_MESSAGE = "The name %1$s is not well-formed.";

    @Override
    public boolean validate(EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return true;
    }

    @Override
    public boolean validate(EDataType eDataType, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return true;
    }

    @Override
    public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
        boolean isValid = true;
        if (eObject instanceof Named flowNamedElement) {
            isValid = this.nameIsWellFormedValidate(flowNamedElement, diagnostics) && isValid;
        }
        return isValid;
    }

    private boolean nameIsWellFormedValidate(Named flowNamedElement, DiagnosticChain diagnostics) {
        boolean isValid = EcoreValidator.isWellFormedJavaIdentifier(flowNamedElement.getName());

        if (!isValid && diagnostics != null) {
            // @formatter:off
            BasicDiagnostic basicDiagnostic = new BasicDiagnostic(Diagnostic.WARNING,
                    "sirius-web-family-application",
                    0,
                    String.format(INVALID_NAME_ERROR_MESSAGE, flowNamedElement.getName()),
                    new Object [] {
                        flowNamedElement,
                        FlowPackage.Literals.NAMED__NAME,
                    });
            // @formatter:on

            diagnostics.add(basicDiagnostic);
        }

        return isValid;
    }
}
