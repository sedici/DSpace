package ar.edu.unlp.sedici.dspace.dspace.xmlworkflow.state.actions.processingaction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;



import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.workflow.WorkflowException;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.ActionResult;
import org.dspace.xmlworkflow.state.actions.processingaction.ProcessingAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.factory.ContentServiceFactory;



/**
 * Processing class of an action that allows users to
 * select the collection to publish the item
 *
 */

public class SelectCollection extends ProcessingAction {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SelectCollection.class);
	
	
	protected static CollectionService collectionService = ContentServiceFactory.getInstance()
            .getCollectionService();
	
	
	@Override
	public void activate(Context c, XmlWorkflowItem wf)
			throws SQLException, IOException, AuthorizeException, WorkflowException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ActionResult execute(Context c, XmlWorkflowItem wfi, Step step, HttpServletRequest request)throws SQLException, AuthorizeException, IOException, WorkflowException {
		UUID uuid = UUID.fromString(request.getParameter("collection_id"));
		collectionService = ContentServiceFactory.getInstance().getCollectionService();
		if(request.getParameter("submit_selectCollection") != null){
	        Collection co = collectionService.find(c, uuid);
	        if (co != null) {
	        	wfi.setCollection(co);
	        	return new ActionResult(ActionResult.TYPE.TYPE_OUTCOME, ActionResult.OUTCOME_COMPLETE);
	        }
	        return new ActionResult(ActionResult.TYPE.TYPE_ERROR);
	    } else {
	        //We pressed the leave button so return to our submissions page
	        return new ActionResult(ActionResult.TYPE.TYPE_SUBMISSION_PAGE);
	    }
	}

	@Override
	public List<String> getOptions() {
		List<String> options = new ArrayList<>();
		options.add("submit_selectCollection");
		return options;
	}

}
