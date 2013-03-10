package httpserver;

import static org.junit.Assert.assertTrue;
import http.filehandler.HttpParser;
import http.filehandler.Logger;

import java.util.Date;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpResponseTest {
	private static Logger logger;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {				
			logger = new Logger("");			
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		logger.deleteLogFile();
	}

	@Test
	public void setTextWithBadParameter() {
		HttpResponse response = new HttpResponse(logger);
		assertTrue(response.setResponseText(null, null, null) == null);
	}
	
	@Test
	public void setTextWithErrorParameter() {		
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + HttpResponse.gmtFormat.format(new Date()) + "\r\n";
		String out = response.setResponseText(HttpParser.HTTP_BADREQUEST, null, null);		
		assertTrue(out.equals("HTTP*/1.0 "+ HttpParser.HTTP_BADREQUEST+" \r\n"+date+"END\n"));
	}
	
	@Test
	public void setTextWithMimeParameter() {
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + HttpResponse.gmtFormat.format(new Date()) + "\r\n";
		String out = response.setResponseText(HttpParser.HTTP_BADREQUEST, HttpResponse.MIME_PLAINTEXT, null);		
		assertTrue(out.equals("HTTP*/1.0 "+ HttpParser.HTTP_BADREQUEST+" \r\n"+"Content-Type: " + HttpResponse.MIME_PLAINTEXT + "\r\n"+date+"END\n"));
	}

	@Test
	public void setTextPong() {		
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + HttpResponse.gmtFormat.format(new Date()) + "\r\n";
		String out = response.setResponseText("PONG", null, null);		
		assertTrue(out.equals("HTTP*/1.0 PONG \r\n"+date+"END\n"));
	}
	
	@Test
	public void setResponseWithoutHeaderProperty() {		
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + HttpResponse.gmtFormat.format(new Date()) + "\r\n";
		String out = response.setResponseText(HttpParser.HTTP_OK, HttpResponse.MIME_PLAINTEXT, null);		
		assertTrue(out.equals("HTTP*/1.0 200 OK \r\nContent-Type: "+HttpResponse.MIME_PLAINTEXT+"\r\n"+date+"END\n"));
	}
	
	@Test
	public void setResponseWithPort() {		
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + HttpResponse.gmtFormat.format(new Date()) + "\r\n";
		Properties property = new Properties();		
		property.put("PORT", "1234");
		String out = response.setResponseText(HttpParser.HTTP_OK, HttpResponse.MIME_PLAINTEXT, property);		
		assertTrue(out.equals("HTTP*/1.0 200 OK \r\nContent-Type: "+HttpResponse.MIME_PLAINTEXT+"\r\n"+date+"PORT: 1234\r\n"+"END\n"));
	}
	
	@Test
	public void setResponseResetPort() {		
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + HttpResponse.gmtFormat.format(new Date()) + "\r\n";
		Properties property = new Properties();		
		property.put("PORT", "1234");
		property.put("PORT", "2345");
		String out = response.setResponseText(HttpParser.HTTP_OK, HttpResponse.MIME_PLAINTEXT, property);		
		assertTrue(out.equals("HTTP*/1.0 200 OK \r\nContent-Type: "+HttpResponse.MIME_PLAINTEXT+"\r\n"+date+"PORT: 2345\r\n"+"END\n"));
	}
	
	@Test
	public void setResponseMultipleProperty() {		
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + HttpResponse.gmtFormat.format(new Date()) + "\r\n";
		Properties property = new Properties();		
		property.put("PORT1", "1234");
		property.put("PORT3", "3456");
		property.put("PORT2", "2345");		
		String out = response.setResponseText(HttpParser.HTTP_OK, HttpResponse.MIME_PLAINTEXT, property);		
		assertTrue(out.equals("HTTP*/1.0 200 OK \r\nContent-Type: "+HttpResponse.MIME_PLAINTEXT+"\r\n"+date+"PORT1: 1234\r\nPORT3: 3456\r\nPORT2: 2345\r\n"+"END\n"));
	}
}
