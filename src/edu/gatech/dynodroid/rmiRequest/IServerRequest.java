/**
 * 
 */
package edu.gatech.dynodroid.rmiRequest;


/**
 * @author machiry
 *
 */
public interface IServerRequest {
	public abstract boolean processRequest();
	public abstract void updateStatus(ServerRequestStatus newStatus);	
	public abstract ServerRequestStatus getCurrentStatus();
	public abstract ServerRequestType getRequestType();
}
