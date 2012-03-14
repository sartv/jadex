/*
 * $class.javaName.java Generated by Protege plugin Beanynizer. This class implements the functionality of RequestFinished. Feel free to change.
 */
package jadex.bdi.examples.blackjack;

import jadex.bridge.fipa.IComponentAction;


/**
 *  Editable Java class for concept <code>RequestFinished</code> of blackjack_beans ontology.
 */
public class RequestFinished implements IComponentAction
{
	//-------- attributes ----------

	/** Attribute for slot gameresult. */
	protected GameResult gameresult;

	//-------- constructors --------

	/** 
	 *  Default Constructor. <br>
	 *  Create a new <code>RequestFinished</code>.
	 */
	public RequestFinished()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}


	/** 
	 *  Clone Constructor. <br>
	 *  Create a new <code>RequestFinished</code>.<br>
	 *  Copy all attributes from <code>proto</code> to this instance.
	 *
	 *  @param proto The prototype instance.
	 */
	public RequestFinished(RequestFinished proto)
	{
	}

	//-------- accessor methods --------

	/**
	 *  Get the gameresult of this RequestFinished.
	 * @return gameresult
	 */
	public GameResult getGameresult()
	{
		return this.gameresult;
	}

	/**
	 *  Set the gameresult of this RequestFinished.
	 * @param gameresult the value to be set
	 */
	public void setGameresult(GameResult gameresult)
	{
		this.gameresult = gameresult;
	}
	
	//-------- object methods -----

	/** 
	 *  Get a string representation of this <code>RequestFinished</code>.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return "RequestFinished(" + ")";
	}

	/** 
	 *  Get a clone of this <code>RequestFinished</code>.
	 *  @return a shalow copy of this instance.
	 */
	public Object clone()
	{
		return new RequestFinished(this);
	}

	/** 
	 *  Test the equality of this <code>RequestFinished</code> 
	 *  and an object <code>obj</code>.
	 *
	 *  @param obj the object this test will be performed with
	 *  @return false if <code>obj</code> is not of <code>RequestFinished</code> class,
	 *          true if all attributes are equal.   
	 */
	public boolean equals(Object obj)
	{
		if(obj instanceof RequestFinished)
		{
//			RequestFinished cmp = (RequestFinished)obj;
			return true;
		}
		return false;
	}
}
