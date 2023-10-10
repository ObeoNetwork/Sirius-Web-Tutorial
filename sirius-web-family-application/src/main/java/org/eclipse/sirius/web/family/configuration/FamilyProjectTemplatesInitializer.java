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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramCreationService;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.EditingContext;
import org.eclipse.sirius.components.emf.services.IEditingContextEPackageService;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.persistence.entities.DocumentEntity;
import org.eclipse.sirius.web.persistence.repositories.IDocumentRepository;
import org.eclipse.sirius.web.persistence.repositories.IProjectRepository;
import org.eclipse.sirius.web.services.api.id.IDParser;
import org.eclipse.sirius.web.services.api.projects.IProjectTemplateInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Provides Family-specific project templates initializers.
 *
 * @author arichard
 */
@Configuration
public class FamilyProjectTemplatesInitializer implements IProjectTemplateInitializer {
    private static final String DOCUMENT_TITLE = "FamilyNewModel";

    private final Logger logger = LoggerFactory.getLogger(FamilyProjectTemplatesInitializer.class);

    private final IProjectRepository projectRepository;

    private final IDocumentRepository documentRepository;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IDiagramCreationService diagramCreationService;

    private final IRepresentationPersistenceService representationPersistenceService;

    private final IEditingContextEPackageService editingContextEPackageService;

    private final StereotypeBuilder stereotypeBuilder;

    public FamilyProjectTemplatesInitializer(IProjectRepository projectRepository, IDocumentRepository documentRepository, IRepresentationDescriptionSearchService representationDescriptionSearchService,
                                           IDiagramCreationService diagramCreationService, IRepresentationPersistenceService representationPersistenceService, IEditingContextEPackageService editingContextEPackageService, MeterRegistry meterRegistry) {
        this.projectRepository = Objects.requireNonNull(projectRepository);
        this.documentRepository = Objects.requireNonNull(documentRepository);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.diagramCreationService = Objects.requireNonNull(diagramCreationService);
        this.representationPersistenceService = Objects.requireNonNull(representationPersistenceService);
        this.editingContextEPackageService = Objects.requireNonNull(editingContextEPackageService);
        this.stereotypeBuilder = new StereotypeBuilder("family-template-initializer", meterRegistry);
    }

    @Override
    public boolean canHandle(String templateId) {
        return FamilyProjectTemplatesProvider.FAMILY_TEMPLATE_ID.equals(templateId);
    }

    @Override
    public Optional<RepresentationMetadata> handle(String templateId, IEditingContext editingContext) {
        if (FamilyProjectTemplatesProvider.FAMILY_TEMPLATE_ID.equals(templateId)) {
            return this.initializeFamilyProject(editingContext);
        }
        return Optional.empty();
    }

    private Optional<RepresentationMetadata> initializeFamilyProject(IEditingContext editingContext) {
        Optional<RepresentationMetadata> result = Optional.empty();
        // @formatter:off
        Optional<AdapterFactoryEditingDomain> optionalEditingDomain = Optional.of(editingContext)
                .filter(EditingContext.class::isInstance)
                .map(EditingContext.class::cast)
                .map(EditingContext::getDomain);
        // @formatter:on
        Optional<UUID> editingContextUUID = new IDParser().parse(editingContext.getId());
        if (optionalEditingDomain.isPresent() && editingContextUUID.isPresent()) {
            AdapterFactoryEditingDomain adapterFactoryEditingDomain = optionalEditingDomain.get();
            ResourceSet resourceSet = adapterFactoryEditingDomain.getResourceSet();

            EPackage familyEPackage = this.getTutorialEPackage(editingContext.getId());

            var optionalDocumentEntity = this.projectRepository.findById(editingContextUUID.get()).map(projectEntity -> {
                DocumentEntity documentEntity = new DocumentEntity();
                documentEntity.setProject(projectEntity);
                documentEntity.setName(DOCUMENT_TITLE);
                documentEntity.setContent(this.getNewFamilyContent(familyEPackage));

                documentEntity = this.documentRepository.save(documentEntity);
                return documentEntity;
            });

            if (optionalDocumentEntity.isPresent()) {
                DocumentEntity documentEntity = optionalDocumentEntity.get();

                JSONResourceFactory jsonResourceFactory = new JSONResourceFactory();
                JsonResource resource = jsonResourceFactory.createResourceFromPath(documentEntity.getId().toString());
                resourceSet.getResources().add(resource);

                try (var inputStream = new ByteArrayInputStream(documentEntity.getContent().getBytes())) {
                    resource.load(inputStream, null);

                    var optionalFamilyDiagram = this.findDiagramDescription(editingContext, "Family Diagram Description");
                    if (optionalFamilyDiagram.isPresent()) {
                        DiagramDescription familyDiagram = optionalFamilyDiagram.get();
                        Object semanticTarget = resource.getContents().get(0);

                        Diagram diagram = this.diagramCreationService.create(familyDiagram.getLabel(), semanticTarget, familyDiagram, editingContext);
                        this.representationPersistenceService.save(editingContext, diagram);

                        result = Optional.of(new RepresentationMetadata(diagram.getId(), diagram.getKind(), diagram.getLabel(), diagram.getDescriptionId()));
                    }
                } catch (IOException exception) {
                    this.logger.warn(exception.getMessage(), exception);
                }

                resource.eAdapters().add(new ResourceMetadataAdapter(DOCUMENT_TITLE));

                resourceSet.getResources().add(resource);
            }
        }
        return result;
    }

    private EPackage getTutorialEPackage(String editingContextId) {
        List<EPackage> ePackages = this.editingContextEPackageService.getEPackages(editingContextId);
        return ePackages.stream().filter(ePakg -> ePakg.getName().equals("tutorial")).findFirst().orElse(null);
    }

    private String getNewFamilyContent(EPackage tutorialPackage) {
        EObject family = EcoreUtil.create((EClass) tutorialPackage.getEClassifier("Family"));
        family.eSet(family.eClass().getEStructuralFeature("name"), "NewFamily");

        EObject starTrekFan = EcoreUtil.create((EClass) tutorialPackage.getEClassifier("StarTrekFan"));
        starTrekFan.eSet(starTrekFan.eClass().getEStructuralFeature("name"), "NewTrekFan");

        EObject starWarsFan = EcoreUtil.create((EClass) tutorialPackage.getEClassifier("StarWarsFan"));
        starWarsFan.eSet(starWarsFan.eClass().getEStructuralFeature("name"), "NewStarWarsFan");

        family.eSet(family.eClass().getEStructuralFeature("members"), List.of(starTrekFan, starWarsFan));

        return this.stereotypeBuilder.getStereotypeBody(List.of(family));
    }

    private Optional<DiagramDescription> findDiagramDescription(IEditingContext editingContext, String label) {
        // @formatter:off
        return this.representationDescriptionSearchService.findAll(editingContext).values().stream()
                .filter(DiagramDescription.class::isInstance)
                .map(DiagramDescription.class::cast)
                .filter(diagramDescrpition -> Objects.equals(label, diagramDescrpition.getLabel()))
                .findFirst();
        // @formatter:on
    }

}
