package jadex.bpmn.runtime.handler;

import jadex.bpmn.model.MActivity;
import jadex.bpmn.model.MNamedIdElement;
import jadex.bpmn.model.MSequenceEdge;
import jadex.bpmn.runtime.BpmnInstance;
import jadex.bpmn.runtime.IActivityHandler;
import jadex.bpmn.runtime.ProcessThread;
import jadex.bpmn.runtime.ThreadContext;

import java.util.List;


/**
 *  Default activity handler, which provides some
 *  useful helper methods.
 */
public class DefaultActivityHandler implements IActivityHandler
{	
	/**
	 *  Execute an activity.
	 *  @param activity	The activity to execute.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 */
	public void execute(MActivity activity, BpmnInstance instance, ProcessThread thread)
	{
		doExecute(activity, instance, thread);
		step(activity, instance, thread, null);
	}
	
	/**
	 *  Execute an activity. Empty default implementation.
	 *  @param activity	The activity to execute.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 */
	protected void doExecute(MActivity activity, BpmnInstance instance, ProcessThread thread)
	{
		System.out.println("Executed: "+activity+", "+instance);
	}
	
	/**
	 *  Make a process step, i.e. find the next edge or activity for a just executed thread.
	 *  @param activity	The activity to execute.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 */
	public void step(MActivity activity, BpmnInstance instance, ProcessThread thread, Object event)
	{
		MNamedIdElement	next	= null;
		Exception	ex	= thread.getException();
		
		// Find next element and context(s) to be removed.
		boolean	outside	= false;
		ThreadContext	remove	= null;	// Context that needs to be removed (if any).
		while(next==null && !outside)
		{
			// Normal flow
			if(ex==null)
			{
				List	outgoing	= activity.getOutgoingSequenceEdges();
				if(outgoing!=null && outgoing.size()==1)
				{
					next	= (MSequenceEdge)outgoing.get(0);
				}
				else if(outgoing!=null && outgoing.size()>1)
				{
					throw new UnsupportedOperationException("Activity has more than one one outgoing edge. Please overridge step() for disambiguation: "+activity);
				}
				// else no outgoing edge -> check parent context, if any.
			}
		
			// Exception flow.
			else
			{
				List	handlers	= activity.getEventHandlers();
				for(int i=0; handlers!=null && next==null && i<handlers.size(); i++)
				{
					MActivity	handler	= (MActivity) handlers.get(i);
					if(handler.getActivityType().equals("EventIntermediateError"))
					{
						// Todo: match exception types.
//						Class	clazz	= handler.getName()!=null ? SReflect.findClass0(clname, imports, classloader);
						next	= handler;
					}
				}
			}
			
			ThreadContext	context	= thread.getThreadContext();
			outside	= context.getParent()==null;
			if(next==null && !outside)
			{
				// When last thread or exception, mark current context for removal.
				if(context.getThreads().size()==1 || ex!=null)
				{
					activity	= (MActivity)context.getModelElement();
					remove	= context;
					context	= context.getParent();
				}
				
				// If more threads are available in current context just exit loop.
				else if(context.getThreads().size()>1)
				{
					outside	= true;
				}
			}
		}

		// Remove inner context(s), if any.
		if(remove!=null)
		{
			thread	= remove.getInitiator();
			thread.setNonWaiting();
			// Todo: Callbacks for aborted threads (to abort external activities)
			thread.getThreadContext().removeSubcontext(remove);
		}

		if(next!=null)
		{

			// Todo: store exception as parameter!?
			if(ex!=null)
				thread.setException(null);
		}
		else if(ex!=null)
		{
			throw new RuntimeException("Unhandled exception in process: "+activity, ex);
		}
		
		// Perform step settings, i.e. set next edge/activity or remove thread.
		if(next instanceof MSequenceEdge)
		{
			thread.setLastEdge((MSequenceEdge)next);
		}
		else if(next instanceof MActivity)
		{
			thread.setNextActivity((MActivity)next);
		}
		else if(next==null)
		{
			thread.getThreadContext().removeThread(thread);
		} 
		else
		{
			throw new UnsupportedOperationException("Unknown outgoing element type: "+next);
		}
	}
	
	/**
	 *  Method that should be called, when an activity is finished and the following activity should be scheduled.
	 *  @param activity	The timing event activity.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 */
	public void	notify(MActivity activity, BpmnInstance instance, ProcessThread thread, Object event)
	{
		step(activity, instance, thread, event);
		thread.setNonWaiting();
		instance.wakeUp();
	}
}
