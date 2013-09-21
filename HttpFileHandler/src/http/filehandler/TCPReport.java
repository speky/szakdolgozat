package http.filehandler;

import java.util.StringTokenizer;

public class TCPReport {
	protected int reporterId = 0;
	protected String interval = "-";
	protected double transferedData = 0.0;
	protected double dlSpeed = 0.0;
	protected double ulSpeed = 0.0;
	protected String data = "KB";
	protected String rate = "Kbits/sec";
	protected StringTokenizer tokens = null;
	
	public TCPReport() {
		reporterId = 0;
		this.interval = "-";
		this.transferedData = 0.0;
		this.dlSpeed = 0.0;
		this.ulSpeed = 0.0;
	}
	
	public TCPReport(final int id, final String interval, final double transferedData, final double dlSpeed, final double ulSpeed) {
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
			interval = (String)tokens.nextElement();
			transferedData = Double.parseDouble((String)tokens.nextElement());
			//skip data type
			tokens.nextElement();
			dlSpeed = Double.parseDouble((String)tokens.nextElement());
			//skip rate type
			tokens.nextElement();
			dlSpeed = Double.parseDouble((String)tokens.nextElement());
			//skip rate type
			tokens.nextElement();
		} catch (Exception ex) {
			ex.getMessage();
			return false;
		}
		return true;
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
		return Integer.toString(reporterId)+interval +" "+Double.toString(transferedData) +" "+ data + 
				" " + Double.toString(dlSpeed) +" "+ rate+" " + Double.toString(ulSpeed) +" "+ rate;
	}

}
