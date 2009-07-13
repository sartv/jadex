package jadex.bdi.examples.antworld.foraging;

import jadex.adapter.base.envsupport.environment.IEnvironmentSpace;
import jadex.adapter.base.envsupport.environment.ISpaceObject;
import jadex.adapter.base.envsupport.environment.space2d.Grid2D;
import jadex.adapter.base.envsupport.environment.space2d.Space2D;
import jadex.adapter.base.envsupport.math.IVector2;
import jadex.adapter.base.envsupport.math.Vector1Int;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Walk through space according to felt pheromones.
 */
public class PheromoneWalkPlan extends Plan {

	/**
	 * The plan body.
	 */
	public void body() {
		System.out.println("Called Pheromone Walk Plan!!!!!!!!!");

		ISpaceObject myself = (ISpaceObject) getBeliefbase()
				.getBelief("myself").getFact();
		IEnvironmentSpace env = (IEnvironmentSpace) getBeliefbase().getBelief(
				"env").getFact();
		Grid2D grid = (Grid2D) env;

		// drop other goals
		IGoal[] goals = getGoalbase().getGoals();
		System.out.println("#PheromoneWalkPlan#GoalBase before drop...");
		for (int i = 0; i < goals.length; i++) {
			System.out
					.println(goals[i].getType() + " , "
							+ goals[i].getLifecycleState() + ", "
							+ goals[i].toString());
		}
		System.out.println("***\n");

		goals = getGoalbase().getGoals("check");
		for (int i = 0; i < goals.length; i++) {
			goals[i].drop();
		}

		goals = getGoalbase().getGoals();
		System.out.println("#GoalBase after drop...");
		for (int i = 0; i < goals.length; i++) {
			System.out
					.println(goals[i].getType() + " , "
							+ goals[i].getLifecycleState() + ", "
							+ goals[i].toString());
		}
		while (getBeliefbase().getBeliefSet("pheromones").getFacts().length > 0) {
			System.out.println("#PheromoneWalkPlan# Executing while loop.");
			Collection pheromonesCol = grid.getNearObjects((IVector2) myself
					.getProperty(Space2D.PROPERTY_POSITION), new Vector1Int(5),
					"pheromone");
			// Select strongest pheromone!
			ISpaceObject selectedPheromone = selectStrongestPheromone(Arrays
					.asList(pheromonesCol.toArray()), myself);
			if (selectedPheromone == null) {
				System.out
						.println("#PheromoneWalkPlan# Break from While loop.");
				break;
			} else {
				IGoal walkRandomly = createGoal("go");
				walkRandomly.getParameter("pos").setValue(
						selectedPheromone
								.getProperty(Space2D.PROPERTY_POSITION));
				dispatchSubgoalAndWait(walkRandomly);
			}
		}

		System.out
				.println("#PheromoneWalkPlan# No pheromones in distance. Walking randomly on grid");
		// Walk randomly on the grid.
		IGoal randomWalk = createGoal("check");
		dispatchTopLevelGoal(randomWalk);

		// get pheromones within a defined distance

		// ISpaceObject[] pheromones = (ISpaceObject[]) getBeliefbase()
		// .getBeliefSet("pheromones").getFacts();
		// System.out.println("#PheromoneWalkPlan# Number of pheromones."
		// + pheromones.length);

		// for (int i = 0; i < pheromones.length; i++) {
		// System.out.println(pheromones[i].toString());
		// }

		// IEnvironmentSpace env = (IEnvironmentSpace)
		// getBeliefbase().getBelief(
		// "env").getFact();
		//		
		// ISpaceObject[] foodSources = (ISpaceObject[]) getBeliefbase()
		// .getBeliefSet("foodSources").getFacts();
		// IVector2 sourcePos = (IVector2) foodSources[0]
		// .getProperty(Space2D.PROPERTY_POSITION);
		// System.out
		// .println("#FoodMiningPlan# Destination of next point (foodSource): "
		// + sourcePos.toString());
		//
		// // change belief "destination"
		// // ISpaceObject myself =
		// // (ISpaceObject)getBeliefbase().getBelief("myself").getFact();
		// // myself.setProperty(CheckingPlanEnv.DESTINATION, sourcePos);
		//
		// IGoal[] goals = getGoalbase().getGoals();
		// System.out.println("#GoalBase before drop...");
		// for (int i = 0; i < goals.length; i++) {
		// System.out
		// .println(goals[i].getType() + " , "
		// + goals[i].getLifecycleState() + ", "
		// + goals[i].toString());
		// }
		// System.out.println("***\n");
		//
		// goals = getGoalbase().getGoals("check");
		// for (int i = 0; i < goals.length; i++) {
		// goals[i].drop();
		// }
		//
		// goals = getGoalbase().getGoals();
		// System.out.println("#GoalBase after drop...");
		// for (int i = 0; i < goals.length; i++) {
		// System.out
		// .println(goals[i].getType() + " , "
		// + goals[i].getLifecycleState() + ", "
		// + goals[i].toString());
		// }
		//
		// // Move to the food source.
		// System.out.println("#FoodMiningPlan# walking to food source: "
		// + sourcePos.toString());
		// IGoal go = createGoal("go");
		// go.getParameter("pos").setValue(sourcePos);
		// dispatchSubgoalAndWait(go);
		//
		// // Take a piece of food.
		// Map params = new HashMap();
		// params.put(ISpaceAction.ACTOR_ID, getAgentIdentifier());
		// SyncResultListener srl = new SyncResultListener();
		// env.performSpaceAction("pickup", params, srl);
		// System.out.println("#FoodMiningPlan# trying ot pick up food.");
		// srl.waitForResult();
		// // TODO: Model failed situation!
		// // if(!((Boolean)srl.waitForResult()).booleanValue())
		// // fail();
		// System.out.println("#FoodMiningPlan# successfully picked up food.");
		//
		// // Move to the food source.
		// ISpaceObject[] nests = (ISpaceObject[]) getBeliefbase().getBeliefSet(
		// "nests").getFacts();
		// if (nests.length == 0) {
		// do {
		// // walk randomly on grid.
		// System.out
		// .println("#FoodMiningPlan# walking randomly on the grid since no nest is known.");
		// // IGoal checkGoal = createGoal("check");
		// // dispatchSubgoalAndWait(checkGoal);
		// IGoal walkRandomly = createGoal("go");
		// walkRandomly.getParameter("pos").setValue(
		// computeNextPositionRandomly());
		// dispatchSubgoalAndWait(walkRandomly);
		// nests = (ISpaceObject[]) getBeliefbase().getBeliefSet("nests")
		// .getFacts();
		// } while (nests.length == 0);
		// }
		// IVector2 nestPos = (IVector2) nests[0]
		// .getProperty(Space2D.PROPERTY_POSITION);
		// System.out.println("#FoodMiningPlan# walking to nest: "
		// + nestPos.toString());
		// IGoal goToNest = createGoal("go");
		// goToNest.getParameter("pos").setValue(nestPos);
		// dispatchSubgoalAndWait(goToNest);
		// System.out
		// .println("#FoodMiningPlan# Reached nest. Drop food and walk randomly on grid.");
		//
		// // TODO:
		// // getBeliefbase().getBeliefSet("carriedFood").removeFacts();
		// // Drop the piece of food in the nest.
		// params = new HashMap();
		// params.put(ISpaceAction.ACTOR_ID, getAgentIdentifier());
		// srl = new SyncResultListener();
		// env.performSpaceAction("drop", params, srl);
		// srl.waitForResult();
		// // TODO: Model failed situation!
		// // if(!((Boolean)srl.waitForResult()).booleanValue())
		// // fail();
		// System.out.println("#FoodMiningPlan# successfully dropped food.");
		//
		// // walk randomly on the grid.
		// IGoal randomWalk = createGoal("check");
		// dispatchTopLevelGoal(randomWalk);
		//
		// goals = getGoalbase().getGoals();
		// System.out.println("#GoalBase after creating new check goal...");
		// for (int i = 0; i < goals.length; i++) {
		// System.out
		// .println(goals[i].getType() + " , "
		// + goals[i].getLifecycleState() + ", "
		// + goals[i].toString());
		// }
	}

