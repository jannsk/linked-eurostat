package com.ontologycentral.extatwrap.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.ontologycentral.estatwrap.convert.DictionaryPage;

public class DictionaryHandler {

	public void perform(String id, PrintStream out) throws IOException, XMLStreamException {
		URL url = new URL("http://epp.eurostat.ec.europa.eu/NavTree_prod/everybody/BulkDownloadListing?file=dic/en/" + id + ".dic");
		
		System.out.println(url);

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		
		InputStream is = null;
				
		if (url.toString().endsWith("gz")) {	
			is = new GZIPInputStream(conn.getInputStream());
		} else {
			is = conn.getInputStream();			
		}
		if (conn.getResponseCode() != 200) {
			throw new IOException("Response code: " + conn.getResponseCode());
		}

		String encoding = conn.getContentEncoding();
		if (encoding == null) {
			encoding = "ISO-8859-1";
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

	    XMLOutputFactory factory = XMLOutputFactory.newInstance();
		
		XMLStreamWriter ch = factory.createXMLStreamWriter(out, "utf-8");

//		DictionaryPage.convert(ch, id, in);

		ch.close();
	}
	
}
