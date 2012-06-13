package jadex.base.service.message.transport.tcpmtp;

import jadex.base.service.message.transport.tcpmtp.TCPTransport.Cleaner;
import jadex.commons.SUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *  TCP output connection for sending messages to a specific target address. 
 */
class TCPOutputConnection
{
	//-------- constants --------
	
	/** 5 sec timeout. */
	public static final int	TIMEOUT	= 5000;
	
	//-------- attributes --------
	
	/** The client socket for sending data. */
	protected Socket sock;

	/** The output stream. */
	protected OutputStream sos;
	
	/** The cleaner. */
	protected Cleaner cleaner;
	
	//-------- constructors --------
	
	/**
	 *  Create a new tcp connection for sending data. 
	 */
//	public TCPOutputConnection(InetAddress iaddr, int iport, Cleaner cleaner, Socket sock) throws IOException
	public TCPOutputConnection(Cleaner cleaner, Socket sock) throws IOException
	{
		this.sock = sock;
//		try
//		{
//			System.out.println("TCP Connection: "+iaddr+":"+iport);
//			this.sock = new Socket();
//			sock.connect(new InetSocketAddress(iaddr, iport), TIMEOUT);
//			System.out.println("TCP Connection: "+iaddr+":"+iport+" established");
//		}
//		catch(IOException e)
//		{
//			System.out.println("TCP Connection: "+iaddr+":"+iport+" failed");
////			e.printStackTrace();
//			throw e;
//		}
		this.sos = new BufferedOutputStream(sock.getOutputStream());
		this.cleaner = cleaner;
		//address = SMTransport.SERVICE_SCHEMA+iaddr.getHostAddress()+":"+iport;
	}

	//-------- methods --------
	
	/**
	 *  Send a message.
	 *  @param msg The message.
	 *  (todo: relax synchronization by performing sends 
	 *  on extra sender thread of transport)
	 */
	public synchronized boolean send(byte[] prolog, byte[] data)
	{
		boolean ret = false;
		
		try
		{
			sos.write(SUtil.intToBytes(prolog.length+data.length));
			sos.write(prolog);
			sos.write(data);
			sos.flush();
			ret = true;
			cleaner.refresh();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			close();
		}
		
		return ret;
	}
	
	/**
	 *  Test if the connection is closed.
	 *  @return True, if closed.
	 */
	public boolean isClosed()
	{
		return sock.isClosed();
	}
	
	/**
	 *  Close the connection.
	 */
	public void close()
	{
		try
		{
			sock.close();
		}
		catch(IOException e)
		{
			//e.printStackTrace();
		}
		cleaner.remove();
	}
}
