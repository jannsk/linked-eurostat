package com.ontologycentral.extatwrap.handler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.ontologycentral.estatwrap.webapp.Listener;

import net.sf.saxon.TransformerFactoryImpl;

public class DsdHandler {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void perform(String id, String xslParentDirectory, OutputStream os) throws IOException, TransformerException {
		TransformerFactory tf = new TransformerFactoryImpl();
		
		Transformer t = tf.newTransformer(new StreamSource(xslParentDirectory + "/dsd2rdf.xsl"));
		
		URL u = new URL(Listener.URI_PREFIX + "?file=data/" + id + ".sdmx.zip");

		_log.info("retrieving " + u);
		
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();

		// Bugfix since user agent java is blocked by Eurostat.
		conn.setRequestProperty("User-agent", "notjava");
		conn.setConnectTimeout(120*1000);
		conn.setReadTimeout(300*1000);

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("lookup on " + u + " resulted HTTP in status code " + conn.getResponseCode());
		}

		InputStream is = conn.getInputStream();

		String encoding = conn.getContentEncoding();
		if (encoding == null) {
			encoding = "ISO-8859-1";
		}

		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

		ZipEntry ze;
		while((ze = zis.getNextEntry()) != null) {
			if (ze.getName().contains("dsd.xml") ) {
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
