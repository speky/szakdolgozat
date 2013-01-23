
package httpserver;

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;

/**
 *
 * @author Specker Zsolt
 */

class CPeer {
	public Socket socket;
	public DataOutputStream outputStream;
	public Scanner scanner;
	public int id;
	public int port;
	public HashSet<String> files;

	CPeer(final Socket socket, final int id, final int port) {
		try{
			this.socket = socket;
			this.id = id;
			this.port = port;			
			outputStream = new DataOutputStream(socket.getOutputStream());
			scanner = new Scanner(socket.getInputStream());
			files = new HashSet<String>();
		}
		catch(Exception e){
			System.out.println("peer hiba: " + e.getMessage() + " Peer id " + id);
		}
	}
}

class CPocket {
	public String text;
	public int id;
	public int hashCode;
	public Set<CPeer> peers;

	CPocket(){
		text = "";
		id = -1;
		hashCode = 0;
		peers = new HashSet<CPeer> ();
	}

	CPocket(String text, int length, int id, int hashCode){
		this.text = text;
		this.id = id;
		this.hashCode = hashCode;
		peers = new HashSet<CPeer> ();
	}
}

class CFile {
	public String name;
	public  int hashId;
	public  int count;
	public Set<CPocket> pieces;

	CFile(){
		name = "";
		hashId = 0;
		pieces = new HashSet<CPocket> ();

	}

	CFile(String name, int count, int hashId){
		this.name = name;
		this.hashId = hashId;
		this.count = count;
		pieces = new HashSet<CPocket> ();
	}

	public void AddPiece(int id, int hash, CPeer peer) {
		for (CPocket p : pieces){
			if (p.id == id ){
				// add peer to the existing piece
				if (p.hashCode == hash){
					p.peers.add(peer);
					// TrackerThread.logAdd("reg add peer to " + name + ":"+ id +" pocket (Peer id " + peer.id + ")");
					return;
				}else
					//TrackerThread.logAdd("reg wrong pocket data! ");
					return;
			}
		}

		// uj csomag
		//  TrackerThread.logAdd("reg add peer to new pocket " + name + ":"+ id +" (Peer id " + peer.id + ")");
		CPocket p = new CPocket("", 0, id, hash);
		pieces.add(p);
		p.peers.add(peer);

	}
}

class TrackerThread extends Thread {
	static List<CFile> files = new ArrayList<CFile>();

	private CPeer peer = null;
	private Logger logger = null;

	public TrackerThread(Logger logger, final Socket socket, final int id) {
		super();
		// register a new peer        
		peer = new CPeer(socket, id, 0);
		this.logger = logger;
		logger.addLine("Add a peer, id: " + peer.id + " IP: " + socket.getInetAddress());

		start();
	}
	
	public void run() {
		try {   
			while (true) {
				if  (peer.scanner. hasNextLine()){
					logger.addLine("Get message from client,  clientId: " + peer.id+"\n");
					HttpParser parser = new HttpParser(logger);
					parseClientRequest(parser);
					sendResponse(parser);
				}
			}
		} catch (Exception e) {            
			logger.addLine("error in run() " + e.getMessage()+" (Peer id " + peer.id+" )");
		} 
	}

	private boolean sendResponse(HttpParser parser) {
		try {
			HttpResponse response = new HttpResponse(logger);
			response.PrintProperties(parser.getMethosProperty("uri"), parser.getMethod(), parser.getHeadProperty(), null);
			
			String errorMessage = parser.getErrorText();
			if (errorMessage != null) {
				String responseText = response.setResponseText(errorMessage, HttpResponse.MIME_PLAINTEXT, null);
				sendMessageToClient(responseText);
			}
			
			String responseText = response.setResponseText(HttpResponse.HTTP_OK, HttpResponse.MIME_PLAINTEXT, null);
			sendMessageToClient(responseText);			
			
		}catch (Exception e){
			logger.addLine("response error: " + e.getMessage());
		}
		
		return false;
	}
	
	private boolean sendMessageToClient(final String message) {
		try {
			peer.outputStream.writeChars(message);
			peer.outputStream.flush();
			return true;
		} catch (IOException e) {
			logger.addLine("Message sending exception: " + e.getMessage()+" (Peer id " + peer.id+" )");
		}		
		return false;
	}
	
	private void parseClientRequest(HttpParser parser) {		
		//Read the http request from the client from the socket interface into a buffer.
		parser.parseHttpHead(peer.scanner);
	}
		
