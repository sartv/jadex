package jadex.base.service.message;

import jadex.commons.future.IFuture;

/**
 * 
 */
public class LocalAbstractConnectionHandler implements IAbstractConnectionHandler
{
	/** The connection. */
	protected AbstractConnection con;
	
	/** The other connection. */
	protected LocalAbstractConnectionHandler conhandler;
	
	/**
	 * 
	 */
	public LocalAbstractConnectionHandler()
	{
	}
	
	/**
	 * 
	 */
	public LocalAbstractConnectionHandler(LocalAbstractConnectionHandler conhandler)
	{
		this.conhandler = conhandler;
	}
	
	//-------- methods called from connection ---------
	
	/**
	 *  Send init message.
	 */
	public IFuture<Void> sendInit()
	{
		getConnectionHandler().initReceived();
		return IFuture.DONE;
	}
	
	/**
	 *  Called from connection.
	 *  Initiates closing procedure (is different for initiator and participant).
	 */
	public IFuture<Void> doClose()
	{
		getConnectionHandler().close();
		if(!getConnection().isClosed())
			getConnection().setClosed();
		return IFuture.DONE;
	}
	
	//-------- methods called from other handler side --------
	
	/**
	 *  Received the init message.
	 */
	public void initReceived()
	{
		con.setInited();
	}
	
	/**
	 *  Close the connection.
	 *  Notifies the other side that the connection has been closed.
	 */
	public void close()
	{
		con.close();
	}
	
	//-------- internal methods --------
	
	/**
	 *  Get the id.
	 *  @return the id.
	 */
	// Can be called savely from any thread, id is immutable
	public int getConnectionId()
	{
		return getConnection().getConnectionId();
	}
	
	/**
	 *  Get the connection.
	 *  @return The connection.
	 */
	protected AbstractConnection getConnection()
	{
		return con;
	}
	
	/**
	 *  Set the connection (needed as connection and handler need each other).
	 *  The connections uses this method to set itself as connection in their constructor.
	 */
	public void setConnection(final AbstractConnection con)
	{
		this.con = con; 
	}
	
	/**
	 *  Get the conhandler.
	 *  @return the conhandler.
	 */
	public LocalAbstractConnectionHandler getConnectionHandler()
	{
		return conhandler;
	}
	
	/**
	 *  Set the connection handler of the other side.
	 */
	public void setConnectionHandler(final LocalAbstractConnectionHandler conhandler)
	{
		this.conhandler = conhandler; 
	}
	
}
