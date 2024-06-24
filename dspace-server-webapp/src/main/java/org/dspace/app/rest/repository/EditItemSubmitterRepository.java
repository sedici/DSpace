
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

import org.dspace.app.rest.model.EPersonRest;
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
 * Link repository for "submitter" subresource of a workflow item.
 */
@Component(EditItemRest.CATEGORY + "." + EditItemRest.NAME + "." + EditItemRest.SUBMITTER)
public class EditItemSubmitterRepository extends AbstractDSpaceRestRepository
        implements LinkRestRepository {

    @Autowired
    ItemService is;

    /**
     * Retrieve the submitter for a workflow item.
     *
     * @param request          - The current request
     * @param id               - The workflow item ID for which to retrieve the submitter
     * @param optionalPageable - optional pageable object
     * @param projection       - the current projection
     * @return the submitter for the workflow item
     */
    @PreAuthorize("hasPermission(#id, 'WORKFLOWITEM', 'READ')")
    public EPersonRest getEditItemSubmitter(@Nullable HttpServletRequest request, UUID id,
                                                @Nullable Pageable optionalPageable, Projection projection) {
        try {
            Context context = obtainContext();
            Item item = is.find(context, id);
            if (item == null) {
                throw new ResourceNotFoundException("No such workflow item: " + id);
            }

            return converter.toRest(item.getSubmitter(), projection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}