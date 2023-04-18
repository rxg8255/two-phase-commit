import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeInterface extends Remote{
	public void reqPrepare() throws RemoteException;
	public void reqAbort() throws RemoteException;
	public void reqCommit() throws RemoteException;
	void reqPrepare_Timeout() throws RemoteException, InterruptedException;
	void reqPrepareAbort() throws RemoteException;
	void reqPrepareAbortAfterTcRecover() throws RemoteException;
	void reqPrepareCommitAbort() throws RemoteException;
	void reqTcFailueBeforePrepare() throws RemoteException, InterruptedException;
	public void reqTcFailueAfterPrepare() throws RemoteException, InterruptedException;
}
