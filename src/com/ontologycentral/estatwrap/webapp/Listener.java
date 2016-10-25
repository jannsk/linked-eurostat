package com.ontologycentral.estatwrap.webapp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import com.ontologycentral.estatwrap.convert.ToC;

public class Listener implements ServletContextListener {
	Logger _log = Logger.getLogger(this.getClass().getName());
	
	public static String URI_PREFIX = "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/BulkDownloadListing";

	public static SimpleDateFormat RFC822 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
	public static SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	public static String FACTORY = "f";
	public static String TOC = "t";
	public static String CACHE = "c";
	
	public static String SDMX_T = "sdmx";
	public static String SDMX_D = "data";

	public static String NUTS = "nuts";	

	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();

	    XMLOutputFactory factory = XMLOutputFactory.newInstance();

	    ctx.setAttribute(FACTORY, factory);
	    
        Cache cache = null;

        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
    		ctx.setAttribute(CACHE, cache);
        } catch (CacheException e) {
        	e.printStackTrace();
        }
        
        //cache.clear();

		javax.xml.transform.TransformerFactory tf =
		      javax.xml.transform.TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",
		    		  Thread.currentThread().getContextClassLoader()); 

//		javax.xml.transform.TransformerFactory tf =
//			javax.xml.transform.TransformerFactory.newInstance(); //"org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader() ); 
		
		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/dsd2rdf.xsl")));
			ctx.setAttribute(SDMX_T, t);
		} catch (TransformerConfigurationException e) {
			_log.severe(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/data2rdf.xsl")));
			ctx.setAttribute(SDMX_D, t);
		} catch (TransformerConfigurationException e) {
			_log.severe(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}

        
	    Map<String, String> map = null;
	    
//	    if (cache.containsKey(TOC)) {
//	    	map = (Map<String, String>)cache.get(TOC);
//	    }

	    if (map == null) {
	    	try {
	    		//URL u = new URL("http://epp.eurostat.ec.europa.eu/NavTree_prod/everybody/BulkDownloadListing?sort=1&file=table_of_contents_en.txt");
	    		URL u = new URL(URI_PREFIX + "?file=table_of_contents_en.txt");
	    		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
	    		InputStream is = conn.getInputStream();

	    		ToC toc = new ToC(is, null);		
	    		map = toc.convert();
//	    		cache.put(TOC, map);
	    	} catch (MalformedURLException e) {
				_log.severe(e.getMessage());
	    		e.printStackTrace();
	    		map = new HashMap<String, String>();
	    	} catch (IOException e) {
				_log.severe(e.getMessage());
	    		e.printStackTrace();
	    		map = new HashMap<String, String>();
	    	} catch (XMLStreamException e) {
				_log.severe(e.getMessage());
	    		e.printStackTrace();
	    		map = new HashMap<String, String>();
	    	}
	    }
	    //map = new HashMap<String, String>();
	    ctx.setAttribute(TOC, map);
	    
	    InputStream in = null;
	    BufferedReader br = null;
	    
		try {
			Set<String> nuts = new HashSet<String>();
			in = new FileInputStream(ctx.getRealPath("/WEB-INF/nuts.txt"));
			br = new BufferedReader(new InputStreamReader(in));

			String line = br.readLine();
			while ((line = br.readLine()) != null)   {
				String nutscode = line.trim();
				
				if (!nutscode.isEmpty()) {
					nuts.add(nutscode);
				}
			}
			ctx.setAttribute(NUTS, nuts);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				br.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		// TODO Auto-generated method stub		
	}
}
