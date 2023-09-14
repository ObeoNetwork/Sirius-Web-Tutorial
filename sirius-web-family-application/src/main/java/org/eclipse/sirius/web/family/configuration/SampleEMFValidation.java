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

import java.util.Objects;

import org.eclipse.emf.ecore.EValidator;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * @author arichard
 */
@Configuration
public class SampleEMFValidation {


    private final EValidator.Registry eValidatorRegistry;

    public SampleEMFValidation(EValidator.Registry eValidatorRegistry) {
        this.eValidatorRegistry = Objects.requireNonNull(eValidatorRegistry);
    }

    @PostConstruct
    public void registerFamilyValidator() {
        this.eValidatorRegistry.put(FlowPackage.eINSTANCE, new FlowValidator());
    }
}
