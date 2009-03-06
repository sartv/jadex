package jadex.bdi.examples.hunterprey2.environment;

import jadex.bdi.examples.hunterprey2.Creature;
import jadex.bdi.examples.hunterprey2.Food;
import jadex.bdi.examples.hunterprey2.Hunter;
import jadex.bdi.examples.hunterprey2.IEnvironment;
import jadex.bdi.examples.hunterprey2.Location;
import jadex.bdi.examples.hunterprey2.Observer;
import jadex.bdi.examples.hunterprey2.Obstacle;
import jadex.bdi.examples.hunterprey2.Prey;
import jadex.bdi.examples.hunterprey2.RequestMove;
import jadex.bdi.examples.hunterprey2.TaskInfo;
import jadex.bdi.examples.hunterprey2.Vision;
import jadex.bdi.examples.hunterprey2.WorldObject;
import jadex.bdi.examples.hunterprey2.engine.action.EatAction;
import jadex.bdi.examples.hunterprey2.engine.action.MoveAction;
import jadex.bdi.planlib.simsupport.common.math.IVector2;
import jadex.bdi.planlib.simsupport.common.math.Vector1Int;
import jadex.bdi.planlib.simsupport.common.math.Vector2Double;
import jadex.bdi.planlib.simsupport.environment.ISimulationEventListener;
import jadex.bdi.planlib.simsupport.environment.grid.IGridSimulationEngine;
import jadex.bdi.planlib.simsupport.environment.grid.simobject.task.GoToDirectionTask;
import jadex.bdi.planlib.simsupport.environment.grid.simobject.task.MoveObjectTask;
import jadex.bdi.planlib.simsupport.environment.simobject.SimObject;
import jadex.bdi.planlib.simsupport.simcap.LocalSimulationEventListener;
import jadex.bdi.runtime.IExternalAccess;
import jadex.commons.SUtil;
import jadex.commons.SimplePropertyChangeSupport;

import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nuggets.Nuggets;

/**
 *  The environment is the container all objects and creatures.
 */
public class Environment implements IEnvironment
{
	//-------- constants --------

//	// The world behavior 
//	public static final int WORLD_BEHAVIOR_EUCLID = 0;
//	public static final int WORLD_BEHAVIOR_TORUS = 1;

	// predefined object types
	public static final String OBJECT_TYPE_OBSTACLE = "obstacle";
	public static final String OBJECT_TYPE_FOOD = "food";
	public static final String OBJECT_TYPE_HUNTER = "hunter";
	public static final String OBJECT_TYPE_PREY = "prey";

	// predefined properties
	public static final String PROPERTY_ONTOLOGY = "ontologyObj";

	/** The default number of lease ticks. */
	public static int DEFAULT_LEASE_TICKS = 50;

	//-------- attributes --------
//
//	/** The behavior of the world */
//	public int world_bahavior = WORLD_BEHAVIOR_TORUS;

	/** The agent created this environment */
	protected IExternalAccess agent;

	/** The simulation environment to wrap on */
//	protected ISimulationEngine engine;
	protected IGridSimulationEngine engine;

//	/** All world object simulation id's accessible per location. */
//	protected MultiCollection world;

	/** The creatures simId's */
	protected Map creatures;

//	/** The obstacles simId's */
//	protected Set obstacles;

//	/** The prey food simId's */
//	protected Set food;

	/** The list for move and eat requests. */
	protected List tasklist;

	/** The counter for move and ??? sim engine tasks. */
	protected int simtaskcounter;

	/** The helper object for bean events. */
	protected SimplePropertyChangeSupport pcs;

	/** The helper simulation event listener */
	ISimulationEventListener listener;

	/** The highscore location. */
	protected List highscore;

	/** The last time the highscore was saved. */
	protected long savetime;

	/** The interval between saves of highscore (-1 for autosave off). */
	protected long saveinterval;

	/** The foodrate determines how often new food pops up. */
	protected int foodrate;

	/** The world age. */
	protected int age;

//	/** The random number generator to create locations. */
//	protected Random rand = new Random(System.currentTimeMillis());

