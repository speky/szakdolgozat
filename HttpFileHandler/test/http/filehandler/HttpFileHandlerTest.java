package http.filehandler;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpFileHandlerTest {
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
	public void testHttpFileHandlerCreation() {
		HttpFileHandler fileHandler = new HttpFileHandler(logger);		
		assertTrue(fileHandler.getNumberOfFiles() == 0);
	}

	@Test
	public void testAddOneFile() {
		HttpFileHandler fileHandler = new HttpFileHandler(logger);
		fileHandler.AddFile("");
		assertTrue(fileHandler.getNumberOfFiles() == 0);
		fileHandler.AddFile("test.txt");
		assertTrue(fileHandler.getNumberOfFiles() == 1);		
	}
	
	@Test
	public void testAddOneFileTwice() {
		HttpFileHandler fileHandler = new HttpFileHandler(logger);		
		fileHandler.AddFile("test.txt");
		assertTrue(fileHandler.getNumberOfFiles() == 1);		
		fileHandler.AddFile("test.txt");
		assertTrue(fileHandler.getNumberOfFiles() == 1);
	}
	
	@Test
	public void testCheckFileExistance() {
		HttpFileHandler fileHandler = new HttpFileHandler(logger);		
		assertFalse(fileHandler.IsFileInSet("test.txt"));
		fileHandler.AddFile("test.txt");
		assertTrue(fileHandler.IsFileInSet("test.txt"));
		assertTrue(fileHandler.getNumberOfFiles() == 1);	
	}
	
	@Test
	public void testGetFileInstance() {
		HttpFileHandler fileHandler = new HttpFileHandler(logger);		
		assertFalse(fileHandler.IsFileInSet("test.txt"));
		assertNull(fileHandler.getFileInstance("test.txt"));
		fileHandler.AddFile("test.txt");
		assertTrue(fileHandler.IsFileInSet("test.txt"));
		FileInstance fi = fileHandler.getFileInstance("test.txt");
		assertNotNull(fi);
		fi.SplitFileToPockets(FileInstance.DEFAULT_SIZE);
		assertTrue(fi.getPocketSize() == 3);
		fi.SplitFileToPockets(100);
		assertTrue(fi.getPocketSize() == 1);
		assertTrue(fileHandler.getNumberOfFiles() == 1);	
	}
	
	
}
