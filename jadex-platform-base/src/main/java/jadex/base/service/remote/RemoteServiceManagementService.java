package jadex.base.service.remote;

import jadex.base.fipa.SFipa;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentManagementService;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IMessageService;
import jadex.bridge.IRemoteServiceManagementService;
import jadex.commons.Future;
import jadex.commons.IFuture;
import jadex.commons.SUtil;
import jadex.commons.concurrent.IResultListener;
import jadex.service.AnyResultSelector;
import jadex.service.BasicService;
import jadex.service.IResultSelector;
import jadex.service.ISearchManager;
import jadex.service.IVisitDecider;
import jadex.service.SServiceProvider;
import jadex.service.TypeResultSelector;
import jadex.service.library.ILibraryService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *  The remote service management service is responsible for 
 *  handling remote service invocations (similar to RMI).
 */
public class RemoteServiceManagementService extends BasicService implements IRemoteServiceManagementService
{
	//-------- attributes --------
	
	/** The component. */
	protected IExternalAccess component;
	
	/** The waiting futures. */
	protected Map waitingcalls;
	
	//-------- constructors --------
	
	/**
	 *  Create a new remote service management service.
	 */
	public RemoteServiceManagementService(IExternalAccess component)
	{
		super(component.getServiceProvider().getId(), IRemoteServiceManagementService.class, null);

		this.component = component;
		this.waitingcalls = Collections.synchronizedMap(new HashMap());
	}
	
	//-------- methods --------
	
