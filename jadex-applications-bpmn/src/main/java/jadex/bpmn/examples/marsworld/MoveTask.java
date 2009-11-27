package jadex.bpmn.examples.marsworld;

import jadex.adapter.base.envsupport.environment.AbstractTask;
import jadex.adapter.base.envsupport.environment.IEnvironmentSpace;
import jadex.adapter.base.envsupport.environment.ISpaceObject;
import jadex.adapter.base.envsupport.environment.space2d.Space2D;
import jadex.adapter.base.envsupport.math.IVector2;
import jadex.adapter.base.envsupport.math.Vector1Double;
import jadex.bdi.runtime.IBeliefSet;
import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.service.clock.IClockService;

import java.util.Iterator;
import java.util.Set;

/**
 *  Move an object towards a destination.
 */
public class MoveTask extends AbstractTask
{
	//-------- constants --------
	
	/** The destination property. */
	public static final String	PROPERTY_TYPENAME = "move";
	
	/** The destination property. */
	public static final String	PROPERTY_DESTINATION = "destination";

	/** The scope property. */
	public static final String	PROPERTY_SCOPE = "scope";

	/** The speed property of the moving object (units per second). */
	public static final String	PROPERTY_SPEED	= "speed";
	
	/** The vision property of the moving object (radius in units). */
	public static final String	PROPERTY_VISION	= "vision";
		
	//-------- IObjectTask methods --------
	
	/**
	 *  Executes the task.
	 *  Handles exceptions. Subclasses should implement doExecute() instead.
	 *  @param space	The environment in which the task is executing.
	 *  @param obj	The object that is executing the task.
	 *  @param progress	The time that has passed according to the environment executor.
	 */
	public void execute(IEnvironmentSpace space, ISpaceObject obj, long progress, IClockService clock)
	{
		IVector2 destination = (IVector2)getProperty(PROPERTY_DESTINATION);
		final IBDIExternalAccess scope = (IBDIExternalAccess)getProperty(PROPERTY_SCOPE);

		double	speed	= ((Number)obj.getProperty(PROPERTY_SPEED)).doubleValue();
		double	maxdist	= progress*speed*0.001;
		IVector2	loc	= (IVector2)obj.getProperty(Space2D.PROPERTY_POSITION);
		// Todo: how to handle border conditions!?
		IVector2	newloc	= ((Space2D)space).getDistance(loc, destination).getAsDouble()<=maxdist
			? destination : destination.copy().subtract(loc).normalize().multiply(maxdist).add(loc);

		((Space2D)space).setPosition(obj.getId(), newloc);

		// Process vision at new location.
		double	vision	= ((Number)obj.getProperty(PROPERTY_VISION)).doubleValue();
		final Set objects	= ((Space2D)space).getNearObjects((IVector2)obj.getProperty(Space2D.PROPERTY_POSITION), new Vector1Double(vision), null);
		if(objects!=null)
		{
			scope.invokeLater(new Runnable()
			{
				public void run()
				{
					IBeliefSet	targetsbel	= scope.getBeliefbase().getBeliefSet("my_targets");
					for(Iterator it=objects.iterator(); it.hasNext(); )
					{
						ISpaceObject so = (ISpaceObject)it.next();
						if(so.getType().equals("target") && !targetsbel.containsFact(so))
						{
//							System.out.println("New target seen: "+scope.getAgentName()+", "+objects[i]);
							targetsbel.addFact(so);
						}
					}
				}
			});
		}
		
		if(newloc==destination)
			setFinished(space, obj, true);
	}
}