	//-------- constructors --------

	/**
	 *  Create a new environment with local simengine reference
	 */
	public Environment(IExternalAccess agent)
	{
		this(agent, null);

	}

	/**
	 *  Create a new environment with local simengine reference
	 */
	public Environment(IExternalAccess agent, IGridSimulationEngine engine)
	{

		assert agent != null : this + " - no external access provided";

		this.agent = agent;

		this.engine = engine;
		// Pre-declare object types
		engine.declareObjectType(OBJECT_TYPE_HUNTER);
		engine.declareObjectType(OBJECT_TYPE_PREY);
		engine.declareObjectType(OBJECT_TYPE_OBSTACLE);
		engine.declareObjectType(OBJECT_TYPE_FOOD);
		
		engine.addAction(new EatAction());
		engine.addAction(new MoveAction());

//		this.world = new MultiCollection();
		this.creatures = new HashMap();
//		this.obstacles = new HashSet();
//		this.food = new HashSet();

		this.tasklist = new ArrayList();
		this.simtaskcounter = 0;

		this.pcs = new SimplePropertyChangeSupport(this);

		this.foodrate = 5;
		this.saveinterval = 5000;
		highscore = loadHighscore();

	}

	//--- simulation engine support methods -----

	protected String getSimObjectType(WorldObject wo)
	{

		if (wo instanceof Hunter)
		{
			return OBJECT_TYPE_HUNTER;
		}
		else if (wo instanceof Prey)
		{
			return OBJECT_TYPE_PREY;
		}
		else if (wo instanceof Food)
		{
			return OBJECT_TYPE_FOOD;
		}
		else if (wo instanceof Obstacle)
		{
			return OBJECT_TYPE_OBSTACLE;
		}
		else
		{
			return "undefined";
		}

	}

	/**
	 * Instantiate a simobject
	 * @return The new simId Integer
	 */
	protected Integer createSimObject(WorldObject wo, String type,
			Map properties, List tasks, boolean signalDestruction,
			boolean listen)
	{

		Location l = wo.getLocation();

		if (properties == null)
			properties = new HashMap();
		properties.put(PROPERTY_ONTOLOGY, wo);

		if (listen)
		{
			if (listener == null)
				listener = new LocalSimulationEventListener(agent);
		}

		Integer simId = engine.createSimObject(type, properties, tasks, l
				.getAsIVector(), signalDestruction, listener);
		
		wo.setSimId(simId);

		return simId;

	}

	/**
	 * Create a SimEnvironment goal to destroy a simobject
	 * @param wo
	 * @return The goal that handles the creation
	 */
	protected boolean destroySimObject(WorldObject wo)
	{

		if (wo.getSimId() == null)
		{
			agent.getLogger().warning(
					"Try to destroy WorldObject without SimId! " + wo);

			return false;
		}

		engine.destroySimObject(wo.getSimId());
		wo.setSimId(null);

		return true;

	}


	/**
	 * Decrease the SimTaskCounter by one
	 */
	protected synchronized void decreaseSimTaskCounter()
	{
		this.pcs.firePropertyChange("simTaskCounter", simtaskcounter, simtaskcounter--);
	}

	/**
	 * Get the number of current active sim tasks
	 * @return The counter for sim tasks
	 */
	protected int getSimTaskCounter()
	{
		return simtaskcounter;
	}

	//-------- interface methods --------

	/**
	 *  Move one field upwards. The method will block
	 *  until the current simulation step has finished.
	 *  @return True, when the operation succeeded.
	 */
	public boolean moveUp(Creature me)
	{
		return move(me, RequestMove.DIRECTION_UP);
	}

	/**
	 *  Move one field downwards. The method will block
	 *  until the current simulation step has finished.
	 *  @return True, when the operation succeeded.
	 */
	public boolean moveDown(Creature me)
	{
		return move(me, RequestMove.DIRECTION_DOWN);
	}

	/**
	 *  Move one field to the left. The method will block
	 *  until the current simulation step has finished.
	 *  @return True, when the operation succeeded.
	 */
	public boolean moveLeft(Creature me)
	{
		return move(me, RequestMove.DIRECTION_LEFT);
	}

