package httpserver;

import static org.junit.Assert.*;
import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class LoggerTest {
    @Before
    public void setUp() {
    	File file = new File("trackerLog.txt");
    	file.delete();
    	file = new File("log.txt");
    	file.delete();     
    }
    
    @AfterClass
    public static void tearDown() {
    	File file = new File("trackerLog.txt");
    	file.delete();
    	file = new File("log.txt");
    	file.delete();
    	System.out.println("Log files deleted");
    }

	@Test
	public void testMakeFileInstance() {
		Logger logger = new Logger();
		logger.makeFileInstance("");
		File file = new File("trackerLog.txt");
		assertTrue(file.exists());		
	}
	
	@Test
	public void testMakeFileInstanceSpecifiedPath() {
		Logger logger = new Logger();
		logger.makeFileInstance("log.txt");
		File file = new File("log.txt");
		assertTrue(file.exists());		
	}

	@Test
	public void testMakeFileWriter() {
		Logger logger = new Logger();		
		assertFalse(logger.makeFileWriter());
		logger.makeFileInstance("");
		assertTrue(logger.makeFileWriter());
		logger.closeFile();
	}

	@Test
	public void testInit() {
		Logger logger = new Logger();
		assertTrue(logger.Init(""));
		Logger logger2 = new Logger();
		assertTrue(logger2.Init("log.txt"));
		logger.closeFile();
		logger2.closeFile();		
	}

	@Test
	public void testAddLine() {
		Logger logger = new Logger();	
		assertFalse(logger.addLine("logText"));
		logger.Init("");
		assertTrue(logger.addLine("logText"));
		logger.closeFile();
	}

	@Test
	public void testDeleteLogFile() {
		Logger logger = new Logger();	
		assertFalse(logger.deleteLogFile());
		logger.Init("test.txt");
		assertTrue(logger.addLine("logText"));
		assertTrue(logger.deleteLogFile());
	}
	
}
