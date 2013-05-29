package http.filehandler;

import java.util.Properties;
import java.util.StringTokenizer;

public class HttpParser {

	// Some HTTP response status codes	
	public static final String
	HTTP_OK = "200 OK",	
	HTTP_FORBIDDEN = "403 Forbidden",
	HTTP_NOTFOUND = "404 Not Found",
	HTTP_BADREQUEST = "400 Bad Request",
	HTTP_INTERNALERROR = "500 Internal Server Error",
	HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	private Logger logger = null;

	private Properties methodProperty = new Properties();
	private Properties headerProperty = new Properties();

	private String TAG = "HTTP_PARSER: ";
	private String errorText = null;

	public String getErrorText(){
		return errorText;
	}

	public void setErrorText(final String errorMessage){
		errorText = errorMessage;
	}
	
	public String getMethod(){
		return methodProperty.getProperty("METHOD") ;
	}

	public String getVersion(){
		return methodProperty.getProperty("VERSION") ;
	}

	public int getMethodSize(){
		return methodProperty.size();
	}

	public int getHeadSize(){
		return headerProperty.size();
	}

	public String getMethodProperty(final String tag){
		if (methodProperty.containsKey(tag)){
			return methodProperty.getProperty(tag) ;
		}else{
			return null;
		}		
	}
	
	public Properties getHeadProperty(){
		return headerProperty;
	}
	
	public String getHeadProperty(final String tag){
		if (headerProperty.containsKey(tag)){
			return headerProperty.getProperty(tag) ;
		}else{
			return null;
		}		
	}

	public HttpParser(Logger logger){
		this.logger = logger;
	}

	/**
	 * Parse the received header and loads the data into
	 * java Properties' key - value pairs
	 * NOTE: HTTP request's body part will be ignored
	 **/
	public boolean parseHttpMessage(final String input) {
		try {
			if ( input == null) {
				logger.addLine(TAG+"Input is empty!");
				return false;
			}
			methodProperty.clear();
			headerProperty.clear();
			//Decode the header into params and header java properties
			if  (!parseRequest(input)) {
				return false;
			}
			return true;		
		} catch ( InterruptedException e ) {
			logger.addLine(TAG+" Error at input parsing!" + e.getMessage() );
		}
		return false;
	}

	/**
	 * Decodes the received headers and loads data into java Properties' key - value pairs
	 **/
	private  boolean parseRequest(String inLine) throws InterruptedException	{		
		logger.addLine(TAG+"Server input method: "+ inLine);
		StringTokenizer token= new StringTokenizer(inLine, "+");
		
		if  (!parseMethod(token.nextToken())) {
			return false;
		}
		// If there's another token, it's protocol version, followed by HTTP headers
		// example: Header1: value1
		// Header2: value2		
		while (token.hasMoreTokens()) {
			String line = token.nextToken();
			if (line.trim().length() > 0) {
				logger.addLine(TAG+"Parse head "+ line);
				int separatorPosition = line.indexOf(':');
				if  (separatorPosition >= 0) {
					String type = line.substring(0,separatorPosition).trim().toUpperCase();
					String value = line.substring(separatorPosition+1).trim();
					headerProperty.put(type, value);							
				}
			}
		}
		return true;		
	}

	/**
	 * Tokenize input header and set method property 
	 * example: GET /path/to/file/index.html HTTP/1.0
	 * method: GET; uri: /path/to/file/index.html; version: HTTP/1.0
	 **/
	private boolean parseMethod(final String inLine) throws InterruptedException {
		StringTokenizer stringTokens = new StringTokenizer(inLine);
		if (!stringTokens.hasMoreTokens()) {
			errorText = HTTP_BADREQUEST;
			logger.addLine(TAG+"Tokenized string is empty!");
			return false;
		}
		String method = stringTokens.nextToken();
		methodProperty.put("METHOD", method.toUpperCase());

		if ( !stringTokens.hasMoreTokens()){
			logger.addLine(TAG+"Tokenized string is too short!");
			errorText = HTTP_BADREQUEST;
			return false;
		}
		String uri = stringTokens.nextToken();
		uri = Utility.decodePercent(uri);
		methodProperty.put("URI", uri);

		if ( !stringTokens.hasMoreTokens()){
			logger.addLine(TAG+"Tokenized string is too short!");
			errorText = HTTP_BADREQUEST;
			return false;
		}
		String httpVersion= stringTokens.nextToken();
		methodProperty.put("VERSION", httpVersion);

		return true;
	}
}
