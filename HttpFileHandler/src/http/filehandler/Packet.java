package http.filehandler;

public class Packet {
		public String text;
		public int id;
		public String hashCode;		

		public Packet(){
			text = "";
			id = -1;
			hashCode = "";
		}

		public Packet(String text, int length, int id) {
			this.text = text;
			this.id = id;
			this.hashCode = Utility.calcSHA1(text);
		}	
}
