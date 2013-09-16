package http.filehandler;

public interface ReceiverReportI {
	public int receivedBytes = 0;
	public int sentBytes = 0;
	
	public void setReceivedtBytes(final int bytes);	
	public void setSentBytes(final int bytes);
}
