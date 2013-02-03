package httpserver;

import static org.junit.Assert.assertTrue;
import http.filehandler.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpParserTest {
private static Logger logger;

	@BeforeClass
	public static void prepare() {
		logger = new Logger("");		
	}
	
	@AfterClass
	public static void tearDown() {		
		logger.deleteLogFile();
	}
	
	@Test
	public void testGetErrorText() {		 
		HttpParser parser = new HttpParser(logger);		
		assertTrue(parser.getErrorText() == null);
	}

	@Test
	public void testParseHttp() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";		
		parser.parseHttpHead(str);
		assertTrue(parser.getErrorText() == null);
		
		str = "HEAD /index.html HTTP*/1.0 \r\n";
		parser.parseHttpHead(str);
		assertTrue(parser.getErrorText() == null);
		
		str = "HEAD 	/index.html 		HTTP*/1.0 ";
		parser.parseHttpHead(str);
		assertTrue(parser.getErrorText() == null);
				
		str = "HEAD /index.html HTTP*/1.0";
		parser.parseHttpHead(str);
		assertTrue(parser.getErrorText() == null);
		
		str = "head  /index.html HTTP*/1.0";
		parser.parseHttpHead(str);
		assertTrue(parser.getErrorText() == null);
	}

	@Test
	public void testParseHttpMethod() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";
		parser.parseHttpHead(str);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getMethod().equals("HEAD"));
		
		str = "head  /index.html HTTP*/1.0";
		
		parser.parseHttpHead(str);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getMethod().equals("HEAD"));
	}
	
	@Test
	public void testParseHttpMethodVersion() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";
		parser.parseHttpHead(str);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getVersion().equals("HTTP*/1.0"));
				
		str = "head  /index.html HTTP/1.0";
		
		parser.parseHttpHead(str);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getVersion().equals("HTTP/1.0"));
	}
	
	@Test
	public void testParseHttpMethodWrongProperty() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD HTTP*/1.0 \r\n";
		parser.parseHttpHead(str);
		assertTrue(parser.getMethodSize() == 2);
		assertTrue(parser.getErrorText().equals(HttpResponse.HTTP_BADREQUEST));
				
		str = "HEAD /index.html \r\n";
		parser.parseHttpHead(str);
		assertTrue(parser.getMethodSize() == 2);
		assertTrue(parser.getErrorText().equals(HttpResponse.HTTP_BADREQUEST));
	}

	@Test
	public void testParseHttpHeadProperty() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 + from : zsolt@gmail.com";
		assertTrue(parser.parseHttpHead(str));
		assertTrue(parser.getErrorText()==null);
		assertTrue(parser.getHeadSize() == 1);
		assertTrue(parser.getHeadProperty("FROM").equals("zsolt@gmail.com"));
		assertTrue(parser.getHeadProperty("from") == null);
				
		str = "HEAD /index.html HTTP*/1.0\r\n";
		parser.parseHttpHead(str);
		assertTrue(parser.getHeadSize() == 0);
		assertTrue(parser.getHeadProperty("from") == null);
		assertTrue(parser.getHeadProperty("FROM") == null);
	}
	
	@Test
	public void testParseHttpHeadPropertyWithFileTag() {
		HttpParser parser = new HttpParser(logger);	
		String str = "GET / HTTP*/1.0 + file1 : UDP+ length	:1";
		parser.parseHttpHead(str);
		assertTrue(parser.getMethod().equals("GET") );
		System.out.println(parser.getHeadSize()); 
		assertTrue(parser.getHeadSize() == 2);
		String file = parser.getHeadProperty("FILE1");
		assertTrue(file.equals("UDP"));
		assertTrue(parser.getHeadProperty("LENGTH").equals("1"));
	}
}