	/**
	 *  Move one field to the right. The method will block
	 *  until the current simulation step has finished.
	 *  @return True, when the operation succeeded.
	 */
	public boolean moveRight(Creature me)
	{
		return move(me, RequestMove.DIRECTION_RIGHT);
	}

	/**
	 *  Eat some object. The object has to be at the same location.
	 *  This method does not block, and can be called multiple
	 *  times during each simulation step.
	 *  @param food The object.
	 *  @return True, when the operation succeeded.
	 */
	public boolean eat(Creature me, WorldObject food)
	{
		boolean ret = false;
		int points = 0;
		me = getCreature(me);
		me.setLeaseticks(DEFAULT_LEASE_TICKS);
		if (food instanceof Food)
		{
			ret = engine.performAction(EatAction.DEFAULT_NAME, me.getSimId(), food.getSimId(), null);
			points = 1;
		}
		else if (me instanceof Hunter && food instanceof Prey)
		{
			// Get actual creature from world model.
			Creature creat = getCreature((Creature) food);
			ret = engine.performAction(EatAction.DEFAULT_NAME, me.getSimId(), creat.getSimId(), null);
			
			removeCreature(creat);
			points = 5;
		}

		if (ret)
		{
			me.setPoints(me.getPoints() + points);
		}
		else
		{
			System.out.println("Creature tried to cheat: " + me.getName());
		}

		return ret;

	}

	/**
	 *  Add a move or eat action to the queue.
	 */
	public synchronized TaskInfo addEatTask(Creature me, WorldObject obj)
	{

		TaskInfo ret = new TaskInfo(new Object[]
		{ "eat", me, obj });
		tasklist.add(ret);
		this.pcs.firePropertyChange("taskSize", tasklist.size() - 1, tasklist
				.size());

		return ret;

	}

	/**
	 *  Add a move or eat action to the queue.
	 */
	public synchronized TaskInfo addMoveTask(Creature me, String dir)
	{

		TaskInfo ret = new TaskInfo(new Object[]
		{ "move", me, dir });
		tasklist.add(ret);
		this.pcs.firePropertyChange("taskSize", tasklist.size() - 1, tasklist
				.size());

		return ret;

	}

	/**
	 * Clear the TaskList
	 * HACK! Should be done in executeStep method, but that leads to problems with
	 * other Agents. They compute their next step with the old vision. :-( 
	 * This is a race condition too! Tasks can be lost.
	 */
	protected synchronized void clearTaskList()
	{

		int length = tasklist.size();
		tasklist.clear();
		this.pcs.firePropertyChange("taskSize", length, tasklist.size());

	}

	/**
	 *  Get the current vision (without updating the creatures leaseticks).
	 *  @param me The creature.
	 */
	public Vision internalGetVision(Creature me)
	{

		Vision ret = new Vision();
		me = getCreature(me);
		WorldObject[] wos;
		if (me instanceof Observer)
		{
			wos = getAllObjects();
		}
		else
		{
			wos = getNearObjects(me.getLocation(), me.getVisionRange());
		}
		ret.setObjects(wos);

		return ret;

	}

	/**
	 *  Get the current vision. This method does not block,
	 *  and can be called multiple times during each simulation step.
	 *  @param me The creature.
	 */
	public Vision getVision(Creature me)
	{

		me = getCreature(me);
		me.setLeaseticks(DEFAULT_LEASE_TICKS);

		return internalGetVision(me);

	}

	/**
	 *  Get the width of the world.
	 */
	public int getWidth()
	{

		return engine.getAreaSize().getXAsInteger();

	}

	/**
	 *  Get the height of the world.
	 */
	public int getHeight()
	{

		return engine.getAreaSize().getYAsInteger();

	}

	//-------- management methods --------

	/**
	 *  Add a new prey food to the world.
	 *  @param nfood The new food.
	 */
	public void addFood(Food nfood)
	{

		createSimObject(nfood, OBJECT_TYPE_FOOD, null, null, false, false);

//		this.food.add(nfood);
//		this.world.put(nfood.getLocation(), nfood);

	}

