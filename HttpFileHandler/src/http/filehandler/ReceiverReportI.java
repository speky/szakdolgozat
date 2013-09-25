package http.filehandler;

public interface ReceiverReportI {
	public int receivedBytes = 0;
		
	public void setReceivedBytes(final int interval, final int bytes);
	
}
