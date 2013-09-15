package http.filehandler;

public class TCPReport {
	protected int reporterId = 0;
	protected String interval = "-";
	protected double transferedData = 0.0;
	protected double bandwidth = 0.0;
	protected String data = "KB";
	protected String rate = "Kbits/sec";
	
	public TCPReport(final String interval, final double transferedData, final double bandwidth) {
		this.interval = interval;
		this.transferedData = transferedData;
		this.bandwidth = bandwidth;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public void setRate(String rate) {
		this.rate = rate;
	}
	
	public String getHeader() {
		return "[ID] Interval Transfer Bandwidth";
	}
	
	@Override
	public String toString() {		
		return "["+Integer.toString(reporterId)+"] "+interval +" "+Double.toString(transferedData) +" "+ data + 
				" " + Double.toString(bandwith) +" "+ rate;
	}
	
}
