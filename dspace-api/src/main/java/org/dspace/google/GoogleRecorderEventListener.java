/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.google;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.dspace.core.Constants;
import org.dspace.services.model.Event;
import org.dspace.statistics.util.IPTable;
import org.dspace.usage.AbstractUsageEventListener;
import org.dspace.usage.UsageEvent;
import org.dspace.utils.DSpace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * User: Robin Taylor
 * Date: 14/08/2014
 * Time: 10:05
 *
 * Notify Google Analytics of... well anything we want really.
 *
 */
public class GoogleRecorderEventListener extends AbstractUsageEventListener {

    private String analyticsKey;
    private CloseableHttpClient httpclient;
    private String GoogleURL = "https://www.google-analytics.com/collect";
    private static Logger log = Logger.getLogger(GoogleRecorderEventListener.class);

    private Boolean useProxiesEnabled;
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private IPTable trustedProxies;

    public GoogleRecorderEventListener() {
        // httpclient is threadsafe so we only need one.
        httpclient = HttpClients.createDefault();
        trustedProxies = parseTrustedProxyRanges(StringUtils.stripAll(new DSpace().getConfigurationService().getPropertyAsType("proxies.trusted.ipranges", String[].class)));
    }

    public void receiveEvent(Event event) {
        if((event instanceof UsageEvent))
        {
            log.debug("Usage event received " + event.getName());

            // This is a wee bit messy but these keys should be combined in future.
            analyticsKey = new DSpace().getConfigurationService().getProperty("jspui.google.analytics.key");
            if (analyticsKey == null ) {
                analyticsKey = new DSpace().getConfigurationService().getProperty("xmlui.google.analytics.key");
            }

            if (analyticsKey != null ) {
                try {
                    UsageEvent ue = (UsageEvent)event;
                    if(UsageEvent.Action.VIEW == ue.getAction() && (Constants.BITSTREAM == ue.getObject().getType())) {
                        bitstreamDownload(ue);
                    }
                }
                catch(Exception e)
                {
                    log.error(e.getMessage());
                }
            }
        }
    }
    private void bitstreamDownload(UsageEvent ue) throws IOException {
        HttpPost httpPost = new HttpPost(GoogleURL);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("v", "1"));
        nvps.add(new BasicNameValuePair("uip", getClientIp(ue.getRequest().getRemoteAddr(), ue.getRequest().getHeader(X_FORWARDED_FOR_HEADER))));
        nvps.add(new BasicNameValuePair("tid", analyticsKey));
        nvps.add(new BasicNameValuePair("cid", "999"));
        nvps.add(new BasicNameValuePair("t", "event"));
        nvps.add(new BasicNameValuePair("dp", ue.getRequest().getRequestURI()));
        nvps.add(new BasicNameValuePair("ec", "bitstream"));
        nvps.add(new BasicNameValuePair("ea", "download"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response2 = httpclient.execute(httpPost)) {
            // I can't find a list of what are acceptable responses, so I log the response but take no action.
            log.debug("Google Analytics response is " + response2.getStatusLine());
        }

        log.debug("Posted to Google Analytics - " + ue.getRequest().getRequestURI());
    }

//  Por lo comentado en http://trac.prebi.unlp.edu.ar/issues/7002
//  es necesario chequear la IP del cliente y ver si en el "X-Forwarder-For" hay algún proxy confiable
//  Este código esta copiado de los servicios implementados para D6 y D7 https://github.com/DSpace/DSpace/blob/2d5eafb384abb94058ce080c86250fb09f029cca/dspace-api/src/main/java/org/dspace/service/impl/ClientInfoServiceImpl.java#L53
//  Descartar este código al migrar a D7 y llamar al servicio correspondiente
    public String getClientIp(String remoteIp, String xForwardedForHeaderValue) {
        String ip = remoteIp;

        if (isUseProxiesEnabled()) {
            String xForwardedForIp = getXForwardedForIpValue(remoteIp, xForwardedForHeaderValue);

            if (StringUtils.isNotBlank(xForwardedForIp) && isRequestFromTrustedProxy(ip)) {
                ip = xForwardedForIp;
            }

        } else if (StringUtils.isNotBlank(xForwardedForHeaderValue)) {
            log.warn(
                    "X-Forwarded-For header detected but useProxiesEnabled is not enabled. " +
                            "If your dspace is behind a proxy set it to true");
        }

        return ip;

    }

    private String getXForwardedForIpValue(String remoteIp, String xForwardedForValue) {
        String ip = null;

        /* This header is a comma delimited list */
        String headerValue = StringUtils.trimToEmpty(xForwardedForValue);
        for (String xfip : headerValue.split(",")) {
            /* proxy itself will sometime populate this header with the same value in
               remote address. ordering in spec is vague, we'll just take the last
               not equal to the proxy
            */
            xfip = xfip.trim();
            if (!StringUtils.equals(remoteIp, xfip) && StringUtils.isNotBlank(xfip)
                    //if we have trusted proxies, we'll assume that they are not the client IP
                    && (trustedProxies == null || !isRequestFromTrustedProxy(xfip))) {

                ip = xfip;
            }
        }

        return ip;
    }

    private boolean isRequestFromTrustedProxy(String ipAddress) {
        try {
            return trustedProxies == null || trustedProxies.contains(ipAddress);
        } catch (IPTable.IPFormatException e) {
            log.error("Request contains invalid remote address", e);
            return false;
        }
    }

    private IPTable parseTrustedProxyRanges(String[] proxyProperty) {
        if (ArrayUtils.isEmpty(proxyProperty)) {
            return null;
        } else {
            //Load all supplied proxy IP ranges into the IP table
            IPTable ipTable = new IPTable();
            try {
                for (String proxyRange : proxyProperty) {
                    ipTable.add(proxyRange);
                }
            } catch (IPTable.IPFormatException e) {
                log.error("Property proxies.trusted.ipranges contains an invalid IP range", e);
                ipTable = null;
            }

            return ipTable;
        }
    }

    public boolean isUseProxiesEnabled() {
            if (useProxiesEnabled == null) {
                useProxiesEnabled = new DSpace().getConfigurationService().getPropertyAsType("useProxies", Boolean.class);
                log.info("useProxies=" + useProxiesEnabled);
            }

            return useProxiesEnabled;
        }


}
