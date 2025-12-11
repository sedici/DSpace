/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import org.dspace.services.ConfigurationService;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.web.ContextUtil;


/**
 * A collection of conditions to be met when uploading Bitstreams.
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class RestrictedUploadConfiguration extends UploadConfiguration{

    /**
     * Construct a bitstream uploading configuration.
     * @param configurationService DSpace configuration provided by the DI container.
     */
    @Inject
    public RestrictedUploadConfiguration(ConfigurationService configurationService) {
        super(configurationService);
    }

   
    @Override
    public List<AccessConditionOption> getOptions() {
    	boolean isAdmin = false;
    	Context context = ContextUtil.obtainCurrentRequestContext();
    	List<Group> groups = context.getCurrentUser().getGroups();
    	isAdmin = groups.stream().anyMatch(g -> g.getName().equals("SeDiCIAdmin"));
    	if (isAdmin) {
    		return super.getOptions();
    	}else {
    		return new ArrayList<AccessConditionOption>();
    	}
    }
}