package httpclient;

import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

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

class PeerThread implements Runnable {
   
    CPeer peer;

    public PeerThread(Socket s, int id) {
        super();
        // regiser a new peer
        peer = new CPeer(s, id, 0);        
    }
    
    public void run()  {
        try  {
             while (true) {
              if (peer.scanner.hasNextLine()) {
                String str = peer.scanner.nextLine();
                if (str.length() != 0) {
                    List<String> order = new ArrayList<String>();
                    order = ((List<String>)Arrays.asList(str.split(" ")));
                    // peer parancsok
                if (order.get(0).equals("ping")){               
                        peer.outputStream.writeChars("pong");
                        peer.outputStream.flush();

                    } else{ // hibás parancs
                    	System.out.println("hibás parancs, bontjuk a kapcsoaltot. (Peer id " + peer.id+" )");
                        peer.socket.close();
                        peer.outputStream.close();
                        peer.scanner.close();
                    }
                }
              }
           }
        }
        catch (Exception e) {
            System.out.println("run hiba " + e.getMessage());            
        }
    
    }
}

class OrderThread extends Thread {
	private Scanner sc;

	public OrderThread(){
		//super();
		sc = new Scanner(System.in);

	}

	@Override
	public void run(){
		try	{
			String line = "";

			while (true){
				if (sc.hasNextLine()) {
					line = sc.nextLine();
					if (line.length() != 0){
						List<String> order = new ArrayList<String>();
						order = ((List<String>)Arrays.asList(line.split(" ")));

						// peer parancsok
						if (order.get(0).equals("getfiles")){
							System.out.println("getfiles parancs" );
							try {
								Socket s = new Socket("localhost", 13468);
								PrintWriter pw = new PrintWriter(s.getOutputStream());
								Scanner sc = new Scanner(s.getInputStream());
								System.out.println("trackertol lekri a fileokat");
								// fájlhoz tartozo peereket lekérjük
								pw.println("getfiles");
								pw.flush();


								String str = sc.nextLine();
								System.out.println("a fileok: " + str);
								System.out.println(str);

								sc.close();
								pw.close();
								s.close();

							}catch (SocketException e){
								System.out.println("tracker reg failed, " + e.getMessage());
							}
						}else if (order.get(0).equals("stopserver")){
							HttpClient.bShutDown = true;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("run hiba " + e.getMessage());
		}
	}
}

public class HttpClient {

	static public boolean bShutDown;
	static public int port;

	public static void main(String[] args) {
		int i = 0;
		bShutDown = false;
		try	{
			ServerSocket ss = new ServerSocket(13666);
			port = ss.getLocalPort();

			// keyboard input scanner
			new Thread(new OrderThread()).start();

			while (!bShutDown) {
				// fogadunk egy kapcsolatot, s a vegpont
				Socket s = ss.accept();
				// elinditunk egy szalat, ami atveszi a peert
				new Thread(new PeerThread(s, i)).start();
				++i;
			}

			System.out.println("SHUT DOWN - nem fogad több kapcsolatot");
		} catch (Exception e) {
			System.out.println("thread hiba" + e.getMessage());
		}
	}
}
