/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.UUID;

import javax.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.EditItemConverter;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.EditItemRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.core.Context;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Link repository for "collection" subresource of a workflow item.
 */
@Component(EditItemRest.CATEGORY + "." + EditItemRest.NAME + "." + EditItemRest.COLLECTION)
public class EditItemCollectionLInkRepository extends AbstractDSpaceRestRepository
        implements LinkRestRepository {

    @Autowired
    ItemService is;
    
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EditItemCollectionLInkRepository.class);

    /**
     * Retrieve the item for a workflow collection.
     *
     * @param request          - The current request
     * @param id               - The workflow item ID for which to retrieve the collection
     * @param optionalPageable - optional pageable object
     * @param projection       - the current projection
     * @return the item for the workflow collection
     */
    //@PreAuthorize("hasPermission(#id, 'WORKFLOWITEM', 'READ')")
    public CollectionRest getEditItemCollection(@Nullable HttpServletRequest request, UUID id,
                                                    @Nullable Pageable optionalPageable, Projection projection) {
        try {
        	log.info("-----------------------");
        	log.info(id);
            Context context = obtainContext();
            Item item = is.find(context, id);
            if (item == null) {
            	
                throw new ResourceNotFoundException("No such item: " + id);
            }

            return converter.toRest(item.getCollections().get(0), projection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}