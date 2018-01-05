/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
importClass(Packages.java.lang.Class);
importClass(Packages.java.lang.ClassLoader);

importClass(Packages.org.dspace.app.xmlui.utils.FlowscriptUtils);
importClass(Packages.org.dspace.app.xmlui.aspect.submission.FlowUtils);
importClass(Packages.org.dspace.app.xmlui.cocoon.HttpServletRequestCocoonWrapper);
importClass(Packages.org.apache.cocoon.environment.http.HttpEnvironment);
importClass(Packages.org.dspace.app.xmlui.aspect.submission.StepAndPage);
importClass(Packages.org.apache.cocoon.servlet.multipart.Part);

importClass(Packages.org.dspace.handle.service.HandleService);
importClass(Packages.org.dspace.handle.factory.HandleServiceFactory);
importClass(Packages.org.dspace.workflow.WorkflowService);
importClass(Packages.org.dspace.core.Constants);
importClass(Packages.org.dspace.workflow.WorkflowItem);
importClass(Packages.org.dspace.content.WorkspaceItem);
importClass(Packages.org.dspace.content.service.WorkspaceItemService);
importClass(Packages.org.dspace.content.factory.ContentServiceFactory);
importClass(Packages.org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem);
importClass(Packages.org.dspace.authorize.service.AuthorizeService);
importClass(Packages.org.dspace.authorize.factory.AuthorizeServiceFactory);
importClass(Packages.org.dspace.license.service.CreativeCommonsService);
importClass(Packages.org.dspace.workflow.factory.WorkflowServiceFactory);
importClass(Packages.org.dspace.xmlworkflow.factory.XmlWorkflowServiceFactory);
importClass(Packages.org.dspace.xmlworkflow.factory.XmlWorkflowFactory);
importClass(Packages.org.dspace.xmlworkflow.storedcomponents.service.PoolTaskService);
importClass(Packages.org.dspace.xmlworkflow.storedcomponents.ClaimedTask);

importClass(Packages.org.dspace.app.xmlui.utils.ContextUtil);
importClass(Packages.org.dspace.app.xmlui.cocoon.HttpServletRequestCocoonWrapper);

importClass(Packages.org.dspace.eperson.EPerson);

/* Global variable which stores a comma-separated list of all fields
 * which errored out during processing of the last step.
 */
var ERROR_FIELDS = null;


/**
 * Simple access method to access the current cocoon object model.
 */
function getObjectModel()
{
  return FlowscriptUtils.getObjectModel(cocoon);
}

/**
 * Return the DSpace context for this request since each HTTP request generates
 * a new context this object should never be stored and instead always accessed
 * through this method so you are ensured that it is the correct one.
 */
function getDSContext()
{
	return ContextUtil.obtainContext(getObjectModel());
}


/**
 * Return the HTTP Request object for this request
 */
function getHttpRequest()
{
	//return getObjectModel().get(HttpEnvironment.HTTP_REQUEST_OBJECT)

	// Cocoon's request object handles form encoding, thus if the users enters
	// non-ascii characters such as those found in foreign languages they will
	// come through corrupted if they are not obtained through the cocoon request
	// object. However, since the dspace-api is built to accept only HttpServletRequest
	// a wrapper class HttpServletRequestCocoonWrapper has bee built to translate
	// the cocoon request back into a servlet request. This is not a fully complete
	// translation as some methods are unimplemented. But it is enough for our
	// purposes here.
	return new HttpServletRequestCocoonWrapper(getObjectModel());
}

/**
 * Return the HTTP Response object for the response
 * (used for compatibility with DSpace configurable submission system)
 */
function getHttpResponse()
{
	return getObjectModel().get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
}



function getHandleService(handle) {
    var dso = HandleServiceFactory.getInstance().getHandleService().resolveToObject(getDSContext(), handle);
    return dso;
}

function getWorkspaceItemService() {
    return ContentServiceFactory.getInstance().getWorkspaceItemService();
}

