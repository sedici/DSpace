package org.dspace.xmlworkflow.state.actions.processingaction;

import java.sql.SQLException;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.WorkflowConfigurationException;
import org.dspace.xmlworkflow.state.actions.userassignment.ClaimAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.springframework.beans.factory.annotation.Autowired;


public class SediciAccepRoleOrNotAdmin extends ClaimAction{
	
    @Autowired
    private AuthorizeService authorizeService;

	/*
	 * If current user is member of the role of the current step, or is not ADMIN of the collection or 
	 * an upper community,then evaluate if exists users that can handle the actions of the current step.
	 */
    @Override
	public boolean isValidUserSelection(Context ctx, XmlWorkflowItem wfi, boolean hasUI) throws WorkflowConfigurationException, SQLException {
		boolean authsIsTurnedOff = ctx.ignoreAuthorization();
		//If authorizaton system was initially turned off, then restore it...
		if(authsIsTurnedOff) {
			ctx.restoreAuthSystemState();
		}
		boolean isValid;
		if (!authorizeService.isAdmin(ctx) && !authorizeService.isAdmin(ctx, wfi.getCollection()))
			isValid = super.isValidUserSelection(ctx, wfi, hasUI);
		else
			isValid = false;
		//Finally, if authorizaton system was initially turned off, then turn off again...
		if(authsIsTurnedOff) {
			ctx.turnOffAuthorisationSystem();
		}
		return isValid;
	}
}
