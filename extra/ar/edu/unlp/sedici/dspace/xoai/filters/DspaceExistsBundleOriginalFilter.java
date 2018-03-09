package ar.edu.unlp.sedici.dspace.xoai.filters;

import java.util.Iterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.xoai.data.DSpaceItem;
import org.dspace.xoai.filter.DSpaceFilter;
import org.dspace.xoai.filter.results.SolrFilterResult;
import org.dspace.xoai.filter.results.DatabaseFilterResult;

import com.lyncode.xoai.dataprovider.xml.xoai.Element;
import com.lyncode.xoai.dataprovider.xml.xoai.Element.Field;

/*
 * @author sedici.unlp.edu.ar - UNLP
 * 
 * This Filter class evaluates if at least any bitstreams is in the bundle ORIGINAL.
 * It can be done in 2 different manners, depending of what instance we are:
 * 		 **in SOLR query or
 * 		 **in an XOAI's decision about to "show" an SolrItem or not.
 * 
 *   In the Solr Query instance we ask about the existence of the dc.format.mimetype metadata.
 *   In the XOAI's decision we ask about the bundle ORIGINAL in the set of bundles. 
 */
 
public class DspaceExistsBundleOriginalFilter extends DSpaceFilter{

	private static Logger log = LogManager
            .getLogger(DspaceExistsBundleOriginalFilter.class);

	/*
	 * dc.format.mimetype is a metadata generated by XOAI at time of add a Document to SOLR XOAI.
	 * @see org.dspace.xoai.app.XOAI
	 */
	public static final String solrQueryField = "dc.format.mimetype";
	
	/* Public methods*/
	@Override
	public DatabaseFilterResult buildDatabaseQuery(Context context) {
	/*
	 * A partir de ésta clase se forma el query sobre la Base de Datos utilizando la tabla <<item>>
	 * @see org.dspace.xoai.data.DSpaceItemDatabaseRepository#getItems(List<Filter>, int, int)	
	 */
		String bundleFilterName = new String("ORIGINAL"); 
		try
        {
            return new DatabaseFilterResult(
                    "EXISTS (SELECT distinct r.bundle_id FROM" + 
                    		"(SELECT a.bundle_id FROM" + 
                    			"(SELECT * FROM item it inner join item2bundle i2b on (it.item_id = i2b.item_id)" + 
                    			"where in_archive and it.item_id = i.item_id)" +                    		
							"as a inner join bundle b on a.bundle_id = b.bundle_id " +
							" WHERE name = ?)" + 
					"as r inner join bundle2bitstream as b2b on r.bundle_id = b2b.bundle_id " +
					" ORDER BY r.bundle_id)", bundleFilterName);
        }
		catch (Exception e)
		{
          log.error(e.getMessage(), e);
		}
        return new DatabaseFilterResult();
	}

	@Override
	public boolean isShown(DSpaceItem item) {
		 return hasAnyBundleOriginal(item);
	}
	
	@Override
	public SolrFilterResult buildSolrQuery(){
		return new SolrFilterResult("metadata."+ DspaceExistsBundleOriginalFilter.solrQueryField +":[* TO *]");
	}
    
	/* Private methods */
	
	/*
	 * This method filter a "original bundle" of an item using the metadata bundles.bundle. 
	 * This is a metadata field generated based in all the bitstreams associated to a item
     * that is imported by XOAI ({dspace_install}/bin/dspace xoai import). 
	 */
	private boolean hasAnyBundleOriginal(DSpaceItem item){
	/*
	 * @return Devuelve true si existen algún <<Bundle ORIGINAL>>.
	 */
		Iterator<Element> metadata = item.getMetadata().getMetadata().getElement().iterator();
		while(metadata.hasNext()){
			Element metadataElement = metadata.next();
			if (metadataElement.getName().equals("bundles")){
				Iterator<Element> bundles = metadataElement.getElement().iterator();
				while(bundles.hasNext()){
					Element bundle = bundles.next();
					if (bundle.getName().equals("bundle")){
						//Cada bundle se compone de un field <<name>> indicando si es ORIGINAL, TEXT, etc
						Iterator<Field> fieldIter = bundle.getField().iterator();
						while(fieldIter.hasNext()){
							Field field = fieldIter.next();
							if(field.getName().equals("name") && field.getValue().equals("ORIGINAL")){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
}