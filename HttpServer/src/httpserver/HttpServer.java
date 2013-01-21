
package httpserver;

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;

/**
 *
 * @author Specker Zsolt
 */

/*
class CPeer {
    public Socket sock;
    public PrintWriter pw;
    public Scanner sc;
    public int id, port;
    public boolean bPong = false;

    public Set<String> files;

    CPeer(Socket s, int id, int port){
        try{
            sock = s;
            this.id = id;
            this.port = port;
            OutputStream os = sock.getOutputStream();
            pw = new PrintWriter(new OutputStreamWriter(os) );
            sc = new Scanner(sock.getInputStream());
            files = new HashSet<String>();

        }
        catch(Exception e){
            TrackerThread.logAdd("peer hiba: " + e.getMessage() + " Peer id " + id+" )");
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
                    TrackerThread.logAdd("reg add peer to " + name + ":"+ id +" pocket (Peer id " + peer.id + ")");
                    return;
                }else
                    TrackerThread.logAdd("reg wrong pocket data! ");
                    return;
            }
        }

        // uj csomag
        TrackerThread.logAdd("reg add peer to new pocket " + name + ":"+ id +" (Peer id " + peer.id + ")");
        CPocket p = new CPocket("", 0, id, hash);
        pieces.add(p);
        p.peers.add(peer);

    }
}
 */


class TrackerThread extends Thread
{
	//static List<CFile> files = new ArrayList<CFile>();
	//static ArrayList<CPeer> activePeers = new ArrayList<CPeer>();

	//CPeer peer;

	public TrackerThread(Socket s, int id) {
		super();

		// regiser a new peer        
		// peer = new CPeer(s, id, 0);

		//        logAdd("Add a peer, id: " + peer.id + " IP: " + s.getInetAddress());
		start();
	}

	private void parser(){
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

	public void run()
	{
		/*try
        {           
            while (true) {
              if ( sc.hasNextLine()){
                String str = sc.nextLine();
            }
           }

        } catch (Exception e) {
            System.out.println("run hiba " + e.getMessage());
            //logAdd("run hiba " + e.getMessage()+" (Peer id " + peer.id+" )");
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

	private final int SERVER_PORT = 13000;
	private final int SERVER_PORT_END = 13050;

	private ServerSocket serverSocket = null;
	private Logger logger = null;
	private int threadCount;

	public void main(String[] args) {
		threadCount = 0;
		logger = new Logger();
		logger.Init("");
		try
		{
			// serverSocket a vegpont generator
			serverSocket = new ServerSocket(SERVER_PORT);

			while (true) {
				// fogadunk egy kapcsolatot, socket a vegpont
				Socket socket = serverSocket.accept();
				threadCount++;
				parseClientRequest(socket);
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

	private void parseClientRequest(Socket socket){
		//figure out what ipaddress the client commes from, just for show!
		InetAddress client = socket.getInetAddress();
		//and print it to log
		logger.addLine(client.getHostName() + " connected to server.\n");
		HttpParser parser = new HttpParser(logger);
				
		DataOutputStream output = null;
		try {
			//Read the http request from the client from the socket interface into a buffer.
			parser.parseHttpHead(socket.getInputStream());
			//Prepare an outputstream from server to the client, 
			// this will be used sending back our response (header + requested file) to the client.
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Hiba a client socket-el: " + e.getMessage());
		}
		
		// elinditunk egy szalat, ami atveszi a vegpontot...
		//new Thread(new TrackerThread(socket, ));
	}	
}
