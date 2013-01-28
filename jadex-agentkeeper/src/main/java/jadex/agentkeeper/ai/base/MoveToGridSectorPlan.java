package jadex.agentkeeper.ai.base;

import jadex.agentkeeper.ai.creatures.orc.OrcBDI;
import jadex.agentkeeper.ai.creatures.orc.OrcBDI.AchieveMoveToSector;
import jadex.agentkeeper.ai.pathfinding.AStarSearch;
import jadex.bdiv3.annotation.GoalTargetCondition;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanCapability;
import jadex.bdiv3.annotation.PlanPlan;
import jadex.bdiv3.annotation.PlanReason;
import jadex.bdiv3.runtime.RPlan;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.extension.envsupport.math.Vector2Double;
import jadex.extension.envsupport.math.Vector2Int;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Move to a Location on the Grid
 * 
 * @author Philip Willuweit p.willuweit@gmx.de
 */
public class MoveToGridSectorPlan
{
	@PlanCapability
	protected OrcBDI				capa;

	@PlanPlan
	protected RPlan					rplan;

	@PlanReason
	protected AchieveMoveToSector	goal;

	private AStarSearch				astar;

	private Iterator<Vector2Int>	path_iterator;

	Vector2Double					myloc;

	// -------- constructors --------

	/**
	 * Create a new plan.
	 */
	public MoveToGridSectorPlan()
	{

		// getLogger().info("Created: "+this);
	}

	// -------- methods --------

	/**
	 * The plan body.
	 */
	@PlanBody
	public IFuture<Void> body()
	{
		final Future<Void> ret = new Future<Void>();
		Vector2Int target = goal.getTarget();
		Vector2Double myloc = capa.getUpdatedPosition();

		// TODO: refractor AStar-Search
		astar = new AStarSearch(myloc, target, capa.getEnvironment(), true);

		if(astar.istErreichbar())
		{
			ArrayList<Vector2Int> path = astar.gibPfadInverted();

			path_iterator = path.iterator();

			moveToNextSector(path_iterator).addResultListener(new DelegationResultListener<Void>(ret));
		}
		else
		{
			ret.setException(new RuntimeException("Not reachable: " + target));
		}


		return ret;
	}


	/**
	 * Iterative Method
	 * @param it iterator
	 * @return empty result when finished
	 */
	private IFuture<Void> moveToNextSector(final Iterator<Vector2Int> it)
	{
		capa.getUpdatedPosition(); // Hack to Update the Belief-Position to
									// Trigger the GoalTargetCondition

		final Future<Void> ret = new Future<Void>();
		if(it.hasNext())
		{
			Vector2Int nextTarget = it.next();

			oneStepToTarget(nextTarget).addResultListener(new DelegationResultListener<Void>(ret)
			{

				public void customResultAvailable(Void result)
				{
					moveToNextSector(path_iterator).addResultListener(new DelegationResultListener<Void>(ret));
				}
			});
		}
		else
		{
			ret.setResult(null);
		}

		return ret;
	}

	/**
	 * We use the MoveTask for the "moving" in the virtual World.
	 * 
	 * @param nextTarget
	 * @return
	 */
	private IFuture<Void> oneStepToTarget(Vector2Int nextTarget)
	{
		final Future<Void> ret = new Future<Void>();
		Map props = new HashMap();
		props.put(MoveTask.PROPERTY_DESTINATION, nextTarget);
		props.put(MoveTask.PROPERTY_SPEED, capa.getMySpeed());

		Object mtaskid = capa.getEnvironment().createObjectTask(MoveTask.PROPERTY_TYPENAME, props, capa.getMySpaceObject().getId());
		capa.getEnvironment().addTaskListener(mtaskid, capa.getMySpaceObject().getId(), new ExceptionDelegationResultListener<Object, Void>(ret)
		{
			public void customResultAvailable(Object result)
			{
				ret.setResult(null);
			}
		});

		return ret;
	}
}
