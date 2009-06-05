package jadex.rules.rulesystem.rules.functions;

import jadex.rules.rulesystem.rules.ILazyValue;
import jadex.rules.state.IOAVState;

import java.util.Collections;
import java.util.Set;

/**
 *  Calculate the modulo between two numbers.
 */
public class Modulo implements IFunction
{
	/**
	 *  Invoke a function and return a value (optional).
	 *  @param paramvalues The parameter values.
	 *  @param state The state.
	 *  @return The function value. 
	 */
	public Object invoke(Object[] paramvalues, IOAVState state)
	{
		Comparable ret = null;
		if(paramvalues.length != 2)
			throw new RuntimeException("Modulo requires two parameters: "+paramvalues);
		
		Object val1 = paramvalues[0] instanceof ILazyValue? ((ILazyValue)paramvalues[0]).getValue(): paramvalues[0]; 
		Object val2 = paramvalues[1] instanceof ILazyValue? ((ILazyValue)paramvalues[1]).getValue(): paramvalues[1]; 
		
		if(!(val1 instanceof Number))
			throw new RuntimeException("Modulo requires Number parameters: "+paramvalues[0]);
		if(!(val2 instanceof Number))
			throw new RuntimeException("Modulo requires Number parameters: "+paramvalues[1]);
		
		Number a = (Number)val1;
		Number b = (Number)val2;
		
		// todo: support long/double/...
		ret = new Long(a.longValue() % b.longValue());
		
		return ret;
	}
	
	/**
	 *  Get the return type of this function.
	 */
	public Class getReturnType()
	{
		return Number.class;
	}

	/**
	 *  Get the set of relevant attribute types.
	 *  @return The relevant attribute types.
	 */
	public Set	getRelevantAttributes()
	{
		return Collections.EMPTY_SET;
	}
	
	/**
	 *  Get the string representation.
	 *  @return The string representation. 
	 */
	public String toString()
	{
		return "%";
	}
	
	/**
	 *  Test for equality.
	 */
	public boolean equals(Object obj)
	{
		return obj instanceof Modulo;
	}
	
	/*public static void main(String[] args)
	{
		double a = 7.2 % 3.5;
		System.out.println(a);
	}*/
	
}