function getItemService() {
    return ContentServiceFactory.getInstance().getItemService();
}

function getWorkflowItemService()
{
    return XmlWorkflowServiceFactory.getInstance().getWorkflowItemService();
}

function getXmlWorkflowFactory()
{
    return XmlWorkflowServiceFactory.getInstance().getWorkflowFactory();
}

function getPoolTaskService()
{
    return XmlWorkflowServiceFactory.getInstance().getPoolTaskService();
}

function getXmlWorkflowService()
{
    return XmlWorkflowServiceFactory.getInstance().getXmlWorkflowService();
}

function getClaimedTaskService()
{
    return XmlWorkflowServiceFactory.getInstance().getClaimedTaskService();
}



/**
 * Send the current page and wait for the flow to be continued. Use this method to add
 * a flow=true parameter. This allows the sitemap to filter out all flow related requests
 * from non flow related requests.
 */
function sendPageAndWait(uri,bizData)
{
    if (bizData == null)
        bizData = {};

    // just to remember where we came from.
    bizData["flow"] = "true";
    cocoon.sendPageAndWait(uri,bizData);
}

/**
 * Send the given page and DO NOT wait for the flow to be continued. Execution will
 * proceed as normal. Use this method to add a flow=true parameter. This allows the
 * sitemap to filter out all flow related requests from non flow related requests.
 */
function sendPage(uri,bizData)
{
    if (bizData == null)
        bizData = {};

    // just to remember where we came from.
    bizData["flow"] = "true";
    cocoon.sendPage(uri,bizData);
}


function doEditItemMetadata() 
{
	var handle = cocoon.parameters["handle"];
	var item = getHandleService(handle);
	var xmlWorkflowItem = getWorkflowItemService().findByItem(getDSContext(), item);
	if(xmlWorkflowItem == null){
	    var collection = item.getOwningCollection();
	    var wf = getXmlWorkflowFactory().getWorkflow(collection);
	    xmlWorkflowItem = getWorkflowItemService().create(getDSContext(), item, collection);
	    getWorkflowItemService().update(getDSContext(), xmlWorkflowItem);
	}
	
	var wsi = getWorkspaceItemService().create(getDSContext(), xmlWorkflowItem);

	xmlWorkflowItem=getXmlWorkflowService().start(getDSContext(), wsi);
	

	var poolTaskList = getPoolTaskService().find(getDSContext(), xmlWorkflowItem);
	var usuario=getDSContext().getCurrentUser();
	var redireccionamiento;
	var contextPath = cocoon.request.getContextPath();
	if(poolTaskList.length > 1)
		throw "Existen multiples PoolTasks para el item "+handle;
	
	//esto es para el caso de la edicion de una solapa abierta previamente a la aceptacion de la tarea desde otra solapa
	if(poolTaskList.isEmpty()){
		var claimedTask=getClaimedTaskService().findByWorkflowIdAndEPerson(getDSContext(), xmlWorkflowItem, getDSContext().getCurrentUser());
		if (claimedTask==null){
			redireccionamiento=contextPath+"/handle/"+handle;
		} else {
			redireccionamiento=contextPath+"/handle/"+handle+"/workflow_edit_metadata?"+"workflowID=X"+xmlWorkflowItem.getID()+"&stepID="+claimedTask.getStepID()+"&actionID="+claimedTask.getActionID();
		}
	} else {
		var poolTask = poolTaskList.get(0);
		redireccionamiento=contextPath+"/handle/"+handle+"/xmlworkflow?"+"workflowID="+xmlWorkflowItem.getID()+"&stepID="+poolTask.getStepID()+"&actionID="+poolTask.getActionID();
		
	}
	
	// Enviamos al usuario a la pantalla de aceptación del trabajo
	cocoon.sendPage("submit/finalize");
	cocoon.redirectTo(redireccionamiento, true);
    //getDSContext().complete();
    cocoon.exit();
}
