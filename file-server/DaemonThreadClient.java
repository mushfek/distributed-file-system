import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class DaemonThreadClient extends Thread {
	Socket socket;
	BufferedReader BR;
	InputOutputHandler IOH;
	
	DaemonThreadClient(Socket socket, InputOutputHandler IOH) {
		super();

		start();
		this.socket = socket;
		this.IOH = IOH;
		try {
			BR = ServerNode.clientReaders.get(socket);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String message;

		try {
			while ((message = BR.readLine() ) != null) {
				String tokens[] = message.split(",");
				String messageType = tokens[0];
				
				System.out.println("Message at "+ServerNode.serverNodeID+": "+message);
				
				if (messageType.equals("READ")) {
					BufferedReader reader = new BufferedReader(new FileReader(tokens[1]+"_"+ServerNode.serverNodeID+".txt"));
					String line = reader.readLine();
					
					Socket bs = ServerNode.serverSocketMap.get(tokens[2]);
					System.out.println("bs:"+bs);

					PrintWriter writer = ServerNode.serverWriters.get(bs);
		            writer.println("DATA,"+ServerNode.serverNodeID+","+line);
		            writer.flush();

		            System.out.println("Sending READ REPLY to client:"+tokens[2]);	
				}

				if (messageType.equals("WRITE")) {
					PrintWriter writer = new PrintWriter(tokens[1]+"_"+ServerNode.serverNodeID+".txt", "UTF-8");
					writer.println(tokens[3]);
					writer.close();
					
					// Sending Order to Secondary Nodes
					
					Socket bs = ServerNode.serverSocketMap.get(String.valueOf((ServerNode.serverNodeID+1) % ServerNode.SERVERNUMNODES));
					System.out.println("bs:"+bs);
					writer = ServerNode.serverWriters.get(bs);
		            writer.println("ORDER,"+tokens[1]+","+tokens[2]);
		            writer.flush();

		            System.out.println("Sending ORDER to server:"+(ServerNode.serverNodeID+1) % ServerNode.SERVERNUMNODES);
		            
		            bs = ServerNode.serverSocketMap.get(String.valueOf((ServerNode.serverNodeID+2) % ServerNode.SERVERNUMNODES));
					System.out.println("bs:"+bs);

					writer = ServerNode.serverWriters.get(bs);
		            writer.println("ORDER,"+tokens[1]+","+tokens[2]);
		            writer.flush();

		            System.out.println("Sending ORDER to server:"+(ServerNode.serverNodeID+2) % ServerNode.SERVERNUMNODES);
				}
				
				if (messageType.equals("REPLICATE")) {

					if (IsWriteAllowed(message)) {
						System.out.println("HURRAY");
						writeObject(message);

						Iterator<String[]> it = ServerNode.bufferedOrder.iterator();
						while(it.hasNext()) {
							String[] arr = (String[]) it.next();
							if(IsWriteAllowed(arr.toString())){
								writeObject(arr.toString());
								ServerNode.bufferedOrder.remove(arr);
							}
						}
						
					} else {
						String[] bufferedOrder = {tokens[0],tokens[1],tokens[2],tokens[3]};
						ServerNode.bufferedOrder.add(bufferedOrder);
						System.out.println("SOP of bufferedOrder"+ServerNode.bufferedOrder);
					}
					

				}
				
				if(messageType.equals("PING")) {
					if(ServerNode.isNodeUp == Boolean.valueOf(tokens[3])) {
						Socket bs = ServerNode.clientSocketMap.get(tokens[1]);
						System.out.println("bs:"+bs);

						PrintWriter writer = ServerNode.clientWriters.get(bs);
			            writer.println("YES,"+ServerNode.serverNodeID+","+tokens[2]);
			            writer.flush();

			            System.out.println("Sending PING REPLY YES to client:"+tokens[1]);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	boolean IsWriteAllowed(String message) {
		String tokens[] = message.split(",");

		Iterator it = ServerNode.writeOrder.iterator();
		List<String[]> tempOrder = new ArrayList<String[]>();
		String[] arr = null;

		while(it.hasNext()) {
			arr = (String[]) it.next();

			if(arr[0].equalsIgnoreCase(tokens[1])) {
				tempOrder.add(arr);
			}
		}

		if(tempOrder.get(0)[1].equalsIgnoreCase(tokens[2])) {
			ServerNode.writeOrder.remove(arr);
			return true;

		} 
		
		return false;
	}
	
	void writeObject(String message) {
		String tokens[] = message.split(",");
		PrintWriter writer;

		try {
			writer = new PrintWriter(tokens[1]+"_"+ServerNode.serverNodeID+".txt", "UTF-8");
			writer.println(tokens[3]);
			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
