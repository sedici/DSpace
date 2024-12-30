package ar.edu.unlp.sedici.dspace.dspace.xmlworkflow.state.actions.processingaction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchemaEnum;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;
import org.dspace.xmlworkflow.factory.XmlWorkflowServiceFactory;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.ActionResult;
import org.dspace.xmlworkflow.state.actions.processingaction.ProcessingAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

/* 
   Clase usada en aquellos casos donde se deba mapear alguna accion, 
   pero realmente se quiera terminar la ejecucion del workflow.
   Esto se debe a que cuando se mapean distintos outcomes en el workflow, 
   no se puede mapear al final de la ejecucion ni a una accion vacia, solo se puede mapear acciones
*/
public class NoOpAction extends ProcessingAction {


    @Override
    public void activate(Context c, XmlWorkflowItem wf) {

    }

    @Override
    public ActionResult execute(Context c, XmlWorkflowItem wfi, Step step, HttpServletRequest request)
        throws SQLException, AuthorizeException, IOException {
    	// TODO Agregar logica pre publicacion
    	return new ActionResult(ActionResult.TYPE.TYPE_OUTCOME, ActionResult.OUTCOME_COMPLETE);
        }

	@Override
	public List<String> getOptions() {
		return null;
	}
}