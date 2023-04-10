
/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package ar.edu.unlp.sedici.dspace.uploader;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.content.DSpaceObject;
import org.dspace.discovery.SolrServiceIndexPlugin;
import org.apache.log4j.Logger;

/**
 * <p>
 * Adds filenames and file descriptions of all files in the ORIGINAL bundle
 * to the Solr search index.
 *
 * <p>
 * To activate the plugin, add the following line to discovery.xml
 * <pre>
 * {@code <bean id="solrServiceFileInfoPlugin" class="org.dspace.discovery.SolrServiceFileInfoPlugin"/>}
 * </pre>
 *
 * <p>
 * After activating the plugin, rebuild the discovery index by executing:
 * <pre>
 * [dspace]/bin/dspace index-discovery -b
 * </pre>
 *
 */

 
public class SolrServiceYoutubeBitstreamPlugin implements SolrServiceIndexPlugin{
    private static final String BUNDLE_NAME = "ORIGINAL";
    private static final String SOLR_FIELD_NAME_FOR_YOUTUBEID = "original_bundle_youtubeid";
    private static final Logger log = Logger.getLogger(VideoUploaderServiceImpl.class);


    @Override
    public void additionalIndex(Context context, DSpaceObject dso, SolrInputDocument document) {
        try{    
            if (dso instanceof Item) {
                /*Bitstream bitstream = ((Bitstream) dso);
                Bundle[] bundlesi = null;
                Bundle[] bundlesb = bitstream.getBundles();
                for (Bundle bundle : bundlesb){
                    if((bundle.getName() != null) && bundle.getName().equals(BUNDLE_NAME)){
                        Item[] item = bundle.getItems();
                        bundlesi = item[0].getBundles();

                    }
                }*/
                Item item= (Item) dso;
                Bundle[] bundles = item.getBundles();
                if (bundles != null ){
                    for (Bundle bundle:bundles){
                        if((bundle.getName() != null) && bundle.getName().equals(BUNDLE_NAME)){
                            Bitstream[] bitstreams = bundle.getBitstreams();
                            if (bitstreams != null) {
                                for (Bitstream bstream : bitstreams) {
                                    String youtubeId = bstream.getMetadata("sedici.identifier.youtubeId");
                                    if (youtubeId != null){
                                        document.addField(SOLR_FIELD_NAME_FOR_YOUTUBEID, youtubeId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            log.error(e.getMessage(), e);
        }
    }
}
