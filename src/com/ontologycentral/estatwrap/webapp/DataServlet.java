package com.ontologycentral.estatwrap.webapp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import com.ontologycentral.extatwrap.handler.DataHandler;

@SuppressWarnings("serial")
public class DataServlet extends HttpServlet {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		OutputStream os = resp.getOutputStream();
		
		String id = req.getRequestURI();
		id = id.substring("/data/".length());
		
		try {
			resp.setContentType("application/rdf+xml");
			resp.setHeader("Content-Disposition", "attachment; filename='" + id + "_data.rdf'");

			// 1 day
			resp.setHeader("Cache-Control", "public,max-age=86400");
			new DataHandler().perform(id, "./WEB-INF", os);
		} catch (TransformerException e) {
			e.printStackTrace(); 
			resp.sendError(500, e.getMessage());
			return;
		} catch (IOException e) {
			resp.sendError(500, id + ": " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (RuntimeException e) {
			resp.sendError(500, id + ": " + e.getMessage());
			e.printStackTrace();
			return;			
		} 
		
		os.close();
	}
	
}
