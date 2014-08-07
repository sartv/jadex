package jadex.bridge.sensor.service;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.BasicServiceInvocationHandler;
import jadex.bridge.service.component.ServiceInvocationContext;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.MethodInfo;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 *  Property for the waiting time of a method or a service as a whole.
 */
public class WaitingTimeProperty extends TimedProperty
{
	/** The name of the property. */
	public static final String NAME = "waiting time";
	
	/** The service identifier. */
	protected IServiceIdentifier sid;
	
	/** The listener. */
	protected IMethodInvocationListener listener;
	
	/** The method info. */
	protected MethodInfo method;
	
	/** The clock. */
	protected IClockService clock;
	
	/**
	 *  Create a new property.
	 */
	public WaitingTimeProperty(final IInternalAccess comp, IService service, MethodInfo method)
	{
		super(NAME, comp, true);
		this.method = method;
		this.sid = service.getServiceIdentifier();
		
		SServiceProvider.getService((IServiceProvider)comp.getServiceContainer(), IClockService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new IResultListener<IClockService>()
		{
			public void resultAvailable(IClockService result)
			{
				WaitingTimeProperty.this.clock = result;
//				System.out.println("assigned clock");
			}
			
			public void exceptionOccurred(Exception exception)
			{
				comp.getLogger().warning("Could not fetch time service in property.");
			}
		});
		
		if(Proxy.isProxyClass(service.getClass()))
		{
			listener = new UserMethodInvocationListener(new IMethodInvocationListener()
			{
				Map<Object, Long> times = new HashMap<Object, Long>();
				
				public void methodCallStarted(Object proxy, Method method, Object[] args, Object callid, ServiceInvocationContext context)
				{
					if(clock!=null)
						times.put(callid, Long.valueOf(clock.getTime()));
				}
				
				public void methodCallFinished(Object proxy, Method method, Object[] args, Object callid, ServiceInvocationContext context)
				{
					if(clock!=null)
					{
						Long start = times.remove(callid);
						// May happen that property is added during ongoing call
						if(start!=null)
						{
							long dur = clock.getTime() - start.longValue();
							setValue(dur);
						}
					}
				}
			});
			comp.getServiceContainer().addMethodInvocationListener(service.getServiceIdentifier(), method, listener);
		}
		else
		{
			throw new RuntimeException("Cannot install waiting time listener hook.");
		}
	}
	
	/**
	 *  Measure the value.
	 */
	public Long measureValue()
	{
		return null;
//		throw new UnsupportedOperationException();
	}
	
	/**
	 *  Set the value.
	 */
	public void setValue(Long value) 
	{
		// ema calculatio: EMAt = EMAt-1 +(SF*(Ct-EMAt-1)) SF=2/(n+1)
		if(this.value!=null && value!=null)
		{
			double sf = 2d/(10d+1); // 10 periods per default
			double delta = value-this.value;
			value = Long.valueOf((long)(this.value+sf*delta));
		}
		
		if(value!=null)
		{
//			System.out.println("Setting value: "+value);
			super.setValue(value);
		}
	}
	
	/**
	 *  Property was removed and should be disposed.
	 */
	public IFuture<Void> dispose()
	{
		comp.getServiceContainer().removeMethodInvocationListener(sid, method, listener);
		return IFuture.DONE;
	}
}