	/**
	 * Computes the strongest pheromone which determines the next destination of
	 * the ant.
	 * 
	 * @param pheromonesCol
	 *            the collection of recognized pheromones in neighbourhood.
	 * @return
	 */
	private ISpaceObject selectStrongestPheromone(List initialPheromonesList,
			ISpaceObject myself) {

		System.out.println("#PheromoneWalkPlan# Before ordering list");
		for (int i = 0; i < initialPheromonesList.size(); i++) {
			System.out.println("#PheromoneWalkPlan#" + i + ": "
					+ ((ISpaceObject) initialPheromonesList.get(i)).toString());
		}

		// ascending ordered.
		Collections.sort(initialPheromonesList, new PheromoneComparator());
		ArrayList strongestPheromonesAsBuckets = new ArrayList();

		// Hack: Remove pheromone of current pos of ant from list
		// TODO: Leave current pheromone in list but do randomChoice over
		// all pheromones according to their strength.

		// for (int i = 0; i < initialPheromonesList.size(); i++) {
		// if (((IVector2) (((ISpaceObject) initialPheromonesList.get(i))
		// .getProperty(Space2D.PROPERTY_POSITION)))
		// .equals((IVector2) myself
		// .getProperty(Space2D.PROPERTY_POSITION))) {
		// // nothing
		// } else {
		// target.add(initialPheromonesList.get(i));
		// }
		//
		// }

		System.out.println("#PheromoneWalkPlan# Ordered list and size: "
				+ initialPheromonesList.size());
		for (int i = 0; i < initialPheromonesList.size(); i++) {
			System.out.println("#PheromoneWalkPlan#" + i + ": "
					+ ((ISpaceObject) initialPheromonesList.get(i)).toString());
		}
		// Returns a list of the pheromones as buckets.
		strongestPheromonesAsBuckets = getBuckets(initialPheromonesList);

		// // Get all pheromoes that have the maximum value for their strength.
		// for (int i = target.size() - 1; i >= 0; i--) {
		// ISpaceObject pheromone = (ISpaceObject) target
		// .get(i);
		// if (i == target.size() - 1) {
		// strongestPheromones.add(pheromone);
		// } else if (new Integer(((ISpaceObject) strongestPheromones.get(0))
		// .getProperty("strength").toString()).intValue() == new Integer(
		// (pheromone.getProperty("strength").toString())).intValue()) {
		// strongestPheromones.add(pheromone);
		// } else {
		// break;
		// }
		// }
		//		
		// System.out
		// .println("#PheromoneWalkPlan# Strongest Pheromones without current Position!");
		// for (int i = 0; i < strongestPheromones.size(); i++) {
		// System.out.println("#PheromoneWalkPlan#" + i + ": "
		// + ((ISpaceObject) strongestPheromones.get(i)).toString());
		// }

		return getStrongestPheromoneRandomly(strongestPheromonesAsBuckets);
//		return strongestPheromones.size() == 0 ? null
//				: (ISpaceObject) strongestPheromones
//						.get(getRandomNumber(strongestPheromones.size() + 1));

		// OLD VERSION WITH DETERMINISTIC CHOICE --> the strongest pheromone is
		// always selected
		// ArrayList target = new ArrayList();
		//
		// System.out.println("#PheromoneWalkPlan# Before ordering list");
		// for (int i = 0; i < initialPheromonesList.size(); i++) {
		// System.out.println("#PheromoneWalkPlan#" + i + ": "
		// + ((ISpaceObject) initialPheromonesList.get(i)).toString());
		// }
		//
		// // ascending ordered.
		// Collections.sort(initialPheromonesList, new PheromoneComparator());
		// ArrayList strongestPheromones = new ArrayList();
		//
		// // Hack: Remove pheromone of current pos of ant from list
		// // TODO: Leave current pheromone in list but do randomChoice over
		// // all pheromones according to their strength.
		//
		// for (int i = 0; i < initialPheromonesList.size(); i++) {
		// if (((IVector2) (((ISpaceObject) initialPheromonesList.get(i))
		// .getProperty(Space2D.PROPERTY_POSITION)))
		// .equals((IVector2) myself
		// .getProperty(Space2D.PROPERTY_POSITION))) {
		// // nothing
		// } else {
		// target.add(initialPheromonesList.get(i));
		// }
		//
		// }
		//
		// System.out.println("#PheromoneWalkPlan# Ordered list and size: "
		// + target.size());
		// for (int i = 0; i < target.size(); i++) {
		// System.out.println("#PheromoneWalkPlan#" + i + ": "
		// + ((ISpaceObject) target.get(i)).toString());
		// }
		//
		// // Get all pheromoes that have the maximum value for their strength.
		// for (int i = target.size() - 1; i >= 0; i--) {
		// ISpaceObject pheromone = (ISpaceObject) target
		// .get(i);
		// if (i == target.size() - 1) {
		// strongestPheromones.add(pheromone);
		// } else if (new Integer(((ISpaceObject) strongestPheromones.get(0))
		// .getProperty("strength").toString()).intValue() == new Integer(
		// (pheromone.getProperty("strength").toString())).intValue()) {
		// strongestPheromones.add(pheromone);
		// } else {
		// break;
		// }
		// }
		//
		// System.out
		// .println("#PheromoneWalkPlan# Strongest Pheromones without current Position!");
		// for (int i = 0; i < strongestPheromones.size(); i++) {
		// System.out.println("#PheromoneWalkPlan#" + i + ": "
		// + ((ISpaceObject) strongestPheromones.get(i)).toString());
		// }
		// return strongestPheromones.size() == 0 ? null
		// : (ISpaceObject) strongestPheromones
		// .get(getRandomNumber(strongestPheromones.size() + 1));

	}

