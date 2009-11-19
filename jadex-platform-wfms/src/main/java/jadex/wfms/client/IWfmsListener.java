package jadex.wfms.client;

public interface IWfmsListener
{
	/**
	 * This method is invoked when a work item is added to the work item queue.
	 * @param event the work item addition event
	 */
	public void workitemAdded(WorkitemQueueChangeEvent event);
	
	/**
	 * This method is invoked when a work item is removed from the work item queue.
	 * @param event the work item removal event
	 */
	public void workitemRemoved(WorkitemQueueChangeEvent event);
	
	/**
	 * This methd is invoked when a process finishes.
	 * @param event the finished process event
	 */
	public void processFinished(ProcessFinishedEvent event);
	
	/**
	 * Returns the client of this listener.
	 * @return the client of this listener
	 */
	public IClient getClient();
}
