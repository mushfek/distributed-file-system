import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputOutputHandler {
	public int totalServerNodes=0, totalClientNodes=0;
	public Map<String, List<String>> serverMap = new HashMap<>();
	public Map<String, List<String>> clientMap = new HashMap<>();
	
	public int readServerConfig() {
		try (BufferedReader br = new BufferedReader(new FileReader("host_configs/server.conf"))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (!sCurrentLine.startsWith("#")) {
					String[] tokens = sCurrentLine.split(" ");
					if(tokens[1].equals("#")) {
						totalServerNodes = Integer.parseInt(tokens[0]);
						System.out.println(totalServerNodes);

					} else {
						List<String> valueList = new ArrayList<String>();
						valueList.add(tokens[1]);
						valueList.add(tokens[2]);
						serverMap.put(tokens[0], valueList);
					}
				}
			}
			
			// Testing the HashMap output
			for (Map.Entry<String, List<String>> entry : serverMap.entrySet()) {
				String key = entry.getKey();	
				List<String> values = entry.getValue();							
				System.out.println("Key = " + key);
				System.out.println("Values = " + values);
				// get(o) is host get(1) is port
				//System.out.println("Values = " + values.get(0));
				//System.out.println("Values = " + values.get(1));
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		}

		return totalServerNodes;
	}
	
	public int readClientConfig() {
		try (BufferedReader br = new BufferedReader(new FileReader("host_configs/client.conf"))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (!sCurrentLine.startsWith("#")) {
					String[] tokens = sCurrentLine.split(" ");
					if(tokens[1].equals("#")) {
						totalClientNodes = Integer.parseInt(tokens[0]);
						System.out.println(totalClientNodes);

					} else {
						List<String> valueList = new ArrayList<String>();
						valueList.add(tokens[1]);
						valueList.add(tokens[2]);
						clientMap.put(tokens[0], valueList);
					}
				}
			}
			
			for (Map.Entry<String, List<String>> entry : clientMap.entrySet()) {
				String key = entry.getKey();	
				List<String> values = entry.getValue();							
				System.out.println("Key = " + key);
				System.out.println("Values = " + values);
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		}

		return totalClientNodes;
	}
}
