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
	public void testFileInstanceInvalidFileName() {
		FileInstance fileInstance = new FileInstance(logger, "test1.txt");		
		assertTrue(fileInstance.getName().equals("test1.txt"));
		fileInstance.splitFileToPockets(FileInstance.DEFAULT_SIZE);
		assertTrue(fileInstance.getPocketSize() == 0);
	}

	@Test
	public void testFileInstanceCreation() {
		FileInstance fileInstance = new FileInstance(logger, "test.txt");		
		assertTrue(fileInstance.getName().equals("test.txt"));
	}
	
	@Test
	public void testFileInstanceSplitting() {
		FileInstance fileInstance = new FileInstance(logger, "test.txt");		
		assertTrue(fileInstance.getName().equals("test.txt"));
		fileInstance.splitFileToPockets(FileInstance.DEFAULT_SIZE);
		assertTrue(fileInstance.getPocketSize() == 3);
	}

	@Test
	public void testFileInstanceSplitting5MBfile() {
		FileInstance fileInstance = new FileInstance(logger, "5MB.bin");		
		assertTrue(fileInstance.getName().equals("5MB.bin"));
		fileInstance.splitFileToPockets(1024);
		assertTrue(fileInstance.getPocketSize() == 5120);
		assertTrue(fileInstance.getCheckSum().equals("c0de104c1e68625629646025d15a6129a2b4b6496cd9ceacd7f7b5078e1849ba"));
	}
	
	@Test
	public void testAddOnePacket() {
		FileInstance fileInstance = new FileInstance(logger, "test");		
		assertTrue(fileInstance.getName().equals("test"));
		fileInstance.addPacket(0, "text");
		assertTrue(fileInstance.getPocketSize() == 1);
	}
	
	@Test
	public void testAddSamePacketTwice() {
		FileInstance fileInstance = new FileInstance(logger, "test");		
		assertTrue(fileInstance.getName().equals("test"));
		fileInstance.addPacket(0, "text");
		fileInstance.addPacket(0, "text");
		assertTrue(fileInstance.getPocketSize() == 1);
	}
	
	@Test
	public void testAddTwoPacket() {
		FileInstance fileInstance = new FileInstance(logger, "test");		
		assertTrue(fileInstance.getName().equals("test"));
		fileInstance.addPacket(0, "text");
		fileInstance.addPacket(1, "text");
		assertTrue(fileInstance.getPocketSize() == 2);
	}

}
