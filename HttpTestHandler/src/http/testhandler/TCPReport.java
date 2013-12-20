package http.testhandler;

import http.testhandler.ReportReceiver.DataType;
import http.testhandler.ReportReceiver.RateType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class TCPReport {
	
	public  static final Map<DataType, String> DataString;
    static {
    	Map<DataType, String> aMap = new HashMap<DataType, String> ();
        aMap.put(DataType.BYTE, "B");
        aMap.put(DataType.KB, "KB");
        aMap.put(DataType.MB, "MB");
        DataString = Collections.unmodifiableMap(aMap);
    }
    
	public static final Map<RateType, String> RateString;
	static {
    	Map<RateType, String> aMap = new HashMap<RateType, String> ();
        aMap.put(RateType.BITS, "bits");
        aMap.put(RateType.KBITS, "kbits");
        aMap.put(RateType.MBITS, "mbits");
        RateString = Collections.unmodifiableMap(aMap);
    }
		
	public  int reporterId = 0;
	public long  interval = 0;
	public  double transferedData = 0.0;
	public  double dlSpeed = 0.0;
	public  double ulSpeed = 0.0;
	protected DataType data = DataType.BYTE;
	protected RateType rate = RateType.BITS;
	protected StringTokenizer tokens = null;
		
	public TCPReport() {
		init();
	}
		
	public TCPReport(final int id, final long interval, final double transferedData, final double dlSpeed, final double ulSpeed) {
		init();
		reporterId = id;
		this.interval = interval;
		this.transferedData = transferedData;
		this.dlSpeed = dlSpeed;
		this.ulSpeed = ulSpeed;
	}

	private void init() {		
		reporterId = 0;
		interval = 0;
		transferedData = 0.0;
		dlSpeed = 0.0;
		ulSpeed = 0.0;
	}
	
	public long getInterval() { return interval; }
	
	public double getTransferedData() { return transferedData; }
	
	public boolean parseReport(final String report) {
		if (report == null) {
			return false;
		}
		tokens = new StringTokenizer(report);
		try {			
			reporterId = Integer.parseInt((String)tokens.nextElement());
			interval = Long.parseLong((String)tokens.nextElement());			
			transferedData = Double.parseDouble((String)tokens.nextElement());			
		} catch (Exception ex) {
			ex.getMessage();
			return false;
		}
		return true;
	}

	public void setTransferedData(Double data) {
		this.transferedData = data;
	}
	
	public void setDLSpeed(Double speed) {
		this.dlSpeed = speed;
	}
	
	public void setULSpeed(Double speed) {
		this.ulSpeed = speed;
	}
	
	public void setData(DataType type) {
		data = type;
	}

	public void setRate(RateType type) {
		rate = type;
	}

	public String getHeader() {
		return "TCP\nId Interval Transfer DL speed UL speed";
	}

	@Override
	public String toString() {		
		return Integer.toString(reporterId)+" " +Long.toString(interval)+" sec" +" "+Double.toString(transferedData) +" "+ DataString.get(data)+ 
				" " + Double.toString(dlSpeed) +" "+ RateString.get(rate)+" " + Double.toString(ulSpeed) +" "+ RateString.get(rate);
	}

}
