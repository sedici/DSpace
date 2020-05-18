package ar.edu.unlp.sedici.dspace.xmlworkflow.state.actions.processingaction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.handle.service.HandleService;
import org.dspace.workflow.WorkflowException;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.ActionResult;
import org.dspace.xmlworkflow.state.actions.processingaction.ProcessingAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.service.XmlWorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;

public class SeDiCISelectCollectionAction extends ProcessingAction {

    @Autowired(required = true)
    protected HandleService handleService;
    @Autowired(required = true)
    protected XmlWorkflowItemService xmlWorkflowItemService;

    private static final String SELECT_COLLECTION = "select_collection";

    @Override
    public void activate(Context c, XmlWorkflowItem wf) throws SQLException, IOException, AuthorizeException, WorkflowException {
        // No hay nada para activar
    }

    @Override
    public ActionResult execute(Context c, XmlWorkflowItem wfi, Step step, HttpServletRequest request) throws SQLException, AuthorizeException, IOException, WorkflowException {
        if (super.isOptionInParam(request)) {
            switch (Util.getSubmitButton(request, SUBMIT_CANCEL)) {
                case SELECT_COLLECTION:
                    String collectionHandle = request.getParameter("collection_handle");
                    if(collectionHandle == null || collectionHandle == "")
                        return new ActionResult(ActionResult.TYPE.TYPE_ERROR);
                    DSpaceObject dso = handleService.resolveToObject(c, collectionHandle);
                    if(!(dso instanceof Collection))
                        return new ActionResult(ActionResult.TYPE.TYPE_ERROR);
                    wfi.setCollection((Collection) dso);
                    xmlWorkflowItemService.update(c, wfi);
                    return new ActionResult(ActionResult.TYPE.TYPE_OUTCOME, ActionResult.OUTCOME_COMPLETE);
                default:
                    return new ActionResult(ActionResult.TYPE.TYPE_CANCEL);
            }
        }
        //We pressed the leave button so return to our submissions page
        return new ActionResult(ActionResult.TYPE.TYPE_SUBMISSION_PAGE);
    }

    @Override
    public List<String> getOptions() {
        List<String> options = new ArrayList<>();
        options.add(SELECT_COLLECTION);
        return options;
    }

}
