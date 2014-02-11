package httpserver;


import http.testhandler.HttpParser;
import http.testhandler.Logger;

import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

public class HttpResponse {

	/**
	 * Common mime types for dynamic content
	 */
	public static final String	MIME_PLAINTEXT = "text/plain";
	public static final String MIME_DEFAULT_BINARY = "application/octet-stream";

	private static final String TAG = "HTTP_RESPONSE: ";
	private static String ROOT_DIR = "c:\\";

	private Logger logger = null;

	public HttpResponse(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Set the response text
	 */
	public String setResponseText(final String status, final String mime, final Properties header) {		
		if  (status == null) {
			logger.addLine(TAG+"sendResponseText: Status can't be null.");
			return null;
		}
		logger.addLine(TAG+"Set response text");
		StringBuffer responseString = new StringBuffer("HTTP*/1.0 " + status + " \r\n");

		if  (mime != null) {
			responseString.append("Content-Type: " + mime + "\r\n");
		}

		if  (header == null || header.getProperty("DATE") == null) {
			responseString.append("Date: " + gmtFormat.format(new Date()) + "\r\n");
		}

		if  (header != null) {
			Enumeration<Object> e = header.keys();
			while (e.hasMoreElements())	{
				String key = (String)e.nextElement();
				String value = header.getProperty(key);
				responseString.append(key + ": " + value + "\r\n");
			}
		}
		responseString.append("END\n");
		logger.addLine(TAG+"response text:" + responseString);

		return responseString.toString();
	}

	/**
	 * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @param method	"GET", "HEAD" etc.
	 * @param header	Header entries, percent decoded	 
	 */
	public void PrintProperties(String uri, String method, Properties header, Properties files) {
		logger.addLine(TAG +"start serve "+ method + " '" + uri + "' " );

		Enumeration<?> enumElements = header.propertyNames();
		while (enumElements.hasMoreElements()) {
			String value = (String)enumElements.nextElement();
			logger.addLine(TAG +"  Header: '" + value + "' = '" + header.getProperty(value) + "'" );
		}

		if ( files != null) {	
			enumElements = files.propertyNames();
			while (enumElements.hasMoreElements()) {
				String value = (String)enumElements.nextElement();
				logger.addLine(TAG +"  File: '" + value + "' = '" + files.getProperty(value));
			}	
		}
	}		

	/**
	 * GMT date formatter
	 */
	public static java.text.SimpleDateFormat gmtFormat;
	static	{
		gmtFormat = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
}
