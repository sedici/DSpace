/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.model.EditItemRest;
import org.dspace.app.rest.model.SubmissionDefinitionRest;
import org.dspace.app.rest.model.SubmissionSectionRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.submit.DataProcessingStep;
import org.dspace.app.util.SubmissionConfigReaderException;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.Collection;
import org.dspace.content.edit.EditItem;
import org.dspace.discovery.IndexableObject;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.dspace.content.Item;

/**
 * @author Danilo Di Nuzzo (danilo.dinuzzo at 4science.it)
 *
 */
@Component
public class EditItemConverter
    extends AInprogressItemConverter<EditItem, org.dspace.app.rest.model.EditItemRest> {
	
	 private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EditItemConverter.class);

	@Autowired
    @Lazy
    private ConverterService converter;
	
	@Autowired
    private SubmissionSectionConverter submissionSectionConverter;

    public EditItemConverter() throws SubmissionConfigReaderException {
        super();
    }

    /* (non-Javadoc)
     * @see org.dspace.app.rest.converter.IndexableObjectConverter#supportsModel(org.dspace.discovery.IndexableObject)
     */
    @Override
    public boolean supportsModel(IndexableObject idxo) {
        return idxo.getIndexedObject() instanceof EditItem;
    }

    /* (non-Javadoc)
     * @see org.dspace.app.rest.converter.DSpaceConverter#
     * convert(java.lang.Object, org.dspace.app.rest.projection.Projection)
     */
    @Override
    public EditItemRest convert(EditItem modelObject, Projection projection) {
        EditItemRest rest = new EditItemRest();
        rest.setProjection(projection);
        fillFromModel(modelObject, rest, projection);
        return rest;
    }

    protected void fillFromModel(EditItem obj, EditItemRest rest, Projection projection) {
        Collection collection = obj.getCollection();
        Item item = obj.getItem();
        EPerson submitter = null;
        submitter = obj.getSubmitter();
        
        rest.setId(obj.getID());
        
        SubmissionDefinitionRest def = converter.toRest(
                submissionConfigService.getSubmissionConfigByName("traditional"), projection);
        rest.setSubmissionDefinition(def);
        //storeSubmissionName(def.getName());
        for (SubmissionSectionRest sections : def.getPanels()) {
            SubmissionStepConfig stepConfig = submissionSectionConverter.toModel(sections);

            if (stepConfig.isHiddenForInProgressSubmission(obj)) {
                continue;
            }

            /*
             * First, load the step processing class (using the current
             * class loader)
             */
            ClassLoader loader = this.getClass().getClassLoader();
            Class stepClass;
            try {
                stepClass = loader.loadClass(stepConfig.getProcessingClassName());

                Object stepInstance = stepClass.newInstance();

                if (stepInstance instanceof DataProcessingStep) {
                    // load the interface for this step
                    DataProcessingStep stepProcessing = (DataProcessingStep) stepClass.newInstance();

                    rest.getSections()
                        .put(sections.getId(), stepProcessing.getData(submissionService, obj, stepConfig));
                } else {
                    log.warn("The submission step class specified by '" + stepConfig.getProcessingClassName() +
                             "' does not extend the class org.dspace.app.rest.submit.AbstractRestProcessingStep!" +
                             " Therefore it cannot be used by the Configurable Submission as the " +
                             "<processing-class>!");
                }

            } catch (Exception e) {
                log.error("An error occurred during the unmarshal of the data for the section " + sections.getId()
                        + " - reported error: " + e.getMessage(), e);
            }

        }
 
        rest.setCollection(collection != null ? converter.toRest(collection, projection) : null);
        rest.setItem(converter.toRest(item, projection));
        if (Objects.nonNull(submitter)) {
            rest.setSubmitter(converter.toRest(submitter, projection));
        }
    }

    /* (non-Javadoc)
     * @see org.dspace.app.rest.converter.DSpaceConverter#getModelClass()
     */
    @Override
    public Class<EditItem> getModelClass() {
        return EditItem.class;
    }
}
