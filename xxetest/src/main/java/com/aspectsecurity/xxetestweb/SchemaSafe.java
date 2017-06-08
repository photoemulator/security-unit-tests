package com.aspectsecurity.xxetestweb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.owasp.encoder.Encode;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Servlet implementation class XXETesting
 */
@WebServlet("/schemasafe")
public class SchemaSafe extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final boolean expectedSafe = true;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SchemaSafe() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setHeader("X-Frame-Options", "DENY");
		
		SchemaFactory factory = 
        		SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		} catch (SAXNotRecognizedException | SAXNotSupportedException e1) {
			e1.printStackTrace();
		}
        try {
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		} catch (SAXNotRecognizedException | SAXNotSupportedException e1) {
			e1.printStackTrace();
		}
        
        Schema schema = null;
		try {
			schema = factory.newSchema(getClass().getResource("/test.xsd"));
		} catch (SAXException e) {
			e.printStackTrace();
		}
        Validator validator = schema.newValidator();
        
        StreamSource input = new StreamSource(new ByteArrayInputStream(request.getParameter("payload").getBytes()));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult output = new StreamResult(baos);
        
        response.getWriter().write("<html><head><title>Results</title></head><body><span style=\"white-space: pre\">");
        response.getWriter().write("Expected result: " + (expectedSafe ? "Safe\n" : "Unsafe\n") + "Actual Result: ");
        try {
        	
	        validator.validate(input, output);
	        String result = new String(baos.toByteArray());
	
	        if (result.contains("SUCCESSFUL"))
	        	response.getWriter().write("XXE was injected! :(\n\nXML Content (Should contain \"SUCCESSFUL\"):\n" + Encode.forHtmlContent(result));
	        	
        } catch (Exception ex) {
			response.getWriter().write("XML Parser is safe! :)\n\nStack Trace:\n");
			ex.printStackTrace(response.getWriter());
        }
        finally {
        	response.getWriter().write("</span></body></html>");
        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
