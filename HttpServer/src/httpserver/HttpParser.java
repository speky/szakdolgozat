package httpserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.StringTokenizer;

public class HttpParser {

	private Logger logger = null;

	private Properties methodProperty = new Properties();
	private Properties headerProperty = new Properties();

	private String TAG = "HTTP_PARSER: ";
	private String errorText = "";

	public String getErrorText(){
		return errorText;
	}

	public String getMethod(){
		return methodProperty.getProperty("method") ;
	}

	public String getVersion(){
		return methodProperty.getProperty("version") ;
	}

	public int getMethodSize(){
		return methodProperty.size();
	}
	
	public int getHeadSize(){
		return headerProperty.size();
	}
	
	public String getHeadTag(final String tag){
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
	public boolean parseHttpHead(final InputStream input) {
		try {      
			if ( input == null){
				logger.addLine(TAG+"Input is empty!");
				return false;
			}

			// Read the first 8192 bytes.  The full header should fit in here. Apache's default header limit is 8KB.
			int bufferSize = 8192;
			byte[] buffer = new byte[bufferSize];
			int readedLength = input.read(buffer, 0, bufferSize);
			if (readedLength <= 0) {
				logger.addLine(TAG+"The http request is empty!" );
				return false;
			}
			
			methodProperty.clear();
			headerProperty.clear();
			// Create a BufferedReader for parsing the header.
			ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer, 0, readedLength);
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(byteStream));			
			//Decode the header into params and header java properties
			if  (!parseRequest(bufferedReader)){
				return false;
			}
			return true; 
		} catch (IOException e) {
			logger.addLine(TAG+"Error at input parsing!" + e.getMessage() );
		} catch ( InterruptedException e ) {
			logger.addLine(TAG+" Error at input parsing!" + e.getMessage() );
		}
		return false;
	}

	/**
	 * Decodes the received headers and loads data into java Properties' key - value pairs
	 **/
	private  boolean parseRequest(final BufferedReader in) throws InterruptedException	{
		try {
			// Read the request line
			String inLine = in.readLine();			
			if (inLine == null) {
				return false;
			}
			logger.addLine(TAG+"Server input method: "+ inLine);

			if  (parseMethod(inLine)){
				return false;
			}
			// If there's another token, it's protocol version, followed by HTTP headers
			// example: Header1: value1
			// Header2: value2						
			String line = in.readLine();			
			while (line != null && line.trim().length() > 0 )	{
				logger.addLine(TAG+"Parse head "+ line);
				int separatorPosition = line.indexOf( ':' );
				if  (separatorPosition >= 0) {
					String type = line.substring(0,separatorPosition).trim().toUpperCase();
					String value = line.substring(separatorPosition+1).trim();
					headerProperty.put(type, value);							
				}
				line = in.readLine();
			}
			return true;
		}	catch (IOException e) {
			errorText = HttpResponse.HTTP_INTERNALERROR;
			logger.addLine("SERVER INTERNAL ERROR: IOException: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Tokenize input header and set method property 
	 * example: GET /path/to/file/index.html HTTP/1.0
	 * method: GET; uri: /path/to/file/index.html; version: HTTP/1.0
	 **/
	private boolean parseMethod(final String inLine) throws InterruptedException {
		StringTokenizer stringTokens = new StringTokenizer(inLine);
		if (!stringTokens.hasMoreTokens()) {
			errorText = HttpResponse.HTTP_BADREQUEST;
			logger.addLine(TAG+"Tokenized string is empty!");
			return false;
		}
		String method = stringTokens.nextToken();
		methodProperty.put("method", method.toUpperCase());

		if ( !stringTokens.hasMoreTokens()){
			logger.addLine(TAG+"Tokenized string is too short!");
			errorText = HttpResponse.HTTP_BADREQUEST;
			return false;
		}
		String uri = stringTokens.nextToken();
		uri = decodePercent(uri);
		methodProperty.put("uri", uri);

		if ( !stringTokens.hasMoreTokens()){
			logger.addLine(TAG+"Tokenized string is too short!");
			errorText = HttpResponse.HTTP_BADREQUEST;
			return false;
		}
		String httpVersion= stringTokens.nextToken();
		methodProperty.put("version", httpVersion);

		if ( stringTokens.hasMoreTokens()){
			return true;
		}
		return false;
	}
	
	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" => "an example string"
	 */
	private String decodePercent(String str) throws InterruptedException	{
		try	{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length(); ++i) {
				char c = str.charAt(i);
				switch ( c ) {
				case '+':
					sb.append( ' ' );
					break;
				case '%':
					sb.append((char)Integer.parseInt( str.substring(i+1, i+3), 16));
					i += 2;
					break;
				default:
					sb.append( c );
					break;
				}
			}
			return sb.toString();
		}
		catch( Exception e ) {
			errorText = HttpResponse.HTTP_BADREQUEST;
			logger.addLine("BAD REQUEST: Bad percent-encoding");
			return null;
		}
	}
}
