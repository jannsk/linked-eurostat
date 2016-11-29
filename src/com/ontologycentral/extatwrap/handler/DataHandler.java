package com.ontologycentral.extatwrap.handler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.ontologycentral.estatwrap.webapp.Listener;

import loader.FileLoader;

public class DataHandler {
	Logger _log = Logger.getLogger(this.getClass().getName());
	
	public void perform(String id, String xslParentDirectory, OutputStream os) throws IOException, TransformerException {
		javax.xml.transform.TransformerFactory tf =
			      javax.xml.transform.TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",
			    		  Thread.currentThread().getContextClassLoader()); 
		Transformer t = tf.newTransformer(new StreamSource(xslParentDirectory + "/data2rdf.xsl"));
		
		// TableOfContentsLoader toc = new TableOfContentsLoader();
		// or use toc.getSMDXDownloadLink(id)
		URL u = new URL(Listener.URI_PREFIX + "?file=data/" + id + ".sdmx.zip");
		
		_log.info("retrieving " + u);
		
		FileLoader loader = new FileLoader(u);
		InputStream is = loader.get();
	
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
	
		ZipEntry ze;
		while((ze = zis.getNextEntry()) != null) {
			if (ze.getName().contains("sdmx.xml") ) {
				break;
			}
		}
	
		StreamSource ssource = new StreamSource(zis);
		StreamResult sresult = new StreamResult(os);
	
		_log.info("lapplying xslt");
	
		t.transform(ssource, sresult);
		
		_log.info("lapplying xslt done");
	
		zis.close();
	}
	
}