	/**
	 *  remove a prey food to the world.
	 *  @param nfood The food.
	 */
	public boolean removeFood(Food nfood)
	{

		if (engine.getSimulationObject(nfood.getSimId()) != null)
		{
			destroySimObject(nfood);
			return true;
		}
		return false;

	}

	/**
	 *  Add a new obstacle to the world.
	 *  @param obstacle The new obstacle.
	 */
	public void addObstacle(Obstacle obstacle)
	{

		createSimObject(obstacle, OBJECT_TYPE_OBSTACLE, null, null, false, false);

	}

	/**
	 *  Remove a  obstacle to the world.
	 *  @param obstacle The obstacle.
	 */
	public boolean removeObstacle(Obstacle obstacle)
	{

		if (engine.getSimulationObject(obstacle.getSimId()) != null)
		{
			destroySimObject(obstacle);
			return true;
		}
		return false;

	}

	/**
	 *  Add a new creature to the world.
	 *  @param creature The creature.
	 */
	public synchronized Creature addCreature(Creature creature)
	{

		Creature copy;

		if (!creatures.containsKey(creature))
		{
			copy = (Creature) creature.clone();
			copy.setLeaseticks(DEFAULT_LEASE_TICKS);
			copy.setWorldWidth(getWidth());
			copy.setWorldHeight(getHeight());

			this.creatures.put(copy, copy);

			if (!(copy instanceof Observer))
			{
				copy.setAge(0);
				copy.setPoints(0);
				copy.setLocation(getEmptyLocation());
				//if(copy instanceof Hunter)
				//	copy.setVisionRange(5);
				//else
				copy.setVisionRange(3);

				Map props = new HashMap();
				props.put(PROPERTY_ONTOLOGY, copy);

				List tasks = new ArrayList();
				tasks.add(new MoveObjectTask(new Vector2Double(0.0), engine.getAreaSize().copy()));
				createSimObject(copy, getSimObjectType(copy), props, tasks,
						true, true);

				this.highscore.add(copy);

			}
		}
		else
		{
			throw new RuntimeException("Creature already exists: " + creature);
		}

		return copy;

	}

	/**
	 *  Remove a creature to the world.
	 *  @param creature The creature.
	 */
	public synchronized boolean removeCreature(Creature creature)
	{

		// Remove tasks of this creature.
		int tasks = tasklist.size();
		for (Iterator it = tasklist.iterator(); it.hasNext();)
		{
			TaskInfo task = (TaskInfo) it.next();
			Object[] params = (Object[]) task.getAction();
			if (creature.equals(params[1]))
			{
				it.remove();
			}
		}

		this.pcs.firePropertyChange("taskSize", tasks, tasklist.size());

//		if (this.world.containsKey(creature.getLocation()))
//			this.world.remove(creature.getLocation(), creature);

		Creature removed = (Creature) this.creatures.remove(creature);
		if (removed != null)
		{
			// destroy simobject
			return destroySimObject(removed);
		}

		return false;

	}

