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
	public void testParseHttpHead() {
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
	public void testParseHttpHeadMethod() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethod().equals("HEAD"));
		
		str = "head  /index.html HTTP*/1.0";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getMethod().equals("HEAD"));
	}
	
	@Test
	public void testParseHttpHeadVersion() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD / HTTP*/1.0 \r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getVersion().equals("HTTP*/1.0"));
		
		str = "head  /index.html HTTP/1.0";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getVersion().equals("HTTP/1.0"));
	}
	
	@Test
	public void testParseHttpHeadWrongRequest() {
		HttpParser parser = new HttpParser(logger);
		String str = "HEAD HTTP*/1.0 \r\n";
		InputStream input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getErrorText().equals(HttpParser.HTTP_BADREQUEST));
				
		str = "HEAD /index.html \r\n";
		input = new ByteArrayInputStream(str.getBytes());
		parser.parseHttpHead(input);
		assertTrue(parser.getErrorText().equals(HttpParser.HTTP_BADREQUEST));
	}

}
