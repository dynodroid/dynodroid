package edu.gatech.dynodroid.RMIHelper;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface RMIBackEndServer extends Remote {
	public UUID submitRequest(String filePath,String emailID,String fileServer,String requestType,int noOfEvents) throws RemoteException;
}
