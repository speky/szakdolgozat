package http.filehandler;


public class UDPReport extends TCPReport{
	public  double jitter = 0.0;
	public  int lostDatagram = 0;
	public  int sumDatagram = 0;
	
	public UDPReport() {
		super();
	}

	public UDPReport(int id, String interval, double transferedData,  final double dlSpeed, final double ulSpeed, double jitter, int lost, int sum) {
		super(id, interval, transferedData, dlSpeed, ulSpeed);
		this.jitter = jitter;
		this.lostDatagram = lost;
		this.sumDatagram = sum;
	}

	@Override
	public boolean parseReport(final String report) {
		boolean retValue = super.parseReport(report);
		if (retValue == false) {
			return false;
		}		
		try {
			jitter = Double.parseDouble((String)tokens.nextElement());
			lostDatagram = Integer.parseInt((String)tokens.nextElement());
			sumDatagram = Integer.parseInt((String)tokens.nextElement());
			//tokens.nextElement();
		} catch (Exception ex) {
			ex.getMessage();
			return false;
		}
		return true;

	}

	@Override
	public String getHeader() {
		return "UDP\nId Interval Transfer DLspeed ULspeed Jitter Lost/Total";
	}

	@Override
	public String toString() {
		String base = super.toString(); 
		double percent = (100.0 / new Double(sumDatagram)) * new Double(lostDatagram); 
		return base + " " + Double.toString(jitter) + " " + Integer.toString(lostDatagram) + " " + Integer.toString(sumDatagram) + " ("+percent+")" ;
	}


}
