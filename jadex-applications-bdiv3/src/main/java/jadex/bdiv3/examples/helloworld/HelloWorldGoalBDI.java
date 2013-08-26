package jadex.bdiv3.examples.helloworld;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.GoalCreationCondition;
import jadex.bdiv3.annotation.GoalParameter;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.runtime.IPlan;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Imports;
import jadex.rules.eca.annotations.Event;

/**
 *  Hello World with goal driven print out.
 *  
 *  class is checked for annotations
 *  goal, plan type declarations from annotations or inline plans 
 *  are added to the agent type and conditions to eca rule system 
 *  class is rewritten to announce belief changes (field accesses and annotated methods)
 */
@Agent
@Imports({"java.util.logging.*"})
//@Properties({@NameValue(name="logging.level", value="Level.INFO")})
@Description("Hello world agent that creates a hello goal.")
public class HelloWorldGoalBDI
{
	/** The bdi agent. */
	@Agent
	protected BDIAgent agent;
	
	/**
	 *  Simple hello world goal.
	 */
	@Goal
	public class HelloGoal
	{
		/** The text. */
		@GoalParameter
		protected String text;
		
		/**
		 *  Create a new goal whenever sayhello belief is changed.
		 */
		@GoalCreationCondition
		public HelloGoal(String text)
		{
			this.text = text;
		}
	}
	
	/**
	 *  The agent body.
	 */
	@AgentBody(keepalive=false)
	public void body()
	{
		agent.dispatchTopLevelGoal(new HelloGoal("Hello BDI agent V3.")).get();
//		System.out.println("body end: "+getClass().getName());
	}
	
	/**
	 *  Plan that prints out goal text and passes.
	 */
	@Plan(trigger=@Trigger(goals=HelloGoal.class))
	protected void printHello(String text, IPlan plan)
	{
		System.out.println(text);
		plan.waitFor(1000).get();
		System.out.println("Good bye.");
	}
}
