package httpserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.StringTokenizer;

public class HttpParser {

	/**
	 * Some HTTP response status codes
	 */
	public static final String
	HTTP_OK = "200 OK",
	HTTP_PARTIALCONTENT = "206 Partial Content",
	HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable",
	HTTP_REDIRECT = "301 Moved Permanently",
	HTTP_NOTMODIFIED = "304 Not Modified",
	HTTP_FORBIDDEN = "403 Forbidden",
	HTTP_NOTFOUND = "404 Not Found",
	HTTP_BADREQUEST = "400 Bad Request",
	HTTP_INTERNALERROR = "500 Internal Server Error",
	HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String
	MIME_PLAINTEXT = "text/plain",
	MIME_HTML = "text/html",
	MIME_DEFAULT_BINARY = "application/octet-stream",
	MIME_XML = "text/xml";

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
	
	public HttpParser(Logger logger){
		this.logger = logger;
	}

	/**
	 * Parsethe received header and loads the data into
	 * java Properties' key - value pairs
	 * NOTE: http request's body part will be ignored
	 **/
	public boolean parseHttpHead(final InputStream input) {
		try {      
			if ( input == null){
				logger.addLine(TAG+"Input is empty!");
				return false;
			}

			// Read the first 8192 bytes.  The full header should fit in here. Apache's default header limit is 8KB.
			int bufsize = 8192;
			byte[] buf = new byte[bufsize];
			int rlen = input.read(buf, 0, bufsize);
			if (rlen <= 0) {
				logger.addLine(TAG+"The request is empty!" );
				return false;
			}

			// Create a BufferedReader for parsing the header.
			ByteArrayInputStream byteStream = new ByteArrayInputStream(buf, 0, rlen);
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( byteStream ));
			logger.addLine(TAG+"Server input: " + bufferedReader);
			//Decode the header into parms and header java properties
			parseRequest(bufferedReader);
			//String method = pre.getProperty("method");
			
			//write out the header, 200 ->everything is ok
			//construct_http_header(200, 5);
			return true; 

		}catch (IOException e) {
			logger.addLine(TAG+"Error at input parsing!" + e.getMessage() );
		}
		catch ( InterruptedException e )
		{
			logger.addLine(TAG+" Error at input parsing!" + e.getMessage() );
		}
		return false;
	}

	/**
	 * Decodes the sent headers and loads the data into
	 * java Properties' key - value pairs
	 **/
	private  void parseRequest(final BufferedReader in) throws InterruptedException
	{
		try {
			// Read the request line
			String inLine = in.readLine();
			if (inLine == null) {
				return;
			}
			
			boolean moreToken = parseMethod(inLine);
			// If there's another token, it's protocol version, followed by HTTP headers
			if (moreToken)
			{
				String line = in.readLine();
				while (line != null && line.trim().length() > 0 )
				{
					int separatorPosition = line.indexOf( ':' );
					if ( separatorPosition >= 0 ){
						headerProperty.put( line.substring(0,separatorPosition).trim().toLowerCase(), 
																line.substring(separatorPosition+1).trim());
					}
					line = in.readLine();
				}
			}			
		}
		catch ( IOException e )
		{
			errorText =HTTP_INTERNALERROR;
			logger.addLine("SERVER INTERNAL ERROR: IOException: " + e.getMessage());
		}
	}

	private boolean parseMethod(final String inLine) throws InterruptedException{
		StringTokenizer stringTokens = new StringTokenizer( inLine );
		if ( !stringTokens.hasMoreTokens()){
			errorText =HTTP_BADREQUEST;
			logger.addLine(TAG+"Tokenized string is empty!");
			return false;
		}
		String method = stringTokens.nextToken();
		methodProperty.put("method", method.toUpperCase());

		if ( !stringTokens.hasMoreTokens()){
			logger.addLine(TAG+"Tokenized string is too short!");
			errorText =HTTP_BADREQUEST;
			return false;
		}
		String uri = stringTokens.nextToken();
		uri = decodePercent(uri);
		methodProperty.put("uri", uri);
		
		if ( !stringTokens.hasMoreTokens()){
			logger.addLine(TAG+"Tokenized string is too short!");
			errorText =HTTP_BADREQUEST;
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
	 * For example: "an+example%20string" -> "an example string"
	 */
	private String decodePercent( String str ) throws InterruptedException
	{
		try
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length(); ++i)
			{
				char c = str.charAt( i );
				switch ( c )
				{
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
		catch( Exception e )
		{
			errorText =HTTP_BADREQUEST;
			logger.addLine("BAD REQUEST: Bad percent-encoding");
			return null;
		}
	}

	//this method makes the HTTP header for the response
	//the headers job is to tell the browser the result of the request
	//among if it was successful or not.
	/*private String construct_http_header(int return_code, int file_type) {
		String str = "HTTP/1.0 ";
		//you probably have seen these if you have been surfing the web a while
		switch (return_code) {
		case 200:
			str = str + "200 OK";
			break;
		case 400:
			str = str + "400 Bad Request";
			break;
		case 403:
			str = "403 Forbidden";
			break;
		case 404:
			str =  "404 Not Found";
			break;
		case 500:
			str =  "500 Internal Server Error";
			break;
		case 501:
			str =  "501 Not Implemented";
			break;
		}

		str = str + "\r\n"; //other header fields,
		str = str + "Connection: close\r\n"; //we can't handle persistent connections
		str = str + "Server: SimpleHTTPtutorial v0\r\n"; //server name

		//Construct the right Content-Type for the header.
		//This is so the browser knows what to do with the
		//file, you may know the browser dosen't look on the file
		//extension, it is the servers job to let the browser know
		//what kind of file is being transmitted. You may have experienced
		//if the server is miss configured it may result in
		//pictures displayed as text!
		switch (file_type) {
		//plenty of types for you to fill in
		case 0:
			break;
		case 1:
			str = str + "Content-Type: image/jpeg\r\n";
			break;
		case 2:
			str = str + "Content-Type: image/gif\r\n";
			break;
		case 3:
			str = str + "Content-Type: application/x-zip-compressed\r\n";
			break;
		case 4:
			str = str + "Content-Type: image/x-icon\r\n";
			break;
		default:
			str = str + "Content-Type: text/html\r\n";
			break;
		}

		////so on and so on......
		str = str + "\r\n"; //this marks the end of the httpheader
		//and the start of the body
		//ok return our newly created header!
		logger.addLine("outpu: " + str);
		return str;
	}*/
}
