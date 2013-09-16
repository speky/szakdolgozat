package http.filehandler;

public class TCPReport {
	protected int reporterId = 0;
	protected String interval = "-";
	protected double transferedData = 0.0;
	protected double dlSpeed = 0.0;
	protected double ulSpeed = 0.0;
	protected String data = "KB";
	protected String rate = "Kbits/sec";
	
	public TCPReport(final String interval, final double transferedData, final double dlSpeed, final double ulSpeed) {
		this.interval = interval;
		this.transferedData = transferedData;
		this.dlSpeed = dlSpeed;
		this.ulSpeed = ulSpeed;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public void setRate(String rate) {
		this.rate = rate;
	}
	
	public String getHeader() {
		return "TCP\n[ID] Interval Transfer DL speed UL speed";
	}
	
	@Override
	public String toString() {		
		return "["+Integer.toString(reporterId)+"] "+interval +" "+Double.toString(transferedData) +" "+ data + 
				" " + Double.toString(dlSpeed) +" "+ rate+" " + Double.toString(ulSpeed) +" "+ rate;
	}
	
}
