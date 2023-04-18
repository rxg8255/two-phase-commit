import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

public class Server implements CoordinatorInterface{
	private NodeInterface[] nodes = new NodeInterface[2];
	private String action = "";
	
	private String log;
	private boolean[] voteCommitArray = {false,false};

	@Override
	public void recVote(String voteMsg, int nodeNum) throws InterruptedException, RemoteException {
		if(voteMsg.equals("commitTcFailureBefore")) {
			readAction(nodeNum);
			return;
		}
		String log = "Node " + nodeNum + " has voted - " + voteMsg;
		logRecord(log);
		System.out.println(log);
		if(action.equals("7")){
			if(nodeNum==0) {
				this.nodes[0].reqCommit();
				log = "Sending commit message to node - 0";
				logRecord(log);
				System.out.println(log);
				log = "Coordinator fails after sending commit to Node 0";
				logRecord(log);
				System.out.println(log);
			}
			if(nodeNum==1) {
				TimeUnit.SECONDS.sleep(10);
				
				log = "Coordinator recovers and reads log to send commit to other node";
				logRecord(log);
				System.out.println(log);
				boolean flag=false;
				try {
					flag = readFile(); //Read log
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(flag){
					log = "Sending commit message to node - 1";
					logRecord(log);
					System.out.println(log);
					this.nodes[1].reqCommit();
				}
			}
			readAction(nodeNum);
		}
		if(voteMsg.equals("commit")){
	        voteCommitArray[nodeNum] = true; 
	        if(nodeNum==1) {
	        	TimeUnit.SECONDS.sleep(10);
	        	if(allVoted(voteCommitArray)){
	        		setFalse(voteCommitArray);
	        		broadcastCommit();
//		            System.out.println("Sending commit message to node - " + nodeNum);
//			        nodes[nodeNum].reqCommit();
//			        callActionRunner(nodes[nodeNum], nodeNum);
		        }
		        else{
		        	setFalse(voteCommitArray);
		        	broadcastAbort();
//			        System.out.println("Sending abort message to node - " + nodeNum);
//			        nodes[nodeNum].reqAbort();
//			        callActionRunner(nodes[nodeNum], nodeNum);
			    }
	        }
	    }
	    else{
	    	setFalse(voteCommitArray);
	    	broadcastAbort();
	    }
	}

	@Override
	public void addNode(NodeInterface node, int nodeNum) throws RemoteException {
		log = "Node " + nodeNum + " is connected";
		System.out.println(log);
        this.nodes[nodeNum] = node;
        logRecord(log);
        readAction(nodeNum);
		
	}
	
	public static void main(String[] args) {
		try {
			Server obj = new Server();
            CoordinatorInterface stub = (CoordinatorInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(5001);
            registry.bind("CoordinatorInterface", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
	}
	
	private synchronized void createThread(String action, NodeInterface tempnode, int tempNodeNum){
	    final NodeInterface node = tempnode; 
	    final int nodeNum = tempNodeNum;
	    if(action.equals("1")){ 
	        (new Thread(){ public synchronized void run(){
	            try { 
	            	node.reqPrepare();
	            	}
	            catch (Exception e) {
	            }
	        }}).start();
	    }
	    if(action.equals("2")){ 
	        (new Thread(){ public synchronized void run(){
	            try { 
	            	node.reqPrepareAbort();
	            	}
	            catch (Exception e) {
	            }
	        }}).start();
	    }
	    if(action.equals("3")){ 
	    	String log = "Node failure scenario, response is not recieved from Node 0. Assumes abort, Coordinator sends abort message";
	    	logRecord(log);
	    	System.out.println(log);
	        (new Thread(){ public synchronized void run(){
	            try { 
	            	node.reqPrepareCommitAbort();
	            	}
	            catch (Exception e) {
	            }
	        }}).start();
	    }
	    if(action.equals("4")){ 
	    	String log = "Node failure scenario, response will be delayed from Node 0. Response timeout and Coordinator sends abort message";
	    	logRecord(log);
	    	System.out.println(log);
	        (new Thread(){ public synchronized void run(){
	            try { 
	            	node.reqPrepare_Timeout();
	            	}
	            catch (Exception e) {
	            }
	        }}).start();
	    }
	    
	    if(action.equals("5")){ 
	    	String log = "Coordinator failure scenario, fails before sending prepare message";
	    	logRecord(log);
	    	System.out.println(log);
	        (new Thread(){ public synchronized void run(){
	            try { 
	            	node.reqTcFailueBeforePrepare();
	            	}
	            catch (Exception e) {
	            }
	        }}).start();
	    }
	    
//	    if(action.equals("5")){ 
//	    	String log = "Coordinator failure scenario, fails before sending prepare message";
//	    	logRecord(log);
//	    	System.out.println(log);
//	        (new Thread(){ public synchronized void run(){
//	            try { 
//	            	node.reqTcFailueBeforePrepare();
//	            	}
//	            catch (Exception e) {
//	            }
//	        }}).start();
//	    }
	    
	    if(action.equals("6")){ 
	    	String log = "Coordinator failure scenario, fails after sending prepare message. Clients respond abort after Coordinator comes online.";
	    	logRecord(log);
	    	System.out.println(log);
	        (new Thread(){ public synchronized void run(){
	            try { 
	            	node.reqTcFailueAfterPrepare();
	            	String log = "Coordinator sent prepare operation, but Nodes respond abort, since TC recovered now.";
	    	    	logRecord(log);
	    	    	System.out.println(log);
	            	TimeUnit.SECONDS.sleep(10);
	            	node.reqPrepareAbortAfterTcRecover();
	            	}
	            catch (Exception e) {
	            }
	        }}).start();
	    }
	    
	    if(action.equals("7")){ 
	    	String log = "Coordinator failure scenario, fails after sending 1 commit. Coordinator recovers, reads log to send commit";
	    	logRecord(log);
	    	System.out.println(log);
	        (new Thread(){ public synchronized void run(){
	            try { 
	            	node.reqPrepare();
	            	}
	            catch (Exception e) {
	            }
	        }}).start();
	    }
}

private void broadcastCommit() throws RemoteException{
    for(int i=0;i<nodes.length;i++){
    	String log = "Sending commit message to node - " + i;
		logRecord(log);
		System.out.println(log);
    	nodes[i].reqCommit();
    	readAction(i);
    }
}
private synchronized void broadcastAbort() throws RemoteException{
    for(int i=0;i<nodes.length;i++){
    	String log = "Sending abort message to node - " + i;
		logRecord(log);
		System.out.println(log);
    	nodes[i].reqAbort();
    	readAction(i);
    }
}
	
	private synchronized boolean allVoted(boolean[] arr){
	    return (arr[0] == true) && (arr[1] == true); 
	}
	private synchronized void setFalse(boolean[] arr){
	    arr[0] = false; arr[1] = false; 
	}
	
	private synchronized String readInput(){ 
	    String input = System.console().readLine("\nType:\n1. BothCommitScenario\r\n"
	    		+ "2. BothAbortScenario\r\n"
	    		+ "3. NodeFailureScenario\r\n"
	    		+ "4. NodeFailureTimeoutScenario\r\n"
	    		+ "5. CoordinatorFailureBeforePrepare\r\n"
	    		+ "6. CoordinatorFailureAfterPrepare\r\n"
	    		+ "7. CoordinatorFailureAfter1Commit\r\n"); 
	    if(input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4")|| input.equals("5") || input.equals("6") || input.equals("7")){
	         return input;
	    }
	    else {
	    	System.out.println("Enter valid input, Press 1 or 2 or 3 or 4 or 5 or 6 or 7");
	    	return readInput();
	    }
	}
	
	private void readAction(int nodeNum) {
		if(nodeNum == 1) {
		action = readInput();
		callCreateThread(action);
		}
		return;
	}
	
	private void callCreateThread(String action) {
		for(int i=0;i<nodes.length;i++){
	        this.createThread(action, nodes[i], i); 
	    }
	}
	
	private void logRecord(String log) {
		try
		{
		    String filename= "coordinator.log";
		    FileWriter fw = new FileWriter(filename,true);
		    fw.write(log);
		    fw.write("\n");
		    fw.close();
		}
		catch(IOException e)
		{
		    System.err.println("IOException: " + e.getMessage());
		}
	}
	
	private  boolean readFile() throws IOException{
		String expected_value = "Node 1 has voted - commit";

			    Path path = Paths.get("coordinator.log");

			    List<String> read = Files.readAllLines(path);
			    for (int i = read.size() - 1; i >= 0; i--) {
					String s = read.get(i);
					s.equals(expected_value);
					return true;
					
				}
				return false;
			}
}