	/**
	 * This method takes a list of (ascending) sorted items. Those items are
	 * taken to get buckets, e.g. every element in a bucket has the same value.
	 * HACK: Only one item per bucket. Todo: all items with the same value in
	 * the same bucket not only one right now.
	 * 
	 * @param sortedList
	 * @return
	 */
	private ArrayList getBuckets(List sortedList) {
		// List that contains buckets
		ArrayList buckets = new ArrayList();
		double sum = 0;

		for (int i = 0; i < sortedList.size(); i++) {
			if (i == 0) {
				buckets.add(sortedList.get(i));
				sum = new Integer(((ISpaceObject) sortedList.get(i))
						.getProperty("strength").toString()).intValue();
			} else {
				int maxVal = new Integer(((ISpaceObject) buckets.get(buckets
						.size() - 1)).getProperty("strength").toString())
						.intValue();
				int currentVal = new Integer(((ISpaceObject) sortedList.get(i))
						.getProperty("strength").toString()).intValue();

				if (maxVal < currentVal) {
					buckets.add(sortedList.get(i));
					sum = sum
							+ new Integer(((ISpaceObject) sortedList.get(i))
									.getProperty("strength").toString())
									.intValue();
				}
			}
		}

		System.out.println("Sorted and indentified buckets. Sum is: " + sum);
		for (int j = 0; j < buckets.size(); j++) {
			int bucketVal = new Integer(((ISpaceObject) buckets.get(j))
					.getProperty("strength").toString()).intValue();
			System.out.println("Bucket No " + j + " : " + bucketVal);
		}

		return getProbabiltiyBorders(buckets, sum);
	}

