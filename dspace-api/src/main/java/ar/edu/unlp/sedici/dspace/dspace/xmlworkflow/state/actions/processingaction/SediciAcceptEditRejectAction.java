package ar.edu.unlp.sedici.dspace.dspace.xmlworkflow.state.actions.processingaction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.ActionResult;
import org.dspace.xmlworkflow.state.actions.processingaction.AcceptEditRejectAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

import jakarta.servlet.http.HttpServletRequest;

public class SediciAcceptEditRejectAction extends AcceptEditRejectAction {
	
	private boolean optionalSelectCollection;
	
	public boolean getOptionalSelectCollection() {
		return optionalSelectCollection;
	}

	public void setOptionalSelectCollection(boolean optionalSelectCollection) {
		this.optionalSelectCollection = optionalSelectCollection;
	}

	protected static final String SUBMITT_APPROVE_NOT_SELECT = "submit_approve_not_select";
    protected static final String SUBMITT_APPROVE_SELECT ="submit_approve_and_select";
	
	@Override
    public List<String> getOptions() {
	    List<String> options = super.getOptions();
	    if (optionalSelectCollection) { 
	    	options.remove(SUBMIT_APPROVE);
		    options.add(SUBMITT_APPROVE_NOT_SELECT);
	    };
	    options.add(SUBMITT_APPROVE_SELECT);
	    return options;
	}
	
	@Override
	public ActionResult execute(Context c, XmlWorkflowItem wfi, Step step, HttpServletRequest request)
            throws SQLException, AuthorizeException, IOException {
        if (super.isOptionInParam(request)) {
            switch (Util.getSubmitButton(request, SUBMIT_CANCEL)) {
                case SUBMIT_APPROVE:
                    return processAccept(c, wfi);
                case SUBMITT_APPROVE_SELECT:
                	return processAccept(c, wfi);
                case SUBMITT_APPROVE_NOT_SELECT:
                	super.addApprovedProvenance(c, wfi);
                    return new ActionResult(ActionResult.TYPE.TYPE_OUTCOME, 1);
                case SUBMIT_REJECT:
                    return super.processRejectPage(c, wfi, request);
                default:
                    return new ActionResult(ActionResult.TYPE.TYPE_CANCEL);
            }
        }
        return new ActionResult(ActionResult.TYPE.TYPE_CANCEL);
    }

}