	private void parser() {
		/*      if (str.length() != 0){

             List<String> order = new ArrayList<String>();
             order = ((List<String>)Arrays.asList(str.split(" ")));
             // reg parancs
             if (order.get(0).equals("reg")){
                 logAdd("reg parancs (Peer id " + peer.id + " )");
                 // legalább 6 szóból kell állnia
                 if (order.size() <= 6){
                     logAdd("reg parancs rövid");
                     return;
                 }
                                            // name          darabok száma               hash code
                 CFile file = new CFile(order.get(1), Integer.parseInt(order.get(2)), Integer.parseInt(order.get(3)));
                 // megnézük hogy létezik-e már a fájl
                 file = AddFile(file);
                 if (file == null)
                     return;

                 // add file name to the actual peer's list
                 peer.files.add(file.name);

                 // peer portja
                 peer.port = Integer.parseInt(order.get(4));
                 // peernél lévő darabok száma
                 int count = Integer.parseInt(order.get(5));

                 int id = 0, hash = 0;
                 for (int i = 0; i < count; i++){
                     id = Integer.parseInt(order.get(6+(2*i)));
                     hash = Integer.parseInt(order.get(7+(2*i)));
                     //file.pieces.add((new CPocket("", 0, id, hashCode)).peers.add(peer));
                    file.AddPiece(id, hash, peer);
                 }

                 logAdd("reg parancs vége (Peer id " + peer.id + ")");


             }// getpeersforfile parancs
             else  if (order.get(0).equals("getpeersforfile")){
                 logAdd("getpeersforfile parancs (Peer id " + peer.id+" )");
                 // legalább 2 szóból kell állnia
                 if (order.size() < 2){
                     logAdd("getpeersforfile parancs rövid");
                     peer.pw.println("nincs ilyen file");
                     peer.pw.flush();
                     return;
                 }

                 CFile ff = null;
                 // find the filename in the database
                 for (CFile f : TrackerThread.files){
                     if (f.name.equals(order.get(1))) {
                         ff = f;
                         break;
                     }
                 }


                 if (ff == null){
                     logAdd("getpeersforfile file nem létezik -" + order.get(1));
                     peer.pw.println("nincs ilyen file");
                     peer.pw.flush();
                     return;
                 }

                 // file's hash code
                 str =(ff.hashId + " ");

                 // count the peers 
                 List<CPeer> list = new ArrayList<CPeer>();
                 //CPeer p = null;
                 for (CPeer p : TrackerThread.activePeers){
                     //p = TrackerThread.activePeers.get(i);
                     if (p.files.contains(order.get(1))) {
                         list.add(p);
                     }
                 }
                 // peerek száma
                 str += (list.size()+ " ");


                 for (CPeer p : list){
                     // write peer's datas
                     str +=(p.sock.getInetAddress() + " ");

                     str += p.port+ " ";

                   // add a used pockets to the list
                     List<CPocket> poc = new ArrayList<CPocket>();
                     for (CPocket pp : ff.pieces){
                         if (pp.peers.contains(p)) {
                             poc.add(pp);
                         }
                     }
                     // pockets size
                    str +=(poc.size() + " ");

                     //write  pockets properties
                     for (CPocket pp : poc){
                        str +=(pp.id + " ");
                        str +=(pp.hashCode + " ");
                     }
                 }

                 peer.pw.println(str);
                 peer.pw.print("\r\n");
                 peer.pw.flush();

                 logAdd("getfiles parancs (Peer id " + peer.id+" )");
             }// getfiles parancs
             else  if (order.get(0).equals("getfiles")){
                 logAdd("getfiles parancs (Peer id " + peer.id+" )");

                  for (CFile f: files){
                        peer.pw.print(f.name + " ");

                  }
                 peer.pw.print("\r\n");
                 peer.pw.flush();

             }else  if (order.get(0).equals("pong")){
                 logAdd("pong üzi (Peer id " + peer.id+" )");
                 peer.bPong = true;
             } else{ // hibás parancs
                 logAdd("hibás parancs, bontjuk a kapcsoaltot. (Peer id " + peer.id+" )");
                 peer.sock.close();
                 peer.pw.close();
                 peer.sc.close();
                 TrackerThread.activePeers.remove(peer);

             }
         }
		 */
	}
	
	/*
    private CFile AddFile(CFile file) {
        for (CFile fi : TrackerThread.files){
            if (fi.name == null ? file.name == null : fi.name.equals(file.name))
                if ( fi.count == file.count && fi.hashId == file.hashId){
                    logAdd("reg file still exist: " + file.name);
                    return fi;

                }else {
                    logAdd("reg file's datas are wrong! -" + file.name);
                    return null;
                }
        }
        logAdd("reg new file: " + file.name);
        // add new file
        TrackerThread.files.add(file);
        return file;
    }*/

}

public class HttpServer {

	private static final int SERVER_PORT = 13000;
	private static final String TAG = "HTTP_Server: ";

	private ServerSocket serverSocket = null;
	private Logger logger = null;
	private int activeConnections;

	public void decreaseConnectounCount() {
		if  (activeConnections > 0) {
			--activeConnections;
			logger.addLine(TAG+" decrease connections: "+ activeConnections);
		}
	}

	public void inreaseConnectounCount() {		
		++activeConnections;
		logger.addLine(TAG+" increase connections: "+ activeConnections);
	}

	public void main(String[] args) {
		activeConnections = 0;
		logger = new Logger();
		logger.Init("");
		try	{
			// serverSocket a vegpont generator
			serverSocket = new ServerSocket(SERVER_PORT);

			while (true) {
				// wait for client connection
				Socket socket = serverSocket.accept();				
				//figure out what is the ip-address of the client
				InetAddress client = socket.getInetAddress();
				//and print it to log
				logger.addLine(TAG+client.getHostName() + " connected to server.\n");
				// start thread for handling a client
	             new Thread(new TrackerThread(logger, socket, activeConnections));
	             inreaseConnectounCount();
			}
		} catch (Exception e) {
			System.out.println("Thread hiba: " + e.getMessage());
		} finally {
			try {
				serverSocket.close();
				logger.addLine("Server socket closed");
				logger.closeFile();
				System.out.println("Connection closed");
			} catch (Exception e) {
				System.err.println("Hiba a kapcsolat lezarasa kozben.");
			}
		}
	}
	
}
