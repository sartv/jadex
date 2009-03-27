package jadex.rules.parser.conditions.javagrammar;

import jadex.rules.rulesystem.rules.IOperator;
import jadex.rules.rulesystem.rules.functions.IFunction;
import jadex.rules.state.IOAVState;


/**
 *  An operation composes two values.
 */
public class OperationExpression	extends Expression
{
	//-------- constants --------
	
	/** The OR operator (||). */
	public static final IOperator	OPERATOR_OR	= new IOperator()
	{
		public boolean evaluate(IOAVState state, Object val1, Object val2)
		{
			return ((Boolean)val1).booleanValue() || ((Boolean)val2).booleanValue();
		}

		public String toString()
		{
			return "||";
		}
	};
	
	/** The AND operator (&&). */
	public static final IOperator	OPERATOR_AND	= new IOperator()
	{
		public boolean evaluate(IOAVState state, Object val1, Object val2)
		{
			return ((Boolean)val1).booleanValue() && ((Boolean)val2).booleanValue();
		}

		public String toString()
		{
			return "&&";
		}
	};
	
	//-------- attributes --------
	
	/** The left value expression. */
	protected Expression	left;

	/** The right value expression. */
	protected Expression	right;
	
	/** The operator. */
	protected Object	operator;

	//-------- constructors --------
	
	/**
	 *  Create a new operation.
	 */
	public OperationExpression(Expression left, Expression right, IOperator operator)
	{
		this.left	= left;
		this.right	= right;
		this.operator	= operator;
	}
	
	/**
	 *  Create a new operation.
	 */
	public OperationExpression(Expression left, Expression right, IFunction operator)
	{
		this.left	= left;
		this.right	= right;
		this.operator	= operator;
	}
	
	//-------- methods --------
	
	/**
	 *  Get the left value.
	 */
	public Expression	getLeftValue()
	{
		return this.left;
	}
	
	/**
	 *  Get the right value.
	 */
	public Expression	getRightValue()
	{
		return this.right;
	}
	
	/**
	 *  Get the operator.
	 */
	public Object	getOperator()
	{
		return this.operator;
	}
	
	/**
	 *  Get a string representation of this operation.
	 */
	public String	toString()
	{
		StringBuffer	ret	= new StringBuffer();
		ret.append(getLeftValue().toString());
		ret.append(" ");
		ret.append(getOperator());
		ret.append(" ");
		ret.append(getRightValue().toString());
		return ret.toString();
	}

	/**
	 *  Test if this operation is equal to some object.
	 */
	public boolean	equals(Object o)
	{
		return o instanceof OperationExpression
			&& ((OperationExpression)o).getLeftValue().equals(getLeftValue())
			&& ((OperationExpression)o).getRightValue().equals(getRightValue())
			&& ((OperationExpression)o).getOperator().equals(getOperator());
	}
	
	/**
	 *  Get the hash code of this operation.
	 */
	public int	hashCode()
	{
		int	ret	= 31 + getLeftValue().hashCode();
		ret	= 31*ret + getRightValue().hashCode();
		ret	= 31*ret + getOperator().hashCode();
		return ret;
	}
}
