package jadex.base.service.message.transport.httprelaymtp;

import jadex.base.service.message.transport.MessageEnvelope;
import jadex.base.service.message.transport.codecs.CodecFactory;
import jadex.base.service.message.transport.codecs.ICodec;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.message.IMessageService;
import jadex.commons.SUtil;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.xml.bean.JavaWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *  The receiver connects to the relay server
 *  and accepts messages.
 */
public class HttpReceiver
{
	//-------- attributes --------
	
	/** The receiver thread. */
	protected Thread	thread;
	IExternalAccess access;
	CodecFactory codecfac;
	String address;
	
	//-------- constructors --------
	
	/**
	 *  Create and start a new receiver.
	 */
	public HttpReceiver(IExternalAccess access_, CodecFactory codecfac_, String address_)
	{
		this.access	= access_;
		this.codecfac	= codecfac_;
		this.address	= address_;
		thread	= new Thread(new Runnable()
		{
			public void run()
			{
				long	lasttry	= 0;
				while(true)
				{
					try
					{
						// When last connection attempt was less than a second ago, wait some time.
						if(lasttry!=0 && System.currentTimeMillis()-lasttry<1000)
						{
							Thread.sleep(30000);
						}
						
						lasttry	= System.currentTimeMillis();
						String	xmlid	= JavaWriter.objectToXML(access.getComponentIdentifier().getRoot(), getClass().getClassLoader());
						URL	url	= new URL(address+"?id="+URLEncoder.encode(xmlid, "UTF-8"));
//						System.out.println("Connecting to: "+url);
						URLConnection	con	= url.openConnection();
						con.setUseCaches(false);
						InputStream	in	= con.getInputStream();
						while(true)
						{
							// Calculate message size by reading the first 4 bytes
							// Read here is always a blocking call.
							int msg_size;
							int	b	= in.read();
							if(b==-1) 
								throw new IOException("Stream closed");
							byte[] codec_ids = new byte[b];
							for(int i=0; i<codec_ids.length; i++)
							{
								b	= in.read();
								if(b==-1) 
									throw new IOException("Stream closed");
								codec_ids[i] = (byte)b;
							}
							
							byte[] asize = new byte[4];
							for(int i=0; i<asize.length; i++)
							{
								b	= in.read();
								if(b==-1) 
									throw new IOException("Stream closed");
								asize[i] = (byte)b;
							}
							
							msg_size = SUtil.bytesToInt(asize);
//							System.out.println("reclen: "+msg_size);
							msg_size = msg_size-4-codec_ids.length-1; // Remove prolog.
							if(msg_size>0)
							{
								byte[] rawmsg = new byte[msg_size];
								int count = 0;
								while(count<msg_size) 
								{
									int bytes_read = in.read(rawmsg, count, msg_size-count);
									if(bytes_read==-1) 
										throw new IOException("Stream closed");
									count += bytes_read;
								}
								
								Object tmp = rawmsg;
								for(int i=codec_ids.length-1; i>-1; i--)
								{
									ICodec dec = codecfac.getCodec(codec_ids[i]);
									tmp = dec.decode((byte[])tmp, getClass().getClassLoader());
								}
								final MessageEnvelope	msg = (MessageEnvelope)tmp;
								
								access.scheduleStep(new IComponentStep<Void>()
								{
									public IFuture<Void> execute(final IInternalAccess ia)
									{
										ia.getServiceContainer().searchService(IMessageService.class, RequiredServiceInfo.SCOPE_PLATFORM)
											.addResultListener(new IResultListener<IMessageService>()
										{
											public void resultAvailable(IMessageService ms)
											{
												try
												{
													ms.deliverMessage(msg.getMessage(), msg.getTypeName(), msg.getReceivers());
												}
												catch(Exception e)
												{
													ia.getLogger().warning("Exception when delivering message: "+e+", "+msg);													
												}
											}
											
											public void exceptionOccurred(Exception e)
											{
												ia.getLogger().warning("Exception when delivering message: "+e+", "+msg);
											}
										});
										return IFuture.DONE;
									}
								});
							}
						}
					}
					catch(final Exception e)
					{
						access.scheduleStep(new IComponentStep<Void>()
						{
							public IFuture<Void> execute(IInternalAccess ia)
							{
								ia.getLogger().warning("Exception in HTTP releay receiver thread causing reconnect: "+e);
								return IFuture.DONE;
							}
						});
					}
				}
			}
		});
		thread.start();
	}
	
	//-------- methods --------
	
	/**
	 *  Stop the receiver.
	 */
	public void	stop()
	{
		// Hack!!! using NIO, one could use thread.interrupt()
		thread.stop();
		access	= null;
		codecfac	= null;
		address	= null;
		thread	= null;
	}
}
