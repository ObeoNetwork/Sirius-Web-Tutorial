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

import java.util.List;

import org.eclipse.sirius.web.services.api.projects.IProjectTemplateProvider;
import org.eclipse.sirius.web.services.api.projects.ProjectTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * Provides Family-specific project templates.
 *
 * @author arichard
 */
@Configuration
public class FamilyProjectTemplatesProvider implements IProjectTemplateProvider {

    public static final String FAMILY_TEMPLATE_ID = "family-template";

    @Override
    public List<ProjectTemplate> getProjectTemplates() {
        // @formatter:off
        var flowTemplate = ProjectTemplate.newProjectTemplate(FAMILY_TEMPLATE_ID)
                .label("Family")
                .imageURL("/images/Family-Template.png")
                .natures(List.of())
                .build();
        // @formatter:on
        return List.of(flowTemplate);
    }

}
