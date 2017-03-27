package com.ontologycentral.estatwrap.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import loader.TableOfContentsLoader;

@SuppressWarnings("serial")
public class TableOfContentsServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		
		try {
			resp.setContentType("application/rdf+xml");
			resp.setHeader("Content-Disposition", "attachment; filename='table_of_contents.rdf'");
			resp.setHeader("Cache-Control", "public,max-age=86400");

			TableOfContentsLoader toc = new TableOfContentsLoader();
			PrintWriter pw = resp.getWriter();
			XMLStreamWriter ch;

			ch = XMLOutputFactory.newInstance().createXMLStreamWriter(pw);

			ch.writeStartDocument("utf-8", "1.0");
			
			ch.writeStartElement("rdf:RDF");
			ch.writeDefaultNamespace("http://www.w3.org/1999/xhtml");
			ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			ch.writeNamespace("dc", "http://purl.org/dc/elements/1.1/");
			
			for (HashMap<String, String> item : toc.getAll()) {
				ch.writeStartElement("rdf:Description");
				ch.writeAttribute("rdf:about", "./id/"+item.get("code"));
				
				ch.writeStartElement("dc:title");
				ch.writeCharacters(item.get("title"));
				ch.writeEndElement(); // dc:title
				
				ch.writeStartElement("dc:date");
				ch.writeCharacters(item.get("lastUpdate"));
				ch.writeEndElement(); // dc:date
				
				ch.writeEndElement(); // rdf:Description
			}
			
			ch.writeEndElement(); // rdf:RDF
			ch.writeEndDocument();
			ch.close();
		} catch (RuntimeException e) {
			resp.sendError(500, e.getMessage());
			e.printStackTrace();
			return;			
		} catch (XMLStreamException e) {
			resp.sendError(500, e.getMessage());
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			resp.sendError(500, e.getMessage());
			e.printStackTrace();
		} 	
	}
	

	
}