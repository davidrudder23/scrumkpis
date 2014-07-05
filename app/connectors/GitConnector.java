package connectors;

import java.util.ArrayList;
import java.util.List;

public class GitConnector extends Connector {

	public void run() {
		
	}
	
	public List<String> getParameterNames() {
		List<String> paramNames = new ArrayList<String>();
		return paramNames;
	}

	
	public String getName() {
		return "Git";
	}

}
