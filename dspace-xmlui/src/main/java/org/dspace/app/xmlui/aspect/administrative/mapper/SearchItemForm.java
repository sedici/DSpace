/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.administrative.mapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.CheckBox;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.content.Collection;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.PluginConfigurationError;
import org.dspace.core.PluginManager;
import org.dspace.handle.HandleManager;
import org.dspace.search.DSQuery;
import org.dspace.search.QueryArgs;
import org.dspace.search.QueryResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Search for items from other collections to map into this collection.
 * 
 * @author Scott Phillips
 */

public class SearchItemForm extends AbstractDSpaceTransformer {

	/** Language strings */
	private static final Message T_dspace_home = message("xmlui.general.dspace_home");
	private static final Message T_submit_cancel = message("xmlui.general.cancel");
	private static final Message T_mapper_trail = message("xmlui.administrative.mapper.general.mapper_trail");
	
	private static final Message T_title = message("xmlui.administrative.mapper.SearchItemForm.title");
	private static final Message T_trail = message("xmlui.administrative.mapper.SearchItemForm.trail");
	private static final Message T_head1 = message("xmlui.administrative.mapper.SearchItemForm.head1");
	private static final Message T_submit_map = message("xmlui.administrative.mapper.SearchItemForm.submit_map");
	private static final Message T_column1 = message("xmlui.administrative.mapper.SearchItemForm.column1");
	private static final Message T_column2 = message("xmlui.administrative.mapper.SearchItemForm.column2");
	private static final Message T_column3 = message("xmlui.administrative.mapper.SearchItemForm.column3");
	private static final Message T_column4 = message("xmlui.administrative.mapper.SearchItemForm.column4");

    private static final Logger log = LoggerFactory.getLogger(SearchItemForm.class);

    @Override
	public void addPageMeta(PageMeta pageMeta) throws WingException  
	{
		pageMeta.addMetadata("title").addContent(T_title);
		
		pageMeta.addTrailLink(contextPath + "/", T_dspace_home);
		pageMeta.addTrail().addContent(T_mapper_trail);
		pageMeta.addTrail().addContent(T_trail);
	}

	
    @Override
	public void addBody(Body body) throws SAXException, WingException, SQLException, IOException
	{
		// Get our parameters and state;
		int collectionID = parameters.getParameterAsInteger("collectionID",-1);
		Collection collection = Collection.find(context,collectionID);	
		
		String query = decodeFromURL(parameters.getParameter("query",null));
		java.util.List<Item> items = performSearch(collection,query);
		
		
		
		// DIVISION: manage-mapper
		Division div = body.addInteractiveDivision("search-items",contextPath + "/admin/mapper", Division.METHOD_GET,"primary administrative mapper");
		div.setHead(T_head1.parameterize(query));
		
		Para actions = div.addPara();
		actions.addButton("submit_map").setValue(T_submit_map);
		actions.addButton("submit_cancel").setValue(T_submit_cancel);
		
		Table table = div.addTable("search-items-table",1,1);
		
		Row header = table.addRow(Row.ROLE_HEADER);
		header.addCellContent(T_column1);
		header.addCellContent(T_column2);
		header.addCellContent(T_column3);
		header.addCellContent(T_column4);
		
		for (Item item : items)
		{
			String itemID = String.valueOf(item.getID());
			Collection owningCollection = item.getOwningCollection();
			String owning = "unknown";
			if (owningCollection != null)
				owning = owningCollection.getMetadata("name");
			String author = "unknown";
			//DCValue[] dcCreators = item.getDC("creator",Item.ANY,Item.ANY);
			DCValue[] dcCreators = item.getMetadata("sedici","creator",Item.ANY,Item.ANY);
			
			if (dcCreators != null && dcCreators.length >= 1)
            {
                author = dcCreators[0].value;
            } else {
            	// Do a fall back look for contributors
				DCValue[] dcContributors = item.getMetadata("sedici","contributor",Item.ANY,Item.ANY);
				if (dcContributors != null && dcContributors.length >= 1)
	            {
	                author = dcContributors[0].value;
	            }else{
	            	DCValue[] dcCompiler = item.getMetadata("sedici","compiler",Item.ANY,Item.ANY);
					if (dcCompiler != null && dcCompiler.length >= 1)
		            {
		                author = dcCompiler[0].value;
		            }else{
		            	DCValue[] dcEditor = item.getMetadata("sedici","editor",Item.ANY,Item.ANY);
						if (dcEditor != null && dcEditor.length >= 1)
			            {
			                author = dcEditor[0].value;
			            }
					
		            }	
	            }
			}
			
			String title = "untitled";
			DCValue[] dcTitles = item.getDC("title",null,Item.ANY);
			if (dcTitles != null && dcTitles.length >= 1)
            {
                title = dcTitles[0].value;
            }

			String url = contextPath+"/handle/"+item.getHandle();
			
			Row row = table.addRow();

            boolean canBeMapped = true;
            Collection[] collections = item.getCollections();
            for (Collection c : collections)
            {
                if (c.getID() == collectionID)
                {
                    canBeMapped = false;
                }
            }

            if (canBeMapped)
            {
                CheckBox select = row.addCell().addCheckBox("itemID");
                select.setLabel("Select");
                select.addOption(itemID);
            }
            else
            {
                row.addCell().addContent("");
            }

			row.addCellContent(owning);
			row.addCell().addXref(url,author);
			row.addCell().addXref(url,title);
		}
		
		actions = div.addPara();
		actions.addButton("submit_map").setValue(T_submit_map);
		actions.addButton("submit_cancel").setValue(T_submit_cancel);
		
		
		
		
		div.addHidden("administrative-continue").setValue(knot.getId());
	}
	
	
	/**
	 * Search the repository for items in other collections that can be mapped into this one.
	 * 
	 * @param collection The collection to map into
	 * @param query The search query.
	 */
	private java.util.List<Item> performSearch(Collection collection, String query) throws SQLException, IOException
	{
        // Which search provider do we use?
        SearchRequestProcessor processor = null;
        try {
            processor = (SearchRequestProcessor) PluginManager
                    .getSinglePlugin(SearchRequestProcessor.class);
        } catch (PluginConfigurationError e) {
            log.warn("{} not properly configured.  Please configure the {} plugin.  {}",
                    new Object[] {
                        SearchItemForm.class.getName(),
                        SearchRequestProcessor.class.getName(),
                        e.getMessage()
                    });
        }
        if (processor == null)
        {   // Discovery is the default search provider since DSpace 4.0
            processor = new DiscoverySearchRequestProcessor();
        }

        // Search the repository
        List<DSpaceObject> results = processor.doItemMapSearch(context, query, collection);

        // Get a list of found items
        ArrayList<Item> items = new ArrayList<Item>();
        for (DSpaceObject resultDSO : results)
        {
            if (resultDSO instanceof Item)
            {
            	Item item = (Item) resultDSO;

            	if (!item.isOwningCollection(collection))
                {
                    items.add(item);
                }
            }
        }

        return items;
    }

}
