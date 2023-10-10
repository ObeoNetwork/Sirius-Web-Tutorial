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

import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.generated.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.CreateInstanceBuilder;
import org.eclipse.sirius.components.view.builder.generated.DeleteToolBuilder;
import org.eclipse.sirius.components.view.builder.generated.DiagramDescriptionBuilder;
import org.eclipse.sirius.components.view.builder.generated.DiagramPaletteBuilder;
import org.eclipse.sirius.components.view.builder.generated.NodeDescriptionBuilder;
import org.eclipse.sirius.components.view.builder.generated.NodePaletteBuilder;
import org.eclipse.sirius.components.view.builder.generated.NodeToolBuilder;
import org.eclipse.sirius.components.view.builder.generated.RectangularNodeStyleDescriptionBuilder;
import org.eclipse.sirius.components.view.builder.generated.SetValueBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;

/**
 * @author arichard
 */
public class FamilyDiagramWithViewBuilder implements IRepresentationDescriptionProvider {

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        DiagramDescriptionBuilder builder = new DiagramDescriptionBuilder();
        return builder
                .autoLayout(true)
                .domainType("tutorial::Family")
                .edgeDescriptions(this.createEdgeDescriptions().toArray(EdgeDescription[]::new))
                .name("FamilyDiagramWithViewBuilder")
                .nodeDescriptions(this.createNodeDescriptions(colorProvider).toArray(NodeDescription[]::new))
                .palette(this.createPalette())
                .titleExpression("Family Diagram with ViewBuilder API")
                .build();
    }

    private List<NodeDescription> createNodeDescriptions(IColorProvider colorProvider) {
        NodeDescriptionBuilder starWarsNodeBuilder = new NodeDescriptionBuilder();
        starWarsNodeBuilder
            .domainType("tutorial::StarWarsFan")
            .labelExpression("aql:self.name")
            .name("StarWarsFan Node")
            .semanticCandidatesExpression("aql:self.members")
            .style(this.createStarWarsNodeStyle(colorProvider))
            .userResizable(false)
            .palette(this.createDefaultNodePalette());
        NodeDescriptionBuilder starTrekNodeBuilder = new NodeDescriptionBuilder();
        starTrekNodeBuilder
            .domainType("tutorial::StarTrekFan")
            .labelExpression("aql:self.name")
            .name("StarTrekFan Node")
            .semanticCandidatesExpression("aql:self.members")
            .style(this.createStarTrekNodeStyle(colorProvider))
            .userResizable(false)
            .palette(this.createDefaultNodePalette());
        return List.of(starWarsNodeBuilder.build(), starTrekNodeBuilder.build());
    }

    private NodeStyleDescription createStarWarsNodeStyle(IColorProvider colorProvider) {
        RectangularNodeStyleDescriptionBuilder builder = new RectangularNodeStyleDescriptionBuilder();
        builder
            .borderColor(colorProvider.getColor("starwars_color"))
            .color(colorProvider.getColor("starwars_color"))
            .heightComputationExpression("100")
            .labelColor(colorProvider.getColor("white"))
            .widthComputationExpression("200")
            .withHeader(false);
        return builder.build();
    }

    private NodeStyleDescription createStarTrekNodeStyle(IColorProvider colorProvider) {
        RectangularNodeStyleDescriptionBuilder builder = new RectangularNodeStyleDescriptionBuilder();
        builder
            .borderColor(colorProvider.getColor("startrek_color"))
            .color(colorProvider.getColor("startrek_color"))
            .heightComputationExpression("100")
            .labelColor(colorProvider.getColor("white"))
            .widthComputationExpression("200")
            .withHeader(false);
        return builder.build();
    }

    private List<EdgeDescription> createEdgeDescriptions() {
        return List.of();
    }

    private DiagramPalette createPalette() {
        DiagramPaletteBuilder builder  = new DiagramPaletteBuilder();
        return builder
                .nodeTools(this.createStarWarsFanNodeTool(), this.createStarTrekFanNodeTool())
                .build();
    }

    private NodeTool createStarWarsFanNodeTool() {
        NodeToolBuilder builder = new NodeToolBuilder();

        SetValueBuilder setValue = new SetValueBuilder();
        setValue
            .featureName("name")
            .valueExpression("new Star Wars Fan");

        ChangeContextBuilder changeContext = new ChangeContextBuilder();
        changeContext
            .expression("aql:newStarWarsFan")
            .children(setValue.build());

        CreateInstanceBuilder createInstance =  new CreateInstanceBuilder();
        createInstance
            .typeName("tutorial::StarWarsFan")
            .referenceName("members")
            .variableName("newStarWarsFan")
            .children(changeContext.build());

        return builder
                .name("StarWars Fan")
                .body(createInstance.build())
                .build();
    }

    private NodeTool createStarTrekFanNodeTool() {
        NodeToolBuilder builder = new NodeToolBuilder();

        SetValueBuilder setValue = new SetValueBuilder();
        setValue
            .featureName("name")
            .valueExpression("new Star Trek Fan");

        ChangeContextBuilder changeContext = new ChangeContextBuilder();
        changeContext
            .expression("aql:newStarTrekFan")
            .children(setValue.build());

        CreateInstanceBuilder createInstance =  new CreateInstanceBuilder();
        createInstance
            .typeName("tutorial::StarTrekFan")
            .referenceName("members")
            .variableName("newStarTrekFan")
            .children(changeContext.build());

        return builder
                .name("StarTrek Fan")
                .body(createInstance.build())
                .build();
    }

    private NodePalette createDefaultNodePalette() {
        ChangeContextBuilder changeContext = new ChangeContextBuilder();
        changeContext.expression("aql:self.defaultDelete()");

        DeleteToolBuilder deleteTool = new DeleteToolBuilder();
        deleteTool
            .name("Delete from Model")
            .body(changeContext.build());

        NodePaletteBuilder builder = new NodePaletteBuilder();
        builder.deleteTool(deleteTool.build());
        return builder.build();
    }
}
