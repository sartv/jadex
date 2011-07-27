package jadex.base.service.awareness.discovery.registry;

import jadex.base.service.awareness.AwarenessInfo;
import jadex.base.service.awareness.discovery.DiscoveryAgent;
import jadex.base.service.awareness.discovery.DiscoveryEntry;
import jadex.base.service.awareness.discovery.DiscoveryState;
import jadex.base.service.awareness.discovery.MasterSlaveSendHandler;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.SUtil;
import jadex.xml.annotation.XMLClassname;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 *  Handle sending.
 */
class RegistrySendHandler extends MasterSlaveSendHandler
{	
	/**
	 *  Create a new lease time handling object.
	 */
	public RegistrySendHandler(DiscoveryAgent agent)
	{
		super(agent);
	}
	
	/**
	 *  Start sending awareness infos.
	 *  (Ends automatically when a new send behaviour is started).
	 */
	public void startSendBehavior()
	{
		if(getAgent().isStarted())
		{
			final String sendid = SUtil.createUniqueId(getAgent().getMicroAgent()
				.getComponentIdentifier().getLocalName());
			this.sendid = sendid;	
			
			getAgent().getMicroAgent().scheduleStep(new IComponentStep()
			{
				@XMLClassname("send")
				public Object execute(IInternalAccess ia)
				{
					if(!getAgent().isKilled() && sendid.equals(getSendId()))
					{
//						System.out.println(System.currentTimeMillis()+" sending: "+getComponentIdentifier());
						send(createAwarenessInfo());
						
						// Additionally send all knowns to all other masters and local (not remote) slaves
						if(getAgent().isRegistry())
						{
							DiscoveryEntry[] rems = getAgent().getRemotes().getEntries();
							for(int i=0; i<rems.length; i++)
							{
								send(rems[i].getInfo());
							}
							DiscoveryEntry[] locs = getAgent().getLocals().getEntries();
							for(int i=0; i<locs.length; i++)
							{
								send(locs[i].getInfo());
							}
						}
						
						if(getAgent().getDelay()>0)
							getAgent().doWaitFor(getAgent().getDelay(), this);
					}
					return null;
				}
			});
		}
	}
	
//	/**
//	 *  Method to send messages.
//	 */
//	public void send(AwarenessInfo info)
//	{
//		try
//		{
//			byte[] data = DiscoveryState.encodeObject(info, getAgent().getMicroAgent().getModel().getClassLoader());
//	
////			System.out.println("packet size: "+data.length);
//
//			// Send always to registry.
//			if(getAgent().isRegistry())
//			{
//				sendToKnowns(data);
//			}
//			else
//			{
//				sendToRegistry(data);
//			}
//			
//	//		System.out.println("sent: "+address);
//	//		System.out.println(getComponentIdentifier()+" sent '"+info+"' ("+data.length+" bytes)");
//		}
//		catch(Exception e)
//		{
//			getAgent().getMicroAgent().getLogger().warning("Could not send awareness message: "+e);
////			e.printStackTrace();
//		}	
//	}
	
	/**
	 *  Method to send messages.
	 */
	public void send(AwarenessInfo info)
	{
		try
		{
			byte[] data = DiscoveryState.encodeObject(info, agent.getMicroAgent().getModel().getClassLoader());
	
//			System.out.println("packet size: "+data.length);

			sendToDiscover(data);
			
			if(getAgent().isRegistry())
			{
				// Distribute to all remote and local platforms.
				sendToRemotes(data);
				sendToLocals(data);
			}
			else if(getAgent().isMaster())
			{
				// As master always send my info to registry.
				sendToRegistry(data);
				// Send to all locals a refresh awareness
//				sendToLocals(data);
			}
			else
			{
				// As slave always send my info to local master.
				sendToMaster(data);
			}
			
//			System.out.println("sent");
//			System.out.println(getComponentIdentifier()+" sent '"+info+"' ("+data.length+" bytes)");
		}
		catch(Exception e)
		{
			agent.getMicroAgent().getLogger().warning("Could not send awareness message: "+e);
//			e.printStackTrace();
		}	
	}
	
	/**
	 *  Send/forward to discover.
	 *  @param data The data to be send.
	 */
	public int sendToDiscover(byte[] data, int maxsend)
	{
		// No discovery.
		return 0;
	}
	
	/**
	 *  Get the agent.
	 */
	protected RegistryDiscoveryAgent getAgent()
	{
		return (RegistryDiscoveryAgent)agent;
	}
	
	/**
	 *  Send to registry.
	 */
	public void sendToRegistry(byte[] data)
	{
		System.out.println("sent to reg: "+getAgent().getAddress()+" "+getAgent().getPort());
		send(data, getAgent().getAddress(), getAgent().getPort());
	}
	
	/**
	 *  Send to registry.
	 */
	public void sendToMaster(byte[] data)
	{
//		System.out.println("sent to reg: "+address+" "+port);
		send(data, SUtil.getInet4Address(), getAgent().getPort());
	}
	
//	/**
//	 *  Send info to all knowns.
//	 *  @param data The data to be send.
//	 */
//	protected void sendToKnowns(byte[] data)
//	{
//		DiscoveryEntry[] rems = getAgent().getKnowns().getEntries();
//		for(int i=0; i<rems.length; i++)
//		{
//			InetSocketAddress isa = (InetSocketAddress)rems[i].getEntry();
//			send(data, isa.getAddress(), isa.getPort());
//		}
////		System.out.println("sent to knwons: "+rems.length);
//	}
	
	/**
	 *  Send a packet.
	 */
	public boolean send(byte[] data, InetAddress address, int port)
	{
//		System.out.println("sent packet: "+address+" "+port);
		boolean ret = true;
		try
		{
			DatagramPacket p = new DatagramPacket(data, data.length, new InetSocketAddress(address, port));
			getAgent().getSocket().send(p);
		}
		catch(Exception e)
		{
			ret = false;
		}
		return ret;
	}
}