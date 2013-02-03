package httpserver;

import static org.junit.Assert.assertTrue;
import http.filehandler.Logger;

import java.util.Date;

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
		String date ="Date: "  + response.gmtFormat.format(new Date()) + "\r\n";
		String out = response.setResponseText(HttpResponse.HTTP_BADREQUEST, null, null);		
		assertTrue(out.equals("HTTP*/1.0 "+ HttpResponse.HTTP_BADREQUEST+" \r\n"+date+"END\n"));
	}
	
	@Test
	public void setTextWithMimeParameter() {
		HttpResponse response = new HttpResponse(logger);
		String date ="Date: "  + response.gmtFormat.format(new Date()) + "\r\n";
		String out = response.setResponseText(HttpResponse.HTTP_BADREQUEST, HttpResponse.MIME_PLAINTEXT, null);		
		assertTrue(out.equals("HTTP*/1.0 "+ HttpResponse.HTTP_BADREQUEST+" \r\n"+"Content-Type: " + HttpResponse.MIME_PLAINTEXT + "\r\n"+date+"END\n"));
	}

}
