/**
 * Copyright (C) 2011 SeDiCI <info@sedici.unlp.edu.ar>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ar.edu.unlp.sedici.dspace.submit.step;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.submit.step.CCLicenseStep;

/**
 * CCLicense step for DSpace Submission Process. 
 *
 * Processes the
 * user response to the license.
 * <P>
 * This class performs all the behind-the-scenes processing that
 * this particular step requires.  This class's methods are utilized 
 * by both the JSP-UI and the Manakin XML-UI
 * <P>
 * 
 * @see org.dspace.app.util.SubmissionConfig
 * @see org.dspace.app.util.SubmissionStepConfig
 * @see org.dspace.submit.AbstractProcessingStep
 * 
 * @author Tim Donohue
 * @author Wendy Bossons (based on earlier CC license step work, but updated
 * for DSpace 1.8 and CC web services api + curation)
 * @version $Revision: 6785 $
 */
public class SediciAdministratorCCLicenseStep extends AbstractProcessingStep
{
    /***************************************************************************
     * STATUS / ERROR FLAGS (returned by doProcessing() if an error occurs or
     * additional user interaction may be required)
     * 
      * (Do NOT use status of 0, since it corresponds to STATUS_COMPLETE flag
     * defined in the JSPStepManager class)
     **************************************************************************/
    // user rejected the license
    public static final int STATUS_LICENSE_REJECTED = 1;

    /** log4j logger */
    private static Logger log = Logger.getLogger(CCLicenseStep.class);
    

    public SediciAdministratorCCLicenseStep() {
		// TODO Auto-generated constructor stub
	}
    
    /**
     * Do any processing of the information input by the user, and/or perform
     * step processing (if no user interaction required)
     * <P>
     * It is this method's job to save any data to the underlying database, as
     * necessary, and return error messages (if any) which can then be processed
     * by the appropriate user interface (JSP-UI or XML-UI)
     * <P>
     * NOTE: If this step is a non-interactive step (i.e. requires no UI), then
     * it should perform *all* of its processing in this method!
     * 
     * @param context
     *            current DSpace context
     * @param request
     *            current servlet request object
     * @param response
     *            current servlet response object
     * @param subInfo
     *            submission info object
     * @return Status or error flag which will be processed by
     *         doPostProcessing() below! (if STATUS_COMPLETE or 0 is returned,
     *         no errors occurred!)
     */
    public int doProcessing(Context context, HttpServletRequest request,
            HttpServletResponse response, SubmissionInfo subInfo)
            throws ServletException, IOException, SQLException,
            AuthorizeException,java.io.IOException
    {
	    HttpSession session = request.getSession();		
        String buttonPressed = Util.getSubmitButton(request, NEXT_BUTTON);
        
        if (buttonPressed.equals("submit_grant"))
        {
            return processSediciCC(context, request, response, subInfo);
        }        
        
        if (buttonPressed.startsWith(PROGRESS_BAR_PREFIX) || buttonPressed.equals(PREVIOUS_BUTTON))
        {
            removeRequiredAttributes(session);
        }
        if (buttonPressed.equals(NEXT_BUTTON) || buttonPressed.equals(CANCEL_BUTTON)  || buttonPressed.equals(PREVIOUS_BUTTON))
        {
            return processSediciCC(context, request, response, subInfo);
        }
        else
        {  
            removeRequiredAttributes(session);
            //session.removeAttribute("inProgress");
            return STATUS_COMPLETE;
        }
    }


