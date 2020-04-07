/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics.util;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.dspace.core.ConfigurationManager;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpiderDetector delegates static methods to SpiderDetectorService, which is used to find IP's that are spiders...
 *
 * @author kevinvandevelde at atmire.com
 * @author ben at atmire.com
 * @author Mark Diggory (mdiggory at atmire.com)
 * @author frederic at atmire.com
 */
public class SpiderDetector {

    private static final Logger log = LoggerFactory.getLogger(SpiderDetector.class);

    //Service where all methods get delegated to, this is instantiated by a spring-bean defined in core-services.xml
    private static SpiderDetectorService spiderDetectorService = StatisticsServiceFactory.getInstance()
                                                                                         .getSpiderDetectorService();
    /** Directory name where the spider pattern files by agents are located. */
    public static String SPIDER_AGENTS_DIRNAME = "agents";

    /**
     * Default constructor
     */
    private SpiderDetector() { }

    /**
     * Get an immutable Set representing all the Spider Addresses here
     *
     * @return a set of IP addresses as strings
     */
    public static Set<String> getSpiderIpAddresses() {

        spiderDetectorService.loadSpiderIpAddresses();
        return spiderDetectorService.getTable().toSet();
    }
    
    /**
     * Get all patterns included in the files under the specified spider pattern directory (i.e. "agents", "domain", etc.).
     * This path is relative to the ${dspace.dir}/config/spiders location.
     * @param spiderPatternDirectory is the directory where the spider patterns files will be loaded.
     * @return a list of patterns or an empty list if patterns cannot be loaded.
     */
    protected static Set<String> getSpiderFrom(String spiderPatternDirectory){
        Set<String> spiderPatternsList = new HashSet<String>();
        
        if(spiderPatternDirectory == null || spiderPatternDirectory.isEmpty()) {
            log.warn("The Spider directory \"{}\" passed as parameter is not valid!", spiderPatternDirectory);
        } else {
            try 
            {
                String filePath = ConfigurationManager.getProperty("dspace.dir");
                File spiderDir = new File(filePath, "config/spiders/" + spiderPatternDirectory);
                
                if (spiderDir.exists() && spiderDir.isDirectory()) {
                    for (File file : spiderDir.listFiles()) {
                        if (file.isFile())
                        {
                            for (String agent : readPatterns(file)) 
                            {
                                spiderPatternsList.add(agent);
                            }
                        }
                    }
                } else {
                    log.info("No spider file loaded from {} directory.", spiderDir.getPath());
                }
            }
            catch (IOException e)
            {
                log.error("Error Loading Spiders: " + e.getMessage(), e);
            }
        }
        return spiderPatternsList;
    }
    
    /**
     * Returns the patterns specified under the spiders directory by user agent.
     * 
     * @return a list of user agent patterns corresponding with spiders.
     */
    public static Set<String> getSpiderAgents() {
        return getSpiderFrom(SpiderDetector.SPIDER_AGENTS_DIRNAME);
    }
    
    /**
     * Returns the patterns specified under the spiders directory by domain (Reverse DNS Lookup).
     * 
     * @return a list of domains patterns corresponding with spiders.
     */
    public static Set<String> getSpiderDomains() {
        return getSpiderFrom(SpiderDetector.SPIDER_DOMAINS_DIRNAME);
    }

    /**
     * Utility method which reads lines from a file & returns them in a Set.
     *
     * @param patternFile the location of our spider file
     * @return a vector full of patterns
     * @throws IOException could not happen since we check the file be4 we use it
     */
    public static Set<String> readPatterns(File patternFile)
        throws IOException {
        return spiderDetectorService.readPatterns(patternFile);
    }

    /**
     * Static Service Method for testing spiders against existing spider files.
     * <p>
     * In future spiders HashSet may be optimized as byte offset array to
     * improve performance and memory footprint further.
     *
     * @param clientIP address of the client.
     * @param proxyIPs comma-list of X-Forwarded-For addresses, or null.
     * @param hostname domain name of host, or null.
     * @param agent    User-Agent header value, or null.
     * @return true if the client matches any spider characteristics list.
     */
    public static boolean isSpider(String clientIP, String proxyIPs,
                                   String hostname, String agent) {
        return spiderDetectorService.isSpider(clientIP, proxyIPs, hostname, agent);
    }

    /**
     * Static Service Method for testing spiders against existing spider files.
     *
     * @param request
     * @return true|false if the request was detected to be from a spider.
     */
    public static boolean isSpider(HttpServletRequest request) {
        return spiderDetectorService.isSpider(request);
    }

    /**
     * Check individual IP is a spider.
     *
     * @param ip
     * @return if is spider IP
     */
    public static boolean isSpider(String ip) {
        return spiderDetectorService.isSpider(ip);
    }

}
