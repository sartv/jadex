package jadex.bdi.examples.cleanerworld_classic.cleaner;

import jadex.bdi.examples.cleanerworld_classic.Location;
import jadex.bdi.examples.cleanerworld_classic.MapPoint;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;

import java.util.List;
import java.util.Random;


/**
 *  Walk to the least visited positions.
 *  Uses a relative measure to go to seldom seen positions.
 */
public class LeastSeenWalkPlan extends Plan
{
	/** Random number generator. */
	protected Random	rnd	= new Random();
	
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public LeastSeenWalkPlan()
	{
//		getLogger().info("Created: "+this+" for goal "+getRootGoal());
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Select randomly one of the least seen locations.
		List	mps = (List)getExpression("query_min_seen").execute();
		MapPoint mp = (MapPoint)mps.get(0);
		int cnt	= 1;
		for( ; cnt<mps.size(); cnt++)
		{
			MapPoint mp2 = (MapPoint)mps.get(cnt);
			if(mp.getSeen()!=mp2.getSeen())
				break;
		}
		mp	= (MapPoint)mps.get(rnd.nextInt(cnt));
//		MapPoint[]	mps = (MapPoint[])getBeliefbase().getBeliefSet("visited_positions").getFacts();
//		MapPoint mp = mps[(int)(Math.random()*mps.length)];

		Location dest = mp.getLocation();
		IGoal moveto = createGoal("achievemoveto");
		moveto.getParameter("location").setValue(dest);
//		System.out.println("Created: "+dest+" "+this);
		dispatchSubgoalAndWait(moveto);
//		System.out.println("Reached: "+dest+" "+this);
	}
	
//	public void aborted()
//	{
//		System.out.println("Aborted: "+this);
//	}
	
//	public void failed()
//	{
//		System.out.println("Failed: "+this);
//	}
}
