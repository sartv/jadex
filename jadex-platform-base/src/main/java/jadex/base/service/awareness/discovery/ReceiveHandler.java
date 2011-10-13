package jadex.base.service.awareness.discovery;

import jadex.base.service.awareness.AwarenessInfo;
import jadex.base.service.awareness.management.IManagementService;
import jadex.bridge.ComponentTerminatedException;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.threadpool.IThreadPoolService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;

import java.net.InetAddress;

/**
 * 
 */
public abstract class ReceiveHandler
{
	//-------- attributes --------
	
	/** The agent. */
	protected DiscoveryAgent agent;

	/** The current send id. */
	protected String sendid;
	
	/** Flag indicating that the agent has received its own discovery info. */
	protected boolean received_self;
	
	//-------- constructors --------
	
	/**
	 *  Create a new lease time handling object.
	 */
	public ReceiveHandler(DiscoveryAgent agent)
	{
		this.agent = agent;
	}
	
	//-------- methods --------
	
	/**
	 *  Receive a packet.
	 */
	public abstract Object[] receive();
	
	/**
	 *  Start receiving awareness infos.
	 *  @return A future indicating when the receiver thread is ready.
	 */
	public IFuture	startReceiving()
	{
		final Future	ret	= new Future();
		
		// Start the receiver thread.
		agent.getMicroAgent().getServiceContainer().getRequiredService("threadpool")
			.addResultListener(new IResultListener()
		{
			public void resultAvailable(Object result)
			{
				final IThreadPoolService tp = (IThreadPoolService)result;
				
				tp.execute(new Runnable()
				{
					public void run()
					{						
						try
						{
							// Init receive socket
							try
							{
								agent.initNetworkRessource();
								ret.setResultIfUndone(null);
							}
							catch(Exception e)
							{
								ret.setExceptionIfUndone(e);
							}
						
							while(!agent.isKilled())
							{
								try
								{
//									final DatagramPacket pack = new DatagramPacket(buf, buf.length);
									final Object[] packet = receive();
									if(packet!=null)
									{
										agent.getMicroAgent().scheduleStep(new IComponentStep<Void>()
										{
											public IFuture<Void> execute(IInternalAccess ia)
											{
												AwarenessInfo info = (AwarenessInfo)DiscoveryState.decodeObject((byte[])packet[2], agent.getMicroAgent().getClassLoader());
//												System.out.println("received info: "+info);
												handleReceivedPacket((InetAddress)packet[0], ((Integer)packet[1]).intValue(), (byte[])packet[2], info);
												return IFuture.DONE;
											}
										});
									}
	//								System.out.println("received: "+getComponentIdentifier());
								}
								catch(Exception e)
								{
									// Can happen if is slave and master goes down.
									// In that case it tries to find new master.
	//								getLogger().warning("Receiving awareness info error: "+e);
									ret.setExceptionIfUndone(e);
								}
							}
						}
						catch(Exception e) 
						{
							ret.setExceptionIfUndone(e);
						}
//						System.out.println("comp and receiver terminated: "+getComponentIdentifier());
					}
				});
			}
			
			public void exceptionOccurred(Exception exception)
			{
				if(!(exception instanceof ComponentTerminatedException))
					agent.getMicroAgent().getLogger().warning("Awareness agent problem, could not get threadpool service: "+exception);
//				exception.printStackTrace();
				ret.setExceptionIfUndone(exception);
			}
		});
		
		return ret;
	}
	
	/**
	 *  Handle a received packet.
	 */
	public void handleReceivedPacket(InetAddress address, int port, byte[] data, AwarenessInfo info)
	{
//		InetAddress address = packet.getAddress();
//		int port = packet.getPort();
//		InetSocketAddress sa = new InetSocketAddress(address, port);
//		System.out.println("received: "+obj+" "+address);
			
		if(info!=null && info.getSender()!=null)
		{
			if(!info.getSender().equals(agent.getRoot()))
			{
				announceAwareness(info);
			}
			else
			{
				received_self	= true;
//				return;
			}
//			System.out.println(System.currentTimeMillis()+" "+getComponentIdentifier()+" received: "+info.getSender());
		}	
//		System.out.println("received awa info: "+getComponentIdentifier().getLocalName()+" "+info.getSender());
	}
		
	/**
	 *  Announce newly arrived awareness info to management service.
	 */
	public void announceAwareness(final AwarenessInfo info)
	{
//		System.out.println("announcing: "+info);
		
		if(info.getSender()!=null)
		{
			if(info.getSender().equals(agent.getRoot()))
				received_self	= true;
			
//			System.out.println(System.currentTimeMillis()+" "+getComponentIdentifier()+" received: "+info.getSender());
			
			agent.getMicroAgent().getRequiredService("management").addResultListener(new DefaultResultListener()
			{
				public void resultAvailable(Object result)
				{
					IManagementService ms = (IManagementService)result;
					ms.addAwarenessInfo(info).addResultListener(new DefaultResultListener()
					{
						public void resultAvailable(Object result)
						{
							boolean initial = ((Boolean)result).booleanValue();
							if(initial && agent.isFast() && agent.isStarted() && !agent.isKilled())
							{
		//						System.out.println(System.currentTimeMillis()+" fast discovery: "+getComponentIdentifier()+", "+sender);
								received_self = false;
								agent.doWaitFor((long)(Math.random()*500), new IComponentStep<Void>()
								{
									int	cnt;
									public IFuture<Void> execute(IInternalAccess ia)
									{
										if(!received_self)
										{
											cnt++;
		//									System.out.println("CSMACD try #"+(++cnt));
											agent.sender.send(agent.createAwarenessInfo());
											agent.doWaitFor((long)(Math.random()*500*cnt), this);
										}
										return IFuture.DONE;
									}
								});
							}
						}
					});
				}
			});
		}
	}

}
