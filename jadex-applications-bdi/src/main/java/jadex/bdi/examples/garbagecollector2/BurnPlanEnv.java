package jadex.bdi.examples.garbagecollector2;

import java.util.HashMap;
import java.util.Map;

import jadex.adapter.base.envsupport.environment.IAgentAction;
import jadex.adapter.base.envsupport.environment.IEnvironmentSpace;
import jadex.adapter.base.envsupport.environment.space2d.Space2D;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.bdi.runtime.Plan.SyncResultListener;
import jadex.bridge.IAgentIdentifier;

/**
 *  Burn a piece of garbage.
 */
public class BurnPlanEnv extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
//		System.out.println("Burn plan activated!");
		
		IEnvironmentSpace env = (IEnvironmentSpace)getBeliefbase().getBelief("env").getFact();

		// Pickup the garbarge.
		IGoal pickup = createGoal("pick");
		dispatchSubgoalAndWait(pickup);

		SyncResultListener srl	= new SyncResultListener();
		Map params = new HashMap();
		params.put(IAgentAction.ACTOR_ID, getAgentIdentifier().getLocalName());
		env.performAgentAction("burn", params, srl);
		srl.waitForResult();
	}
}
