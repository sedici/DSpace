/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package ar.edu.unlp.sedici.dspace.xmlworkflow.state.actions.userassignment;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.*;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.*;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.userassignment.ClaimAction;
import ar.edu.unlp.sedici.util.AuthorizeUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Processing class for an action where x number of users
 * have to accept a task from a designated pool
 * 
 * @author Bram De Schouwer (bram.deschouwer at dot com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class SelectIfAdmin extends ClaimAction {
	
	@Override
    public boolean isValidUserSelection(Context context, XmlWorkflowItem wfi, boolean hasUI) throws WorkflowConfigurationException, SQLException {
        //A user claim action always needs to have a UI, since somebody needs to be able to claim it
        if(hasUI){
            if (AuthorizeUtil.epersonInGroup(context, 2 ,context.getCurrentUser())||AuthorizeUtil.epersonInGroup(context, 1 ,context.getCurrentUser())) {
            	return true;
            }
            return super.isValidUserSelection(context, wfi, hasUI);
        }else
            return true;

    }
	
	@Override
	public void activate(Context context, XmlWorkflowItem wfItem) throws SQLException, IOException, AuthorizeException {
		Step owningStep = getParent().getStep();
        // Create pooled tasks for the Sedici Submitter
		if (AuthorizeUtil.epersonInGroup(context, 2 ,context.getCurrentUser())||AuthorizeUtil.epersonInGroup(context, 1 ,context.getCurrentUser())){
			RoleMembers rol = new RoleMembers();
			rol.addEPerson(context.getCurrentUser());
            XmlWorkflowManager.createPoolTasks(context, wfItem, rol, owningStep, getParent());
            alertUsersOnActivation(context, wfItem, rol);
        }
        else
        	super.activate(context, wfItem);


    }

}