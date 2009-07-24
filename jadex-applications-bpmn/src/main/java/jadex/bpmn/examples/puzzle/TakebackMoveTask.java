package jadex.bpmn.examples.puzzle;

import jadex.bdi.examples.puzzle.IBoard;
import jadex.bpmn.runtime.IProcessInstance;
import jadex.bpmn.runtime.ITaskContext;
import jadex.bpmn.runtime.task.AbstractTask;

/**
 *  Take back a move on the board.
 */
public class TakebackMoveTask extends AbstractTask
{
	/**
	 *  Execute the task.
	 */
	public void doExecute(ITaskContext context, IProcessInstance instance)
	{
		IBoard	board	= (IBoard)context.getParameterValue("board");
		board.takeback();

//		BpmnExecutor exe = new BpmnExecutor((BpmnInstance) instance, true);
//		ExecutionControlPanel.createBpmnFrame(instance.getModelElement().getName()+": "+context.getModelElement().getName(), (BpmnInstance) instance, exe);	
	}
}