	/**
	 *  Get a service proxies from a remote platform.
	 *  (called from arbitrary components)
	 *  @param cid Component id that is used to start the search.
	 *  @param manager The search manager.
	 *  @param decider The visit decider.
	 *  @param selector The result selector.
	 *  @return Collection or single result (i.e. service proxies). 
	 */
	public IFuture getServiceProxies(final IComponentIdentifier cid, 
		final ISearchManager manager, final IVisitDecider decider, final IResultSelector selector)
	{
		final Future ret = new Future();
		
		SServiceProvider.getService(component.getServiceProvider(), IComponentManagementService.class)
			.addResultListener(component.createResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				IComponentManagementService cms = (IComponentManagementService)result;
				// Hack! create remote rms cid with "rms" assumption.
				IComponentIdentifier rrms = cms.createComponentIdentifier("rms@"+cid.getPlatformName(), false, cid.getAddresses());
		
				String callid = SUtil.createUniqueId(component.getComponentIdentifier().getLocalName());
				waitingcalls.put(callid, ret);
		
				RemoteSearchCommand content = new RemoteSearchCommand(cid, manager, 
					decider, selector, callid);
				
				final Map msg = new HashMap();
				msg.put(SFipa.SENDER, component.getComponentIdentifier());
				msg.put(SFipa.RECEIVERS, new IComponentIdentifier[]{rrms});
				msg.put(SFipa.CONVERSATION_ID, callid);
				msg.put(SFipa.LANGUAGE, SFipa.JADEX_XML);
				msg.put(SFipa.CONTENT, content);
			
				SServiceProvider.getService(component.getServiceProvider(), ILibraryService.class)
					.addResultListener(component.createResultListener(new IResultListener()
				{
					public void resultAvailable(Object source, Object result)
					{
						final ILibraryService ls = (ILibraryService)result;
						
						SServiceProvider.getService(component.getServiceProvider(), IMessageService.class)
							.addResultListener(component.createResultListener(new IResultListener()
						{
							public void resultAvailable(Object source, Object result)
							{
								IMessageService ms = (IMessageService)result; 
								ms.sendMessage(msg, SFipa.FIPA_MESSAGE_TYPE, component.getComponentIdentifier(), ls.getClassLoader());
							}
							
							public void exceptionOccurred(Object source, Exception exception)
							{
								ret.setException(exception);
							}
						}));
					}
					public void exceptionOccurred(Object source, Exception exception)
					{
						ret.setException(exception);
					}
				}));
			}
			public void exceptionOccurred(Object source, Exception exception)
			{
				ret.setException(exception);
			}
		}));
		
		return ret;
	}
	
	/**
	 *  Get a service proxy from a remote platform.
	 *  (called from arbitrary components)
	 *  @param platform The component id of the remote platform.
	 *  @param providerid Optional component id that is used to start the search.
	 *  @param service The service type.
	 *  @return The service proxy.
	 */
	public IFuture getServiceProxy(IComponentIdentifier cid, final Class service)
	{
		return getServiceProxies(cid, SServiceProvider.sequentialmanager, SServiceProvider.abortdecider, 
			new TypeResultSelector(service, true));
	}
	
	/**
	 *  Get all service proxies from a remote platform.
	 *  (called from arbitrary components)
	 *  @param platform The component id of the remote platform.
	 *  @param providerid Optional component id that is used to start the search.
	 *  @param service The service type.
	 *  @return The service proxy.
	 */
	public IFuture getServiceProxies(IComponentIdentifier cid, final Class service)
	{
		return getServiceProxies(cid, SServiceProvider.sequentialmanager, SServiceProvider.contdecider, 
			new TypeResultSelector(service, true));
	}
	
	/**
	 *  Get all declared service proxies from a remote component.
	 *  (called from arbitrary components)
	 *  @param cid The remote provider id.
	 *  @param service The service type.
	 *  @return The service proxy.
	 */
	public IFuture getDeclaredServiceProxies(IComponentIdentifier cid)
	{
		return getServiceProxies(cid, SServiceProvider.localmanager, SServiceProvider.contdecider, 
			new AnyResultSelector(false, true));
	}
	
	/**
	 *  Get an external access proxy from a remote component.
	 *  (called from arbitrary components)
	 *  @param cid Component target id.
	 *  @return External access of remote component. 
	 */
	public IFuture getExternalAccessProxy(final IComponentIdentifier cid, final Class targetclass)
	{
		final Future ret = new Future();
		
		SServiceProvider.getService(component.getServiceProvider(), IComponentManagementService.class)
			.addResultListener(component.createResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				IComponentManagementService cms = (IComponentManagementService)result;
				// Hack! create remote rms cid with "rms" assumption.
				IComponentIdentifier rrms = cms.createComponentIdentifier("rms@"+cid.getPlatformName(), false, cid.getAddresses());
		
				String callid = SUtil.createUniqueId(component.getComponentIdentifier().getLocalName());
				waitingcalls.put(callid, ret);
		
				RemoteGetExternalAccessCommand content = new RemoteGetExternalAccessCommand(cid, targetclass, callid);
				
				final Map msg = new HashMap();
				msg.put(SFipa.SENDER, component.getComponentIdentifier());
				msg.put(SFipa.RECEIVERS, new IComponentIdentifier[]{rrms});
				msg.put(SFipa.CONVERSATION_ID, callid);
				msg.put(SFipa.LANGUAGE, SFipa.JADEX_XML);
				msg.put(SFipa.CONTENT, content);
			
				SServiceProvider.getService(component.getServiceProvider(), ILibraryService.class)
					.addResultListener(component.createResultListener(new IResultListener()
				{
					public void resultAvailable(Object source, Object result)
					{
						final ILibraryService ls = (ILibraryService)result;
						
						SServiceProvider.getService(component.getServiceProvider(), IMessageService.class)
							.addResultListener(component.createResultListener(new IResultListener()
						{
							public void resultAvailable(Object source, Object result)
							{
								IMessageService ms = (IMessageService)result; 
								ms.sendMessage(msg, SFipa.FIPA_MESSAGE_TYPE, component.getComponentIdentifier(), ls.getClassLoader());
							}
							
							public void exceptionOccurred(Object source, Exception exception)
							{
								ret.setException(exception);
							}
						}));
					}
					public void exceptionOccurred(Object source, Exception exception)
					{
						ret.setException(exception);
					}
				}));
			}
			public void exceptionOccurred(Object source, Exception exception)
			{
				ret.setException(exception);
			}
		}));
		
		return ret;
	}

	
	/**
	 *  Get the component.
	 *  @return the component.
	 */
	public IExternalAccess getComponent()
	{
		return component;
	}
	
	/**
	 *  Get the waiting calls.
	 *  @return the waiting calls.
	 */
	public Map getWaitingCalls()
	{
		return waitingcalls;
	}
}

