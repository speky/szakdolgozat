package http.filehandler;

public interface ReceiverReportI {
	public int receivedBytes = 0;
		
	public void setReceivedBytes(final int id, final int interval, final int bytes);
	public void setReceivedBytes(final int id, final int interval, final int bytes, final double jitter,
						final int lost, final int outOfOrdered, final int sum );
	
}
