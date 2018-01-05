package ar.edu.unlp.sedici.aspect.collectionViewer;


import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.util.HashUtil;
import org.apache.excalibur.source.SourceValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.DSpaceValidity;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Options;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Constants;
import org.xml.sax.SAXException;

/**
 * 
 * Create the navigation options for everything in the administrative aspects. This includes 
 * Epeople, group, item, access control, and registry management.
 * 
 * @author Scott Phillips
 * @author Afonso Araujo Neto (internationalization)
 * @author Alexey Maslov
 * @author Jay Paz
 */
public class Navigation extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    private static final Message T_context_add_item = message("sedici.aspect.collectionViewer.addItem");
    private static final Message T_context_head= message("xmlui.administrative.Navigation.context_head");
    /** Cached validity object */
	private SourceValidity validity;
	
	/** exports available for download */
	java.util.List<String> availableExports = null;
	
   public Serializable getKey()
   {
       try
       {
           DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

           if (dso == null)
           {
               return "0";
           }
               
           return HashUtil.hash(dso.getHandle());
       }
       catch (SQLException sqle)
       {
           // Ignore all errors and just return that the component is not
           // cachable.
           return "0";
       }
   }
	
    public SourceValidity getValidity()
    {
    	if (this.validity == null)
    	{
            Collection collection = null;
 	        try
 	        {
 	            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
 	
 	            if (dso == null)
                {
                    return null;
                }
 	
 	            if (!(dso instanceof Collection))
                {
                    return null;
                }
 	
 	            collection = (Collection) dso;
 	
 	            DSpaceValidity validity = new DSpaceValidity();
 	            
 	            // Add the actual collection;
 	            validity.add(context, collection);
 	
 	            this.validity = validity.complete();
 	        }
 	        catch (Exception e)
 	        {
 	            // Just ignore all errors and return an invalid cache.
 	        }

    	}
    	return this.validity;
    }
	
   public void addOptions(Options options) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
	   
    	/* Create skeleton menu structure to ensure consistent order between aspects,
    	 * even if they are never used 
    	 */
        options.addList("browse");
        options.addList("account");
        List context = options.addList("context");
        options.addList("administrative");
        
        // Context Administrative options
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        
    	if (dso instanceof Collection){
    		Collection collection = (Collection) dso;

    		//collections = Collection.findAuthorized(context, null, Constants.ADD);
    		
            if (AuthorizeServiceFactory.getInstance().getAuthorizeService().authorizeActionBoolean(this.context, dso, Constants.ADD)){
            	    context.setHead(T_context_head);
                    context.addItem().addXref(contextPath+"/handle/"+collection.getHandle()+"/submit", T_context_add_item );
            }
    	}  	     

    }
     
    /**
     * recycle
     */
    public void recycle()
    {
        this.validity = null;
        super.recycle();
    }
    
}