	/**
	 *  Execute a step.
	 */
	public synchronized void executeStep()
	{

		
//		StringBuffer b = new StringBuffer();
//		b.append("executing step " + age + "\n");
//		b.append("WorldSize=\t" + world.size() + "\n");
//		b.append("CreatureSize=\t" + creatures.size() + "\n");
//		b.append("FoodSize=\t" + food.size() + "\n");
//		b.append("TaskSize=\t" + tasklist.size() + "\n");
//		b.append("-------------------------------------");
//		System.out.println(b.toString());
		
		if (simtaskcounter != 0)
		{
			agent.getLogger().warning(
					this + " - execute a step with taskcounter != 0");
		}
		
		// halt the engine during step execution
		Object simObjAccess = engine.getSimObjectAccess();
		synchronized (simObjAccess)
		{
			// Creatures that already acted in this step.
			Set acted = new HashSet();

			Creature[] creatures = getCreatures();
			for (int i = 0; i < creatures.length; i++)
			{
				creatures[i].setAge(creatures[i].getAge() + 1);
				creatures[i].setLeaseticks(creatures[i].getLeaseticks() - 1);
				if (creatures[i].getLeaseticks() < 0)
					removeCreature(creatures[i]);
			}

			// Perform eat/move tasks.
			TaskInfo[] tasks = (TaskInfo[]) tasklist.toArray(new TaskInfo[tasklist
					.size()]);
			for (int i = 0; i < tasks.length; i++)
			{
				Object[] params = (Object[]) tasks[i].getAction();
				if (params[0].equals("eat"))
				{
					if (!acted.contains(params[1]))
					{
						tasks[i].setResult(new Boolean(eat((Creature) params[1],
								(WorldObject) params[2])));
						acted.add(params[1]);
					}
					else
					{
						tasks[i].setResult(new Boolean(false));
					}
				}
			}
			for (int i = 0; i < tasks.length; i++)
			{
				Object[] params = (Object[]) tasks[i].getAction();
				if (params[0].equals("move"))
				{

					if (!acted.contains(params[1]))
					{
						tasks[i].setResult(new Boolean(move((Creature) params[1],
								(String) params[2])));
						acted.add(params[1]);
					}
					else
					{
						tasks[i].setResult(new Boolean(false));
					}
				}
			}

			// TODO: move food creation to engine process?
			
			// Place new food.
			if (age % foodrate == 0)
			{
				int foodCount = ((List) engine.getTypedSimObjectAccess().get(OBJECT_TYPE_FOOD)).size();
				
				if (foodCount < ((Integer) agent.getBeliefbase().getBelief(
						"max_food").getFact()).intValue())
				{
					IVector2 pos = engine.getEmptyGridPosition();
					IVector2 test = engine.getEmptyGridPosition();

					// Make sure there will be some empty location left.
					if (!pos.equals(test))
					{
						addFood(new Food(new Location(pos.getXAsInteger(), pos.getYAsInteger())));
					}
				}
				else
				{
//					// hack for testing- remove old food
//					System.out.println("-- removing old food --");
//					WorldObject[] f = (WorldObject[]) food.toArray(new WorldObject[food.size()]);
//					int count = 0;
//					for (int i = 0; i < f.length && count < 10; i++)
//					{
//						if (f[i].getSimId() != null)
//						{
//							removeFood((Food) f[i]);
//							count++;
//						}
//					}
				}
			}

			tasklist.clear();
			this.pcs.firePropertyChange("taskSize", tasks.length, tasklist.size());
			
		}


		// Save highscore.
		long time = System.currentTimeMillis();
		if (saveinterval >= 0 && savetime + saveinterval <= time)
		{
			saveHighscore();
			savetime = time;
		}

		age++;

	}

	/**
	 *  Get the world age.
	 *  @return The age of the world.
	 */
	public int getWorldAge()
	{

		return age;

	}

	/**
	 *  Get the foodrate.
	 *  @return The foodrate.
	 */
	public int getFoodrate()
	{

		return foodrate;

	}

	/**
	 *  Set the foodrate. 
	 *  @param foodrate The foodrate.
	 */
	public void setFoodrate(int foodrate)
	{

		this.foodrate = foodrate;

	}

	/**
	 *  Perform a move.
	 *  @param me The creature.
	 *  @param dir The direction.
	 */
	public boolean move(Creature me, String dir)
	{

		me = getCreature(me);
		me.setLeaseticks(DEFAULT_LEASE_TICKS);
		if (me.getSimId() == null)
		{
			agent.getLogger().warning("Creature without sim id requested move! " + me);
			return false;
		}
		
		IVector2 direction = null;
		if (RequestMove.DIRECTION_UP.equals(dir))
		{
			direction = GoToDirectionTask.DIRECTION_UP;
		}
		else if (RequestMove.DIRECTION_DOWN.equals(dir))
		{
			direction = GoToDirectionTask.DIRECTION_DOWN;
		}
		else if (RequestMove.DIRECTION_LEFT.equals(dir))
		{
			direction = GoToDirectionTask.DIRECTION_LEFT;
		}
		else if (RequestMove.DIRECTION_RIGHT.equals(dir))
		{
			direction = GoToDirectionTask.DIRECTION_RIGHT;
		}
		List parameters = new ArrayList();
		parameters.add(direction);
		boolean ret = engine.performAction(MoveAction.DEFAULT_NAME, me.getSimId(), null, parameters);
		if (ret)
		{
			simtaskcounter++;
			return true;
		}
		
		return false;

	}

