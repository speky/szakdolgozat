package httpserver;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpParserTest {
private static Logger logger;

	@BeforeClass
	public static void prepare() {
		logger = new Logger();
		logger.Init("");
	}
	
	@AfterClass
	public static void tearDown() {		
		logger.deleteLogFile();
	}
	
	@Test
	public void testGetErrorText() {		 
		HttpParser parser = new HttpParser(logger);		
		assertTrue(parser.getErrorText().equals(""));
	}

	@Test
	public void testParseHttp() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getErrorText().equals(""));
		
		str = "HEAD /index.html HTTP*/1.0 \r\n";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getErrorText().equals(""));
		
		str = "HEAD 	/index.html 		HTTP*/1.0 \r\n";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getErrorText().equals(""));
				
		str = "HEAD /index.html HTTP*/1.0";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getErrorText().equals(""));
		
		str = "head  /index.html HTTP*/1.0";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getErrorText().equals(""));
	}

	@Test
	public void testParseHttpMethod() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getMethod().equals("HEAD"));
		
		str = "head  /index.html HTTP*/1.0";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getMethod().equals("HEAD"));
	}
	
	@Test
	public void testParseHttpMethodVersion() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getVersion().equals("HTTP*/1.0"));
				
		str = "head  /index.html HTTP/1.0";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethodSize() == 3);
		assertTrue(parser.getVersion().equals("HTTP/1.0"));
	}
	
	@Test
	public void testParseHttpMethodWrongProperty() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD HTTP*/1.0 \r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethodSize() == 2);
		assertTrue(parser.getErrorText().equals(HttpResponse.HTTP_BADREQUEST));
				
		str = "HEAD /index.html \r\n";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethodSize() == 2);
		assertTrue(parser.getErrorText().equals(HttpResponse.HTTP_BADREQUEST));
	}

	@Test
	public void testParseHttpHeadProperty() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n from : zsolt@gmail.com\r\n\r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		assertTrue(parser.parseHttpHead(input));
		System.out.println(parser.getErrorText());
		assertTrue(parser.getErrorText().equals(""));
		assertTrue(parser.getHeadSize() == 1);
		assertTrue(parser.getHeadTag("FROM").equals("zsolt@gmail.com"));
		assertTrue(parser.getHeadTag("from") == null);
				
		str = "HEAD /index.html HTTP*/1.0\r\n";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getHeadSize() == 0);
		assertTrue(parser.getHeadTag("from") == null);
		assertTrue(parser.getHeadTag("FROM") == null);
	}
	
	@Test
	public void testParseHttpHeadPropertyWithFileTag() {
		HttpParser parser = new HttpParser(logger);	
		String str = "GET / HTTP*/1.0\r\n file1 : UDP\r\n length	:1\r\n\r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethod().equals("GET") );
		assertTrue(parser.getHeadSize() == 2);
		String file = parser.getHeadTag("FILE1");
		assertTrue(file.equals("UDP"));
		assertTrue(parser.getHeadTag("LENGTH").equals("1"));
	}
}
