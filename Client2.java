import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;

public class Client2 implements NodeInterface{
	private CoordinatorInterface coordinator;
	private static int nodeNum = 1;
	
	public static void main(String[] args) {
		try {
			Client2 node = new Client2();
	    	NodeInterface stub = (NodeInterface) UnicastRemoteObject.exportObject(node, 0);
	        Registry registry = LocateRegistry.getRegistry(5001);
	        registry.rebind("node1", registry);
	        node.coordinator = (CoordinatorInterface) registry.lookup("CoordinatorInterface");
	        node.coordinator.addNode(node, nodeNum);
    } catch (Exception e) {
        System.err.println("Client exception: " + e.toString());
        e.printStackTrace();
    }
	}

	@Override
	public void reqPrepare() throws RemoteException {
		String log = "Prepare operation";
		logRecord(log);
		System.out.println(log);
		String vote = "commit";
        try{
        	log = this.nodeNum + " has voted as : " + vote;
    		logRecord(log);
    		System.out.println(log);
            coordinator.recVote(vote, this.nodeNum);
            return;
        }
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}
	
	@Override
	public void reqPrepareCommitAbort() throws RemoteException {
		String vote = "commit";
        try{
        	String log = this.nodeNum + " has voted as : " + vote;
    		logRecord(log);
    		System.out.println(log);
            coordinator.recVote(vote, this.nodeNum);
            return;
        }
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}

	@Override
	public void reqAbort() throws RemoteException {
		String log = "Abort operation";
		logRecord(log);
		System.out.println(log);
	}

	@Override
	public void reqCommit() throws RemoteException {
		String log = "Commit operation";
		logRecord(log);
		System.out.println(log);
	}
	
	private synchronized String readVoteInput(){
        String input = System.console().readLine("\tPleaseVote: commit OR abort"); 
        if(input.equals("abort")){ return "abort"; }
        else if(input.equals("commit")){return "commit";}
        else{
            System.out.println("incorrectInput Vote again"); 
            return this.readVoteInput();
        }
    }

	@Override
	public void reqPrepare_Timeout() throws RemoteException, InterruptedException {
		String log = "Prepare operation";
		logRecord(log);
		System.out.println(log);
		String vote = "commit";
        try{
        	log = this.nodeNum + " has voted as : " + vote;
    		logRecord(log);
    		System.out.println(log);
            coordinator.recVote(vote, this.nodeNum);
            return;
        }
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
		
	}
	
	@Override
	public void reqPrepareAbort() throws RemoteException {
		String log = "Prepare operation";
		logRecord(log);
		System.out.println(log);
		String vote = "abort";
        try{
        	log = this.nodeNum + " has voted as : " + vote;
    		logRecord(log);
    		System.out.println(log);
            coordinator.recVote(vote, this.nodeNum);
            return;
        }
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}

	@Override
	public void reqTcFailueBeforePrepare() throws RemoteException, InterruptedException {
		TimeUnit.SECONDS.sleep(5);
		String log = "Coordinator failure before sending prepare message. Timeout and heading for abort operation";
		logRecord(log);
		System.out.println(log);
		reqAbort();
		String vote = "commitTcFailureBefore";
        try{
            coordinator.recVote(vote, this.nodeNum);
            return;
        }
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}

	@Override
	public void reqTcFailueAfterPrepare() throws RemoteException, InterruptedException {
		String log = "Coordinator failure after sending prepare message.";
		logRecord(log);
		System.out.println(log);
		log = "Sending commit, but Coordinator not recieved";
		logRecord(log);
		System.out.println(log);
	}
	
	@Override
	public void reqPrepareAbortAfterTcRecover() throws RemoteException {
		String log = "Prepare operation. Coordinator recovers and sends prepare operation. But Node responds abort";
		logRecord(log);
		System.out.println(log);
		String vote = "abort";
        try{
        	log = this.nodeNum + " has voted as : " + vote;
    		logRecord(log);
    		System.out.println(log);
            coordinator.recVote(vote, this.nodeNum);
            return;
        }
        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}
	
	private void logRecord(String log) {
		try
		{
		    String filename= "client1.log";
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

}
