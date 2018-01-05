package ar.edu.unlp.sedici.dspace.xmlworkflow.state.actions.processingaction;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.xmlworkflow.factory.XmlWorkflowServiceFactory;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.ActionResult;
import org.dspace.xmlworkflow.state.actions.processingaction.ProcessingAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

public class SeDiCISelectCollectionAction extends ProcessingAction {
	protected AuthorizeService authorizeService;
	
	public SeDiCISelectCollectionAction() {
		authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
	}
	
	@Override
	public void activate(Context c, XmlWorkflowItem wf) throws SQLException, IOException, AuthorizeException {
		// No hay nada para activar
	}

	@Override
	public ActionResult execute(Context c, XmlWorkflowItem wfi, Step step, HttpServletRequest request) throws SQLException, AuthorizeException, IOException {
        if(request.getParameter("submit") != null){
            String collectionHandle = request.getParameter("collection_handle");
            if(collectionHandle == null || collectionHandle == "")
            	return new ActionResult(ActionResult.TYPE.TYPE_ERROR);
            
            DSpaceObject dso = HandleServiceFactory.getInstance().getHandleService().resolveToObject(c, collectionHandle);
            if(!(dso instanceof Collection))
            	return new ActionResult(ActionResult.TYPE.TYPE_ERROR);
            
            wfi.setCollection((Collection) dso);
            XmlWorkflowServiceFactory.getInstance().getWorkflowItemService().update(c,wfi);
            
            return new ActionResult(ActionResult.TYPE.TYPE_OUTCOME, ActionResult.OUTCOME_COMPLETE);
        } else {
            //We pressed the leave button so return to our submissions page
            return new ActionResult(ActionResult.TYPE.TYPE_SUBMISSION_PAGE);
        }
	}

}
