package http.filehandler;

import java.util.StringTokenizer;

public class TCPReport {
	public  int reporterId = 0;
	public int  interval = 0;
	public  double transferedData = 0.0;
	public  double dlSpeed = 0.0;
	public  double ulSpeed = 0.0;
	protected String data = "KB";
	protected String rate = "Kbits/sec";
	protected StringTokenizer tokens = null;
	
	public TCPReport() {
		reporterId = 0;
		this.interval = 0;
		this.transferedData = 0.0;
		this.dlSpeed = 0.0;
		this.ulSpeed = 0.0;
	}
	
	public TCPReport(final int id, final int interval, final double transferedData, final double dlSpeed, final double ulSpeed) {
		reporterId = id;
		this.interval = interval;
		this.transferedData = transferedData;
		this.dlSpeed = dlSpeed;
		this.ulSpeed = ulSpeed;
	}

	public boolean parseReport(final String report) {
		if (report == null) {
			return false;
		}
		tokens = new StringTokenizer(report);
		try {			
			reporterId = Integer.parseInt((String)tokens.nextElement());
			interval = Integer.parseInt((String)tokens.nextElement());			
			transferedData = Double.parseDouble((String)tokens.nextElement());			
		} catch (Exception ex) {
			ex.getMessage();
			return false;
		}
		return true;
	}

	public void setDLSpeed(Double speed) {
		this.dlSpeed = speed;
	}
	
	public void setULSpeed(Double speed) {
		this.ulSpeed = speed;
	}
	
	public void setData(String data) {
		this.data = data;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getHeader() {
		return "TCP\nId Interval Transfer DL speed UL speed";
	}

	@Override
	public String toString() {		
		return Integer.toString(reporterId)+" " +Integer.toString(interval)+" sec" +" "+Double.toString(transferedData) +" "+ data + 
				" " + Double.toString(dlSpeed) +" "+ rate+" " + Double.toString(ulSpeed) +" "+ rate;
	}

}
