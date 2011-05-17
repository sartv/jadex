package jadex.bridge;

import jadex.commons.future.IFuture;

/** 
 *  Notifier service for dynamic kernels.
 *
 */
public interface IMultiKernelNotifierService
{
	/**
	 *  Adds a kernel listener.
	 *  @param listener The listener.
	 *  @return Null, when done.
	 */
	public IFuture addKernelListener(IMultiKernelListener listener);
	
	/**
	 *  Removes a kernel listener.
	 *  @param listener The listener.
	 *  @return Null, when done.
	 */
	public IFuture removeKernelListener(IMultiKernelListener listener);
	
	// TODO: Temporary, until service references become available.
	//public IFuture fireTypesAdded(String[] types);
	//public IFuture fireTypesRemoved(String[] types);
}
