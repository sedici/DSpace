package ar.edu.unlp.sedici.dspace.xmlworkflow.state.actions.processingaction;

import java.sql.SQLException;

import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.WorkflowConfigurationException;
import org.dspace.xmlworkflow.state.actions.userassignment.ClaimAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;


/**
 * Behaves like the ClaimAction processing action, except that when the logged user is a target collection's admin, 
 * this step is not executed.
 * 
 * @author nestor
 */
public class SkipAdminClaimAction extends ClaimAction {
	protected AuthorizeService authorizeService;
	
	public SkipAdminClaimAction() {
		authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
	}
	
	@Override
	public boolean isValidUserSelection(Context context, XmlWorkflowItem wfi, boolean hasUI) throws WorkflowConfigurationException, SQLException {
		if(authorizeService.isAdmin(context, wfi.getCollection()))
			return false;
		return super.isValidUserSelection(context, wfi, hasUI);
	}
}
