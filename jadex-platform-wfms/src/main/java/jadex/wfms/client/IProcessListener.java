package jadex.wfms.client;

public interface IProcessListener
{
	/**
	 * This method is invoked when a process finishes.
	 * @param event the finished process event
	 */
	public void processFinished(ProcessEvent event);
}
