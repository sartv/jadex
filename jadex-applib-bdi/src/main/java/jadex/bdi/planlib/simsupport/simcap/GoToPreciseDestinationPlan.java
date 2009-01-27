package jadex.bdi.planlib.simsupport.simcap;

import jadex.bdi.planlib.simsupport.common.math.IVector1;
import jadex.bdi.planlib.simsupport.common.math.IVector2;
import jadex.bdi.planlib.simsupport.environment.SimulationEvent;
import jadex.bdi.planlib.simsupport.environment.simobject.task.GoToDestinationTask;
import jadex.bdi.planlib.simsupport.environment.simobject.task.GoToPreciseDestinationTask;
import jadex.bdi.planlib.simsupport.environment.simobject.task.ISimObjectTask;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IInternalEvent;
import jadex.bdi.runtime.Plan;

/** Plan setting the velocity (direction and speed) of the object.
 */
public class GoToPreciseDestinationPlan extends Plan
{
	/** Sets the new velocity of the object.
	 */
	public void body()
	{
		Integer objectId = (Integer) getParameter("object_id").getValue();
		IVector2 destination = (IVector2) getParameter("destination").getValue();
		IVector1 speed = (IVector1) getParameter("speed").getValue();
		
		IGoal removeTask = createGoal("sim_remove_task");
		removeTask.getParameter("object_id").setValue(objectId);
		removeTask.getParameter("task_name").setValue(GoToPreciseDestinationTask.DEFAULT_NAME);
		dispatchSubgoalAndWait(removeTask);
		
		ISimObjectTask task = new GoToPreciseDestinationTask(destination, speed);
		
		IGoal setTask = createGoal("sim_add_task");
		setTask.getParameter("object_id").setValue(objectId);
		setTask.getParameter("task").setValue(task);
		dispatchSubgoalAndWait(setTask);
		IInternalEvent evt = null;
		do
		{
			evt = waitForInternalEvent("simulation_event");
		}
		while (!evt.getParameter("type").getValue().equals(SimulationEvent.GO_TO_PRECISE_DESTINATION_REACHED));
		IVector2 position = (IVector2) evt.getParameter("position").getValue();
		if (!position.equals(destination))
		{
			fail();
		}
	}
}
