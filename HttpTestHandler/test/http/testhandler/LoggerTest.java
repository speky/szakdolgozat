package http.testhandler;

import static org.junit.Assert.*;

import http.testhandler.Logger;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class LoggerTest {
    @Before
    public void setUp() {
    	File file = new File("log.txt");
    	file.delete();
    	file = new File("log.txt");
    	file.delete();     
    }
    
    @AfterClass
    public static void tearDown() {
    	File file = new File("log.txt");
    	file.delete();
    	file = new File("log.txt");
    	file.delete();
    	System.out.println("Log files deleted");
    }

	@Test
	public void testMakeFileInstanceWithNullString() {
		Logger logger = new Logger("");
		File file = new File("log.txt");
		assertTrue(!file.exists());
		logger.closeFile();
	}
	
	@Test
	public void testMakeFileInstanceWithDefaultName() {
		Logger logger = new Logger(Logger.LOG_FILE_NAME);
		File file = new File("log.txt");
		assertTrue(file.exists());
		logger.closeFile();
	}
	@Test
	public void testMakeFileInstanceSpecifiedPath() {
		Logger logger = new Logger("log.txt");
		File file = new File("log.txt");
		assertTrue(file.exists());
		logger.closeFile();
	}
	
	@Test
	public void testCreateDouble() {
		Logger logger = new Logger(Logger.LOG_FILE_NAME);		
		Logger logger2 = new Logger("log1.txt");
		File file = new File("log.txt");
		assertTrue(file.exists());
		file = new File("log1.txt");
		assertTrue(file.exists());
		logger.closeFile();
		logger2.closeFile();		
	}

	@Test
	public void testAddLine() {
		Logger logger = new Logger("");		
		assertTrue(logger.addLine("logText"));
		logger.closeFile();
	}

	@Test
	public void testDeleteLogFile() {
		Logger logger = new Logger("test1.txt");		
		assertTrue(logger.addLine("logText"));
		assertTrue(logger.deleteLogFile());
	}
	
}
