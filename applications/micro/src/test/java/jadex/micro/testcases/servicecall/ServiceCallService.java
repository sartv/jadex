package jadex.micro.testcases.servicecall;

import jadex.bridge.service.annotation.Service;
import jadex.commons.future.IFuture;

/**
 *  Implementation of a service.
 */
@Service
public class ServiceCallService	implements IServiceCallService
{
	/**
	 *  Dummy method for service call benchmark.
	 */
	public IFuture<Void> call()
	{
		return IFuture.DONE;
	}
	
	/**
	 *  Dummy method for service call benchmark.
	 */
	public IFuture<Void> rawcall()
	{
		return IFuture.DONE;
	}
}