	/**
	 *  Get the creatures.
	 *  @return The creatures.
	 */
	public Creature[] getCreatures()
	{

		return (Creature[]) creatures.values().toArray(
				new Creature[creatures.size()]);

	}

	/**
	 *  Get the obstacles.
	 *  @return The obstacles.
	 */
	public Obstacle[] getObstacles()
	{

//		ArrayList ret = new ArrayList();
//
//		// halt the engine
//		Map typedAccess = engine.getTypedSimObjectAccess();
//		synchronized (typedAccess)
//		{
//			Collection simobjs = (List) typedAccess.get(Environment.OBJECT_TYPE_OBSTACLE);
//			for (Iterator it = simobjs.iterator(); it.hasNext();)
//			{
//				SimObject object = (SimObject) it.next();
//				Object obstacle = object.getProperty(PROPERTY_ONTOLOGY);
//				if (obstacle!= null && obstacle instanceof Food)
//				{
//					ret.add(obstacle);
//				}
//			}
//		}
		
		Collection ret = getOntologyObjects(OBJECT_TYPE_OBSTACLE);

		return (Obstacle[]) ret.toArray(new Obstacle[ret.size()]);

		
	}

	/**
	 *  Get the obstacles.
	 *  @return The obstacles.
	 */
	public Food[] getFood()
	{

//		ArrayList ret = new ArrayList();
//
//		// halt the engine
//		Map typedAccess = engine.getTypedSimObjectAccess();
//		synchronized (typedAccess)
//		{
//			Collection simobjs = (List) typedAccess.get(Environment.OBJECT_TYPE_FOOD);
//			for (Iterator it = simobjs.iterator(); it.hasNext();)
//			{
//				SimObject object = (SimObject) it.next();
//				Object food = object.getProperty(PROPERTY_ONTOLOGY);
//				if (food!= null && food instanceof Food)
//				{
//					ret.add(food);
//				}
//			}
//		}
		
		Collection ret = getOntologyObjects(OBJECT_TYPE_FOOD);

		return (Food[]) ret.toArray(new Food[ret.size()]);

	}
	
	/**
	 *  Get the obstacles.
	 *  @return The obstacles.
	 */
	public Collection getOntologyObjects(String engineObjectType)
	{

		ArrayList ret = new ArrayList();

		// halt the engine
		Map typedAccess = engine.getTypedSimObjectAccess();
		synchronized (typedAccess)
		{
			Collection simobjs = (List) typedAccess.get(engineObjectType);
			for (Iterator it = simobjs.iterator(); it.hasNext();)
			{
				SimObject object = (SimObject) it.next();
				Object o = object.getProperty(PROPERTY_ONTOLOGY);
				ret.add(o);
			}
		}

		return ret;

	}

