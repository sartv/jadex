package jadex.base.service.message.transport.httprelaymtp.nio;

import jadex.base.service.message.ISendTask;
import jadex.base.service.message.transport.ITransport;
import jadex.base.service.message.transport.httprelaymtp.SRelay;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.types.message.IMessageService;
import jadex.commons.Tuple2;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Binding;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class HttpRelayTransport implements ITransport
{
	//-------- attributes --------
	
	/** The component. */
	protected IInternalAccess component;
	
	/** The relay server address. */
	protected String	address;
	
	/** The receiver process. */
	protected NIOSelectorThread	selectorthread;
	
	//-------- constructors --------
	
	/**
	 *  Create a new relay transport.
	 */
	public HttpRelayTransport(IInternalAccess component, String address)
	{
		this.component	= component;
		this.address	= address;
		if(!address.startsWith(getServiceSchema()))
			throw new RuntimeException("Address does not match service schema: "+address+", "+getServiceSchema());
	}
	
	//-------- ITransport  interface --------
	
	/**
	 *  Start the transport.
	 */
	public IFuture<Void> start()
	{
		final Future<Void>	ret	= new Future<Void>();
		component.getServiceContainer().searchService(IMessageService.class, Binding.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<IMessageService, Void>(ret)
		{
			public void customResultAvailable(IMessageService ms)
			{
				try
				{
					// Create the selector thread (starts automatically).
					selectorthread	= new NIOSelectorThread(component.getComponentIdentifier().getRoot(), address, ms, component.getLogger(), component.getExternalAccess());
					ret.setResult(null);
				}
				catch(Exception e)
				{
					ret.setException(e);
				}
			}
		});
		return ret;
	}

	/**
	 *  Perform cleanup operations (if any).
	 */
	public IFuture<Void> shutdown()
	{
		// Stop the reciever.
		this.selectorthread.stop();
		return IFuture.DONE;
	}
	
	/**
	 *  Test if a transport is applicable for the message.
	 *  
	 *  @return True, if the transport is applicable for the message.
	 */
	public boolean	isApplicable(ISendTask task)
	{
		boolean	ret	= false;
		for(int i=0; !ret && i<task.getReceivers().length; i++)
		{
			String[]	raddrs	= task.getReceivers()[i].getAddresses();
			for(int j=0; !ret && j<raddrs.length; j++)
			{
				ret	= raddrs[j].toLowerCase().startsWith(getServiceSchema());
			}			
		}
		return ret;
	}

	/**
	 *  Send a message to receivers on the same platform.
	 *  This method is called concurrently for all transports.
	 *  Each transport should immediately announce its interest and try to connect to the target platform
	 *  (or reuse an existing connection) and afterwards acquire the token for the task.
	 *  
	 *  The first transport that acquires the token (i.e. the first connected transport) tries to send the message.
	 *  If sending fails, it may release the token to trigger the other transports.
	 *  
	 *  All transports may keep any established connections open for later messages.
	 *  
	 *  @param task The message to send.
	 *  @return True, if the transport is applicable for the message.
	 */
	public void	sendMessage(final ISendTask task)
	{
		// Fetch all addresses
		Set<String>	addresses	= new LinkedHashSet<String>();
		for(int i=0; i<task.getReceivers().length; i++)
		{
			String[]	raddrs	= task.getReceivers()[i].getAddresses();
			for(int j=0; j<raddrs.length; j++)
			{
				if(raddrs[j].startsWith(getServiceSchema()))
					addresses.add(raddrs[j]);
			}			
		}

		// Iterate over all different addresses and try to send
		for(Iterator<String> it=addresses.iterator(); it.hasNext(); )
		{
			this.selectorthread.addSendTask(task, it.next());
		}
	}
	
	/**
	 *  Returns the prefix of this transport
	 *  @return Transport prefix.
	 */
	public String getServiceSchema()
	{
		return SRelay.ADDRESS_SCHEME;
	}
	
	/**
	 *  Get the addresses of this transport.
	 *  @return An array of strings representing the addresses 
	 *  of this message transport mechanism.
	 */
	public String[] getAddresses()
	{
		return new String[]{address};
	}

	/**
	 *  Parse the address.
	 *  @return Host, port and path.
	 */
	public static Tuple2<Tuple2<String, Integer>, String> parseAddress(String address)
	{
		String	path	= "";
		int port	= 80;
		String host	= address.substring(SRelay.ADDRESS_SCHEME.length());
		if(host.indexOf('/')!=-1)
		{
			path	= host.substring(host.indexOf('/'));
			host	= host.substring(0, host.indexOf('/'));
		}
		if(host.indexOf(':')!=-1)
		{
			port	= Integer.parseInt(host.substring(host.indexOf(':')+1));
			host	= host.substring(0, host.indexOf(':'));			
		}
		Tuple2<String, Integer>	adr	= new Tuple2<String, Integer>(host, new Integer(port));
		Tuple2<Tuple2<String, Integer>, String>	tup	= new Tuple2<Tuple2<String, Integer>, String>(adr, path);
		return tup;
	}
}
