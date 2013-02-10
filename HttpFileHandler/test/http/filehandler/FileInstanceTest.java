package http.filehandler;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileInstanceTest {	
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
	public void testFileInstanceCreation() {
		FileInstance fileInstance = new FileInstance(logger, "test.txt");		
		assertTrue(fileInstance.getName().equals("test.txt"));
	}
}
