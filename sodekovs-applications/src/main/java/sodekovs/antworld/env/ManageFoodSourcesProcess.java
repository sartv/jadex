package sodekovs.antworld.env;

import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SimplePropertyObject;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.ISpaceProcess;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;

/**
 * Process is responsible for the life cycle of the food source objects.
 */
public class ManageFoodSourcesProcess extends SimplePropertyObject implements ISpaceProcess {
	// -------- attributes --------

	// -------- constructors --------

	/**
	 * Create a new create package process.
	 */
	public ManageFoodSourcesProcess() {
		 System.out.println("Created Manage Food Sources Process!");
	}

	// -------- ISpaceProcess interface --------

	/**
	 * This method will be executed by the object before the process gets added to the execution queue.
	 * 
	 * @param clock
	 *            The clock.
	 * @param space
	 *            The space this process is running in.
	 */
	public void start(IClockService clock, IEnvironmentSpace space) {
//		this.lasttick = clock.getTick();
		// System.out.println("create package process started.");
	}

	/**
	 * This method will be executed by the object before the process is removed from the execution queue.
	 * 
	 * @param clock
	 *            The clock.
	 * @param space
	 *            The space this process is running in.
	 */
	public void shutdown(IEnvironmentSpace space) {
		// System.out.println("create package process shutdowned.");
	}

	/**
	 * Executes the environment process
	 * 
	 * @param clock
	 *            The clock.
	 * @param space
	 *            The space this process is running in.
	 */
	public void execute(IClockService clock, IEnvironmentSpace space) {
		
		ContinuousSpace2D mySpace = (ContinuousSpace2D) space;
		
		ISpaceObject[] allFood = mySpace.getSpaceObjectsByType("food");
		
		for(ISpaceObject food : allFood){
			
			if((Integer) food.getProperty("food_pieces") == 0){
				mySpace.destroySpaceObject(food.getId());
			}
		}
	}
}
