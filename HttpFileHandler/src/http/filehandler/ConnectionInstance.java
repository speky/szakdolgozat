package http.filehandler;

import java.util.concurrent.Callable;

public class ConnectionInstance implements Callable<Integer> {
	public static final int TCP = 0;
	public static final int UDP = 1;
	
	protected int id = -1;
	protected int type = -1;
	protected Logger logger = null;
	protected String errorMessage = null;	
	
	public ConnectionInstance(final int type, final int id, Logger logger) {
		this.type = type;
		this.id = id;
		this.logger = logger;
	}
	
	public int getId() {
		return id;
	}
	
	public int getType() {
		return type;
	}
		
	public String getErrorMessage() { 
		return errorMessage;
	}	
	
	public void stop() {}
	
	@Override
	public Integer call() throws Exception {
		return 0;
	}

}