	/**
	 *  Get all objects in the world (obstacles, food, and creature).
	 */
	public WorldObject[] getAllObjects()
	{

		// Add obstacles and food to return set.
		ArrayList ret = new ArrayList();

		// halt the engine
		Map simObjectAccess = engine.getSimObjectAccess();
		synchronized (simObjectAccess)
		{
			Collection simobjs = simObjectAccess.values();
			for (Iterator it = simobjs.iterator(); it.hasNext();)
			{
				SimObject object = (SimObject) it.next();
				Object worldobject = object.getProperty(PROPERTY_ONTOLOGY);
				if (worldobject!= null && worldobject instanceof WorldObject)
				{
					ret.add(worldobject);
				}
			}
		}

		return (WorldObject[]) ret.toArray(new WorldObject[ret.size()]);

	}

//	/**
//	 *  Create a location.
//	 *  @param loc The location.
//	 *  @param dir The direction.
//	 *  @return The new location.
//	 */
//	protected Location createLocation(Location loc, String dir)
//	{
//
//		int sizey = getHeight();
//		int sizex = getWidth();
//
//		int x = loc.getX();
//		int y = loc.getY();
//
//		switch (world_bahavior)
//		{
//			case WORLD_BEHAVIOR_EUCLID:
//			{
//				if (RequestMove.DIRECTION_UP.equals(dir))
//				{
//					y = (y - 1 <= sizey ? y - 1 : y);
//				}
//				else if (RequestMove.DIRECTION_DOWN.equals(dir))
//				{
//					y = (y + 1 >= 0 ? y + 1 : y);
//				}
//				else if (RequestMove.DIRECTION_LEFT.equals(dir))
//				{
//					x = (x - 1 >= 0 ? x - 1 : x);
//				}
//				else if (RequestMove.DIRECTION_RIGHT.equals(dir))
//				{
//					x = (x + 1 <= sizex ? x + 1 : x);
//				}
//			}
//				break;
//
//			case WORLD_BEHAVIOR_TORUS:
//			{
//				if (RequestMove.DIRECTION_UP.equals(dir))
//				{
//					y = (sizey + y - 1) % sizey;
//				}
//				else if (RequestMove.DIRECTION_DOWN.equals(dir))
//				{
//					y = (y + 1) % sizey;
//				}
//				else if (RequestMove.DIRECTION_LEFT.equals(dir))
//				{
//					x = (sizex + x - 1) % sizex;
//				}
//				else if (RequestMove.DIRECTION_RIGHT.equals(dir))
//				{
//					x = (x + 1) % sizex;
//				}
//			}
//				break;
//
//			default:
//				// create no new location
//				break;
//		}
//
//		return new Location(x, y);
//
//	}

	/**
	 *  Get an empty location.
	 *  Retrieves an empty grid position and creates a Location object for that position.
	 *  @return The location.
	 */
	public synchronized Location getEmptyLocation()
	{

		IVector2 emptyPosition = engine.getEmptyGridPosition();
		return new Location(emptyPosition.getXAsInteger(), emptyPosition.getYAsInteger());

	}

	/**
	 *  Return the size of the task list.
	 *  @return The task size.
	 */
	public int getTaskSize()
	{

		return tasklist.size();

	}

	/**
	 *  Get the internal representation of a creature.
	 *  If the creature is unknown it gets added to the environment.
	 *  @param creature The creature.
	 *  @return The creature as known in the environment.
	 */
	protected Creature getCreature(Creature creature)
	{

		Creature ret = (Creature) creatures.get(creature);

		if (ret == null)
		{
			ret = addCreature(creature);
		}

		return ret;

	}

	/**
	 *  Get objects near a position.
	 *  @param loc The location.
	 *  @param range The range.
	 */
	protected WorldObject[] getNearObjects(Location loc, int range)
	{

		SimObject[] objs = engine.getNearObjects(loc.getAsIVector(), new Vector1Int(range));
		WorldObject[] wos = new WorldObject[objs.length];
		for (int i = 0; i < objs.length; i++)
		{
			wos[i] = (WorldObject) objs[i].getProperty(PROPERTY_ONTOLOGY);
		}
		return wos;
		
//		int sizey = getHeight();
//		int sizex = getWidth();
//
//		Collection ret = new ArrayList();
//		int x = loc.getX();
//		int y = loc.getY();
//
//		switch (world_bahavior)
//		{
//			case WORLD_BEHAVIOR_TORUS:
//			{
//				for (int i = x - range; i <= x + range; i++)
//				{
//					for (int j = y - range; j <= y + range; j++)
//					{
//						Collection tmp = world.getCollection(new Location(
//								(i + sizex) % sizex, (j + sizey) % sizey));
//						if (tmp != null)
//							ret.addAll(tmp);
//					}
//				}
//			}
//				break;
//
//			case WORLD_BEHAVIOR_EUCLID:
//			{
//				int minx = (x - range >= 0 ? x - range : 0);
//				int maxx = (x + range <= sizex ? x + range : sizex);
//
//				int miny = (y - range >= 0 ? y - range : 0);
//				int maxy = (y + range <= sizey ? y + range : sizey);
//
//				for (int i = minx; i <= maxx; i++)
//				{
//					for (int j = miny; j <= maxy; j++)
//					{
//						Collection tmp = world
//								.getCollection(new Location(i, j));
//						if (tmp != null)
//							ret.addAll(tmp);
//					}
//				}
//			}
//				break;
//
//			default:
//				// ignore, no vision
//				break;
//		}
//
//		return (WorldObject[]) ret.toArray(new WorldObject[ret.size()]);

	}

