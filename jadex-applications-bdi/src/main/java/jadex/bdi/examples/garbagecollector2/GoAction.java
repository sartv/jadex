package jadex.bdi.examples.garbagecollector2;

import java.util.Map;

import jadex.adapter.base.envsupport.environment.IAgentAction;
import jadex.adapter.base.envsupport.environment.IEnvironmentSpace;
import jadex.adapter.base.envsupport.environment.ISpaceAction;
import jadex.adapter.base.envsupport.environment.ISpaceObject;
import jadex.adapter.base.envsupport.environment.ObjectEvent;
import jadex.adapter.base.envsupport.environment.space2d.Grid2D;
import jadex.adapter.base.envsupport.environment.space2d.Space2D;
import jadex.adapter.base.envsupport.environment.space2d.action.AbstractSpace2dAction;
import jadex.adapter.base.envsupport.math.IVector2;
import jadex.adapter.base.envsupport.math.Vector2Int;
import jadex.bdi.examples.garbagecollector.Position;

/**
 *  The go action for moving one field in one of four directions.
 */
public class GoAction implements IAgentAction
{
	//-------- constants --------

	/** The directions. */
	public static final String UP = "up";
	public static final String DOWN = "down";
	public static final String LEFT = "left";
	public static final String RIGHT = "right";

	public static final String DIRECTION = "direction";
	
//	public static final String POSITION_CHANGED = "position_changed";

	
	//-------- methods --------
	
	/**
	 * Performs the action.
	 * @param parameters parameters for the action
	 * @param space the environment space
	 * @return action return value
	 */
	public Object perform(Map parameters, IEnvironmentSpace space)
	{
//		System.out.println("go action: "+parameters);
		
		String dir = (String)parameters.get(DIRECTION);
		Object oid = parameters.get(IAgentAction.OBJECT_ID);
		ISpaceObject obj = space.getSpaceObject(oid);
		IVector2 pos = (IVector2)obj.getProperty(Space2D.POSITION);
		
		IVector2 size = ((Space2D)space).getAreaSize();
//		int sizex = size.getXAsInteger();
//		int sizey = size.getYAsInteger();
		int px = pos.getXAsInteger();
		int py = pos.getYAsInteger();
		if(dir.equals(UP))
		{
//			pos = new Vector2Int(px, (py-1+sizey)%sizey);
			pos = new Vector2Int(px, py-1);
		}
		else if(dir.equals(DOWN))
		{
//			pos = new Vector2Int(px, (py+1)%sizey);
			pos = new Vector2Int(px, py+1);
		}
		else if(dir.equals(LEFT))
		{
//			pos = new Vector2Int((px-1+sizex)%sizex, py);
			pos = new Vector2Int(px-1, py);
		}
		else if(dir.equals(RIGHT))
		{
//			pos = new Vector2Int((px+1)%sizex, py);
			pos = new Vector2Int(px+1, py);
		}
		
		((Space2D)space).setPosition(oid, pos);
		
//		System.out.println("Go action: "+obj.getProperty(ISpaceObject.ACTOR_ID)+" "+pos);
		
//		obj.fireObjectEvent(new ObjectEvent(POSITION_CHANGED));
		
		return null;
	}

	/**
	 * Returns the ID of the action.
	 * @return ID of the action
	 */
	public Object getId()
	{
		// todo: remove here or from application xml?
		return "go";
	}
}
