package jadex.simulation.analysis.process.optimisation.tasks;

import jadex.bpmn.runtime.BpmnInterpreter;
import jadex.bpmn.runtime.ITaskContext;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.simulation.analysis.process.basicTasks.ATask;
import jadex.simulation.analysis.process.basicTasks.user.AServiceCallUserTaskView;

public class OptimierenStepTask extends ATask
{
	public OptimierenStepTask()
	{
		view = new AServiceCallUserTaskView(this);
		addTaskListener(view);
	}

	@Override
	public IFuture execute(ITaskContext context, BpmnInterpreter instance)
	{
		super.execute(context, instance);
//		IADatenobjekteParametrisierenGUIService service = (IADatenobjekteParametrisierenGUIService) SServiceProvider.getService(instance.getServiceProvider(), IADatenobjekteParametrisierenGUIService.class).get(susThread);
//		UUID session = (UUID) service.createSession(null).get(susThread);
//		// service.getSessionView(session).get(susThread);
//		((AServiceCallUserTaskView) view).addServiceGUI((JComponent) service.getSessionView(session).get(susThread), new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
//
//		IAModel model = (IAModel)service.engineerGuiDataObject(session, AModelFactory.createTestAModel(Modeltype.DesmoJ)).get(susThread);
//		taskChanged(new ATaskEvent(this, context, instance, AConstants.TASK_USER));
//		((AServiceCallUserTaskView)view).startGUI().get(susThread);
//		context.setParameterValue("modell", model);
//		taskChanged(new ATaskEvent(this, context, instance, AConstants.TASK_BEENDET));
		return new Future(model);
	}

//	/**
//	 * Get the meta information about the task.
//	 */
//	public static TaskMetaInfo getMetaInfo()
//	{
//		String desc = "Erzeugt ein IAModell mit Hilfe einer GUI";
//
//		ParameterMetaInfo modelmi = new ParameterMetaInfo(ParameterMetaInfo.DIRECTION_OUT,
//				IAModel.class, "modell", null, "Erzeugtes IAModel");
//
//		return new TaskMetaInfo(desc, new ParameterMetaInfo[] { modelmi });
//	}

}