	/**
	 *  Get the current highscore.
	 *  @return The 10 best creatures.
	 */
	public synchronized Creature[] getHighscore()
	{

		try
		{
			Collections.sort(highscore, new Comparator()
			{
				public int compare(Object o1, Object o2)
				{
					return ((Creature) o2).getPoints()
							- ((Creature) o1).getPoints();
				}
			});
			List copy = highscore.subList(0, Math.min(highscore.size(), 10));

			return (Creature[]) copy.toArray(new Creature[copy.size()]);
		}
		catch (ClassCastException cce)
		{
			return new Creature[0];
		}

	}

	/**
	 *  Load the highscore from a file.
	 */
	private List loadHighscore()
	{

		List highscore_ = null;

		// Read highscore list.
		InputStream is = null;
		try
		{
			//			InputStream tmp = SUtil.getResource("highscore.dmp", Environment.class.getClassLoader());
			//			ObjectInputStream is = new ObjectInputStream(tmp);
			//			this.highscore = SUtil.arrayToList(is.readObject());
			//			is.close();

			is = SUtil.getResource("highscore.dmp", Environment.class
					.getClassLoader());
			StringBuffer out = new StringBuffer();
			byte[] b = new byte[4096];
			for (int n; (n = is.read(b)) != -1;)
			{
				out.append(new String(b, 0, n));
			}
			highscore_ = SUtil.arrayToList(Nuggets.objectFromXML(
					out.toString(), Environment.class.getClassLoader()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//			System.out.println(e);
			highscore_ = new ArrayList();
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (Exception e)
				{
				}
			}
		}

		return highscore_;

	}

	/**
	 *  Save the highscore to a file.
	 */
	public synchronized void saveHighscore()
	{

		OutputStreamWriter os = null;
		try
		{
			String outputFile = "highscore.dmp";

			// write as serialized object
			//ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(outputFile));
			//os.writeObject(getHighscore());
			//os.close();

			// write as xml file
			os = new OutputStreamWriter(new FileOutputStream(outputFile),
					"UTF-8");
			os.write(Nuggets.objectToXML(getHighscore(), this.getClass()
					.getClassLoader()));
			os.close();

			//			System.out.println("Saved highscore.");
		}
		catch (Exception e)
		{
			System.out
					.println("Error writing hunterprey highscore 'highscore.dmp'.");
			e.printStackTrace();
		}
		finally
		{
			if (os != null)
			{
				try
				{
					os.close();
				}
				catch (Exception e)
				{
				}
			}
		}

	}

	/**
	 *  Set the highscore save interval (-1 for autosave off).
	 */
	public void setSaveInterval(long saveinterval)
	{

		this.saveinterval = saveinterval;

	}

	/**
	 *  Get the highscore save interval (-1 for autosave off).
	 */
	public long getSaveInterval()
	{

		return this.saveinterval;

	}

	//-------- property methods --------

	/**
	 *  Add a PropertyChangeListener to the listener list.
	 *  The listener is registered for all properties.
	 *  @param listener  The PropertyChangeListener to be added.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{

		pcs.addPropertyChangeListener(listener);

	}

	/**
	 *  Remove a PropertyChangeListener from the listener list.
	 *  This removes a PropertyChangeListener that was registered
	 *  for all properties.
	 *  @param listener  The PropertyChangeListener to be removed.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{

		pcs.removePropertyChangeListener(listener);

	}

}
