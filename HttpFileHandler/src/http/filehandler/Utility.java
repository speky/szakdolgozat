package http.filehandler;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Utility {
	
	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" => "an example string"
	 */
	public static String decodePercent(String str) {
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
			System.out.println("BAD REQUEST: Bad percent-encoding");
			return null;
		}
	}
	
	public static String calcCheckSum(final byte[]  bytes) {	    
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.reset();
	        md.update(bytes);	        
	        byte[] mdbytes = md.digest();
	        return byteToHex(mdbytes);
	    }   catch(NoSuchAlgorithmException e)  {
	        e.printStackTrace();
	    } 
	    return null;
	}

	private static String byteToHex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}
}
