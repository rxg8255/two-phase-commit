import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CoordinatorInterface extends Remote {
	void recVote(String voteMsg, int nodeNum) throws RemoteException, InterruptedException;
	void addNode(NodeInterface node, int nodeNum) throws RemoteException;
	
}