    /**
     * Process the input from the CC license page using CC Web service
     * 
     * @param context
     *            current DSpace context
     * @param request
     *            current servlet request object
     * @param response
     *            current servlet response object
     * @param subInfo
     *            submission info object
     *
     * 
     * @return Status or error flag which will be processed by
     *         doPostProcessing() below! (if STATUS_COMPLETE or 0 is returned,
     *         no errors occurred!)
     */
    protected int processSediciCC(Context context, HttpServletRequest request,
            HttpServletResponse response, SubmissionInfo subInfo)
            throws ServletException, IOException, SQLException,
            AuthorizeException {
    	
    	String[] uriField=(ConfigurationManager.getProperty("cc.license.uri")).split("\\.");
        String uriFieldSchema=(uriField.length>0)?uriField[0]:null;
    	String uriFieldElement=(uriField.length>1)?uriField[1]:null;
    	String uriFieldQualifier=(uriField.length>2)?uriField[2]:null;
    	
    	String[] uriName=(ConfigurationManager.getProperty("cc.license.name")).split("\\.");
    	String uriNameSchema=(uriName.length>0)?uriName[0]:null;
    	String uriNameElement=(uriName.length>1)?uriName[1]:null;
    	String uriNameQualifier=(uriName.length>2)?uriName[2]:null;
    	
    	//String jurisdictionId = (ConfigurationManager.getProperty("cc.license.jurisdiction") != null) ? ConfigurationManager.getProperty("cc.license.jurisdiction") : "";
    	//String jurisdictionDescription = (ConfigurationManager.getProperty("cc.license.jurisdiction.description") != null) ? ConfigurationManager.getProperty("cc.license.jurisdiction.description") : "";
        
    	Item item = subInfo.getSubmissionItem().getItem();
	    Collection coleccion=subInfo.getSubmissionItem().getCollection();
    	HttpSession session = request.getSession();

        //es administrador de la coleccion a la que se está agregando el item, hay un select
    	String licenseUri = request.getParameter("cc_license_chooser");
	   //limpio los metadatos
	   item.clearMetadata(uriNameSchema, uriNameElement, uriNameQualifier, Item.ANY);
       item.clearMetadata(uriFieldSchema, uriFieldElement, uriFieldQualifier, Item.ANY);
       if (licenseUri!=""){
            String licenseDescription= GetLicenses().get(licenseUri);
	    	//agrego los metadatos
	    	item.addMetadata(uriFieldSchema, uriFieldElement, uriFieldQualifier, null, licenseUri);
	    	item.addMetadata(uriNameSchema, uriNameElement, uriNameQualifier, null, licenseDescription);
	    }
        //actualizo y comiteo
		item.update();
		context.commit();
		removeRequiredAttributes(session);
		return STATUS_COMPLETE;			
    }

	
	private void removeRequiredAttributes(HttpSession session) {
		session.removeAttribute("ccError");
		session.removeAttribute("isFieldRequired");
	}
     
     
     /**
     * Retrieves the number of pages that this "step" extends over. This method
     * is used to build the progress bar.
     * <P>
     * This method may just return 1 for most steps (since most steps consist of
     * a single page). But, it should return a number greater than 1 for any
     * "step" which spans across a number of HTML pages. For example, the
     * configurable "Describe" step (configured using input-forms.xml) overrides
     * this method to return the number of pages that are defined by its
     * configuration file.
     * <P>
     * Steps which are non-interactive (i.e. they do not display an interface to
     * the user) should return a value of 1, so that they are only processed
     * once!
     * 
     * @param request
     *            The HTTP Request
     * @param subInfo
     *            The current submission information object
     * 
     * @return the number of pages in this step
     */
    public int getNumberOfPages(HttpServletRequest request,
            SubmissionInfo subInfo) throws ServletException
    {
		return 1;
    }
    

    private static final String PropertiesFilename = "sedici-dspace";
	private static HashMap<String, String> Licencias=null;
	public static HashMap<String, String> GetLicenses(){
		if (Licencias==null || Licencias.size()<=1){
				//cargo el map con las licencias
				Licencias=new HashMap<String, String>();
			    String values=ConfigurationManager.getProperty(PropertiesFilename, "map.licenses");
		        String[] valores=values.split(",");
		        String[] valor;
		        for (String entrada : valores) {
					valor=entrada.split(" = ");
					Licencias.put(valor[0], valor[1]);
			}

		};
		return Licencias;
	}
   
}