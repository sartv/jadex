package jadex.bridge.service.component.interceptors;

import jadex.bridge.service.IInternalService;
import jadex.bridge.service.IService;
import jadex.bridge.service.annotation.ServiceShutdown;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.bridge.service.component.ServiceInfo;
import jadex.bridge.service.component.ServiceInvocationContext;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 *  The resolve interceptor is responsible for determining
 *  the object on which the method invocation is finally performed.
 * 
 *  Checks whether the object is a ServiceInfo. In this case
 *  it delegates method calls of I(Internal)Service to 
 *  the automatically created BasicService instance and all
 *  other calls to the domain object.
 *  
 *  // todo: much annotation stuff and injection of objects to the pojo.
 */
public class ResolveInterceptor extends AbstractApplicableInterceptor
{
	//-------- constants --------
	
	/** The static map of subinterceptors (method -> interceptor). */
	protected static Set SERVICEMETHODS;
	protected static Method START_METHOD;
	protected static Method SHUTDOWN_METHOD;
	
	static
	{
		try
		{
			START_METHOD = IInternalService.class.getMethod("startService", new Class[0]);
			SHUTDOWN_METHOD = IInternalService.class.getMethod("shutdownService", new Class[0]);
			SERVICEMETHODS = new HashSet();
			SERVICEMETHODS.add(IService.class.getMethod("getServiceIdentifier", new Class[0]));
			SERVICEMETHODS.add(IInternalService.class.getMethod("getPropertyMap", new Class[0]));
			SERVICEMETHODS.add(IInternalService.class.getMethod("isValid", new Class[0]));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *  Execute the interceptor.
	 *  @param context The invocation context.
	 */
	public IFuture execute(final ServiceInvocationContext sic)
	{
		final Future ret = new Future();
		
		Object service = sic.getObject();
		if(service instanceof ServiceInfo)
		{
			final ServiceInfo si = (ServiceInfo)service;
			
			if(START_METHOD.equals(sic.getMethod()))
			{
				invokeDoubleMethod(sic, si, START_METHOD, ServiceStart.class).addResultListener(new DelegationResultListener(ret));
			}
			else if(SHUTDOWN_METHOD.equals(sic.getMethod()))
			{
				invokeDoubleMethod(sic, si, SHUTDOWN_METHOD, ServiceShutdown.class).addResultListener(new DelegationResultListener(ret));
			}
			else if(SERVICEMETHODS.contains(sic.getMethod()))
			{
				sic.setObject(si.getManagementService());
				sic.invoke().addResultListener(new DelegationResultListener(ret));
			}
			else
			{
				sic.setObject(si.getDomainService());
				sic.invoke().addResultListener(new DelegationResultListener(ret));
			}
		}
		else
		{
			sic.invoke().addResultListener(new DelegationResultListener(ret));
		}
		
		return ret;
	}
	
	/**
	 *  Invoke double methods.
	 */
	protected IFuture invokeDoubleMethod(final ServiceInvocationContext sic, final ServiceInfo si, Method m, Class annotation)
	{
		// todo: call in which order?
		
		final Future ret = new Future();
		
		final Method origmethod = sic.getMethod();
		Method[] methods = si.getDomainService().getClass().getMethods();
		boolean found = false;
		
		for(int i=0; i<methods.length && !found; i++)
		{
			if(methods[i].isAnnotationPresent(annotation))
			{
				sic.setMethod(methods[i]);
				sic.setObject(si.getDomainService());
				sic.invoke().addResultListener(new DelegationResultListener(ret)
				{
					public void customResultAvailable(Object result)
					{
						sic.setMethod(origmethod);
						sic.setObject(si.getManagementService());
						sic.invoke().addResultListener(new DelegationResultListener(ret));
					}
				});
				found = true;
			}
		}
		
		if(!found)
		{
			sic.setObject(si.getManagementService());
			sic.invoke().addResultListener(new DelegationResultListener(ret));
		}
		
		return ret;
	}
	
}
