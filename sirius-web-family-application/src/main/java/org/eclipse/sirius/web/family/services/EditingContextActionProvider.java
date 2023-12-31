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
package org.eclipse.sirius.web.family.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.sirius.components.collaborative.api.IEditingContextActionProvider;
import org.eclipse.sirius.components.collaborative.dto.EditingContextAction;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.domain.DomainPackage;
import org.eclipse.sirius.components.emf.services.EditingContext;
import org.eclipse.sirius.components.view.ViewPackage;
import org.springframework.stereotype.Service;

/**
 * Provides the list of possible actions on the editingContext.
 *
 * @author frouene
 */
@Service
public class EditingContextActionProvider implements IEditingContextActionProvider {

    public static final String EMPTY_ACTION_ID = "empty";

    public static final String EMPTY_FLOW_ID = "empty_flow";

    public static final String ROBOT_FLOW_ID = "robot_flow";

    public static final String EMPTY_DOMAIN_ID = "empty_domain";

    public static final String EMPTY_VIEW_ID = "empty_view";

    private static final EditingContextAction EMPTY_EDITING_CONTEXT_ACTION = new EditingContextAction(EMPTY_ACTION_ID, "Others...");

    private static final EditingContextAction EMPTY_FLOW_EDITING_CONTEXT_ACTION = new EditingContextAction(EMPTY_FLOW_ID, "Flow");

    private static final EditingContextAction ROBOT_FLOW_EDITING_CONTEXT_ACTION = new EditingContextAction(ROBOT_FLOW_ID, "Robot Flow");

    private static final EditingContextAction EMPTY_DOMAIN_EDITING_CONTEXT_ACTION = new EditingContextAction(EMPTY_DOMAIN_ID, "Domain");

    private static final EditingContextAction EMPTY_VIEW_EDITING_CONTEXT_ACTION = new EditingContextAction(EMPTY_VIEW_ID, "View");

    @Override
    public List<EditingContextAction> getEditingContextAction(IEditingContext editingContext) {
        var actions = new ArrayList<EditingContextAction>();
        if (editingContext instanceof EditingContext emfEditingContext) {
            var nsURIs = emfEditingContext.getDomain().getResourceSet().getPackageRegistry().values()
                    .stream()
                    .filter(EPackage.class::isInstance)
                    .map(EPackage.class::cast)
                    .map(EPackage::getNsURI)
                    .toList();

            var containsDomain = nsURIs.contains(DomainPackage.eNS_URI);
            var containsView = nsURIs.contains(ViewPackage.eNS_URI);

            actions.add(EMPTY_FLOW_EDITING_CONTEXT_ACTION);
            actions.add(ROBOT_FLOW_EDITING_CONTEXT_ACTION);
            if (containsDomain) {
                actions.add(EMPTY_DOMAIN_EDITING_CONTEXT_ACTION);
            }
            if (containsView) {
                actions.add(EMPTY_VIEW_EDITING_CONTEXT_ACTION);
            }
            actions.add(EMPTY_EDITING_CONTEXT_ACTION);
        }
        return actions;
    }
}
