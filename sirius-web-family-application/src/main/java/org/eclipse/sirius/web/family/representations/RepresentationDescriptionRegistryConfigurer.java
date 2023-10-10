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
package org.eclipse.sirius.web.family.representations;

import java.util.List;
import java.util.UUID;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.configuration.IRepresentationDescriptionRegistry;
import org.eclipse.sirius.components.core.configuration.IRepresentationDescriptionRegistryConfigurer;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.ColorPaletteBuilder;
import org.eclipse.sirius.components.view.builder.generated.FixedColorBuilder;
import org.eclipse.sirius.components.view.builder.generated.ViewBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.emf.IViewConverter;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.services.api.representations.IInMemoryViewRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * @author arichard
 */
@Configuration
public class RepresentationDescriptionRegistryConfigurer implements IRepresentationDescriptionRegistryConfigurer {

    private final IViewConverter viewConverter;

    private final EPackage.Registry ePackagesRegistry;

    private final IInMemoryViewRegistry inMemoryViewRegistry;

    public RepresentationDescriptionRegistryConfigurer(IViewConverter viewConverter, Registry ePackagesRegistry, IInMemoryViewRegistry inMemoryViewRegistry) {
        this.viewConverter = viewConverter;
        this.ePackagesRegistry = ePackagesRegistry;
        this.inMemoryViewRegistry = inMemoryViewRegistry;
    }

    @Override
    public void addRepresentationDescriptions(IRepresentationDescriptionRegistry registry) {
        // Create org.eclipse.sirius.components.view.View
        ViewBuilder viewBuilder = new ViewBuilder();
        View view = viewBuilder
                        .colorPalettes(this.createColorPalette())
                        .build();
        IColorProvider colorProvider = new ColorProvider(view);

        // Create org.eclipse.sirius.components.view.RepresentationDescription
        FamilyDiagramWithViewBuilder familyDiagramWithViewBuilder = new FamilyDiagramWithViewBuilder();
        RepresentationDescription viewRepresentationDescription = familyDiagramWithViewBuilder.create(colorProvider);
        view.getDescriptions().add(viewRepresentationDescription);

        // Add an ID to all view elements
        view.eAllContents().forEachRemaining(eObject -> {
            eObject.eAdapters().add(new IDAdapter(UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes())));
        });

        // All programmatic Views need to be stored in a Resource and registered in IInMemoryViewRegistry
        String resourcePath = UUID.nameUUIDFromBytes("FamilyDiagramWithViewBuilder".getBytes()).toString();
        JsonResource resource = new JSONResourceFactory().createResourceFromPath(resourcePath);
        resource.eAdapters().add(new ResourceMetadataAdapter("ViewWithViewBuilder"));
        resource.getContents().add(view);
        this.inMemoryViewRegistry.register(view);

        // Convert org.eclipse.sirius.components.view.RepresentationDescription to org.eclipse.sirius.components.representations.IRepresentationDescription
        List<EPackage> staticEPackages = this.ePackagesRegistry.values().stream().filter(EPackage.class::isInstance).map(EPackage.class::cast).toList();
        var representationDescriptions = this.viewConverter.convert(List.of(view), staticEPackages);

        // Register org.eclipse.sirius.components.representations.IRepresentationDescription
        representationDescriptions.forEach(registry::add);
    }

    private ColorPalette createColorPalette() {
        ColorPaletteBuilder colorPaletteBuilder = new ColorPaletteBuilder();
        return colorPaletteBuilder
                .colors(this.createFixedColor("color_empty", ""),
                        this.createFixedColor("white", "white"),
                        this.createFixedColor("black", "black"),
                        this.createFixedColor("blue", "blue"),
                        this.createFixedColor("green", "green"),
                        this.createFixedColor("red", "red"),
                        this.createFixedColor("starwars_color", "#d595b8"),
                        this.createFixedColor("startrek_color", "#a59ab0"))
                .build();
    }

    private FixedColor createFixedColor(String name, String value) {
        FixedColorBuilder fixedColor = new FixedColorBuilder();
        return fixedColor
                .name(name)
                .value(value)
                .build();
    }
}