	/**
	 * Takes a list of buckets with their sum. Computes the probability borders
	 * between the buckets.
	 * 
	 * @param buckets
	 * @param sum
	 * @return
	 */
	private ArrayList getProbabiltiyBorders(ArrayList buckets, double sum) {
		for (int i = 0; i < buckets.size(); i++) {
			if (i == 0) {				
				double bucketVal = new Double(((ISpaceObject) buckets.get(i))
						.getProperty("strength").toString()).doubleValue() 	/ sum;
				((ISpaceObject) buckets.get(i)).setProperty("tmpBucketBorder", new Double(bucketVal));
//				((ISpaceObject) buckets.get(i)).setProperty("strength", new Double(bucketVal));
//				buckets.set(i, new Double(bucketVal));				
				
			} else {
				double bucketValSum = 
					new Double(((ISpaceObject) buckets.get(i-1))
							.getProperty("tmpBucketBorder").toString()).doubleValue();
						
				double bucketVal = new Double(((ISpaceObject) buckets.get(i))
						.getProperty("strength").toString()).doubleValue() / sum; 
//				((ISpaceObject) buckets.get(i)).setProperty("strength", new Double(bucketVal + bucketValSum));
				((ISpaceObject) buckets.get(i)).setProperty("tmpBucketBorder", new Double(bucketVal + bucketValSum));

			}
		}

		System.out
				.println("Sorted and indentified buckets with probablitiy borders.");
		for (int j = 0; j < buckets.size(); j++) {
			double bucketVal = new Double(((ISpaceObject) buckets.get(j))
					.getProperty("tmpBucketBorder").toString()).doubleValue();
			System.out.println("Bucket No/Prob " + j + " : " + bucketVal);
		}
		return buckets;
	}

	/**
	 * Takes a list of buckets with pheromones. The border determines which pheromone is taken.s 
	 */
	private ISpaceObject getStrongestPheromoneRandomly(ArrayList buckets) {
		
		double currentRand = getRandomNumber();
		System.out.println("Random number that determines selected phermone: " + currentRand);
		
		for (int j = 0; j < buckets.size(); j++) {
			double bucketVal = new Double(((ISpaceObject) buckets.get(j))
					.getProperty("tmpBucketBorder").toString()).doubleValue();
			
			if (buckets.size() == 1) {
				System.out.println("Choosen value: " + bucketVal);
				return (ISpaceObject) buckets.get(j);
				
			}
			if (currentRand <= bucketVal || j + 1 == buckets.size()) {
				System.out.println("Choosen value: " + bucketVal);
				return (ISpaceObject) buckets.get(j);
			}
		}
		return null;
	}

	/**
	 * Compute a random number.
	 */
	private int getRandomNumber(int i) {
		// SecureRandom rand = new SecureRandom();
		try {
			return SecureRandom.getInstance("SHA1PRNG").nextInt(i);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		// Compute new position randomly
		// int xvalue = rand.nextInt(size.getXAsInteger());
		// int yvalue = rand.nextInt(size.getYAsInteger());
		// return new Vector2Int(xvalue, yvalue);
	}
	
	/**
	 * Compute a random number.
	 */
	private double getRandomNumber() {
		try {
			return SecureRandom.getInstance("SHA1PRNG").nextDouble();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0.0;
		}
	}
	

}
