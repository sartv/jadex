package jadex.bridge.service;

import jadex.commons.SReflect;

/**
 *  Contains information for provided service implementation:
 *  - implementation class or
 *  - creation expression or
 *  - implementation forward to other component via binding 
 */
public class ProvidedServiceImplementation
{
	// todo: use UnparsedExpression instead of implementation and expression text?
	
	//-------- attributes --------
	
	/** The implementation class. */
	protected Class implementation;

	/** The creation expression. */
	protected String expression;
		
	/** The binding for forwarding service calls. */
	protected RequiredServiceBinding binding;

	/** The proxy type. */
	protected String proxytype;
	
	//-------- constructors --------
	
	/**
	 *  Create a new service implementation.
	 */
	public ProvidedServiceImplementation()
	{
		// bean constructor.
	}
	
	/**
	 *  Create a new service implementation.
	 */
	public ProvidedServiceImplementation(Class implementation,
		String expression, String proxytype, RequiredServiceBinding binding)
	{
		this.implementation = implementation;
		this.expression = expression;
		this.proxytype = proxytype;
		this.binding = binding;
	}
	
	/**
	 *  Create a new service implementation.
	 */
	public ProvidedServiceImplementation(ProvidedServiceImplementation prov)
	{
		this(prov.getImplementation(), prov.getExpression(), prov.getProxytype(), prov.getBinding()!=null? new RequiredServiceBinding(prov.getBinding()): null);
	}

	//-------- methods --------
	
	/**
	 *  Get the implementation.
	 *  @return The implementation.
	 */
	public Class getImplementation()
	{
		return implementation;
	}

	/**
	 *  Set the implementation.
	 *  @param implementation The implementation to set.
	 */
	public void setImplementation(Class implementation)
	{
		this.implementation = implementation;
	}

	/**
	 *  Get the expression.
	 *  @return The expression.
	 */
	public String getExpression()
	{
		return expression;
	}

	/**
	 *  Set the expression.
	 *  @param expression The expression to set.
	 */
	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	/**
	 *  Get the proxy type.
	 *  @return The proxy type.
	 */
	public String getProxytype()
	{
		return proxytype;
	}

	/**
	 *  Set the proxy type.
	 *  @param proxytype The proxy type to set.
	 */
	public void	setProxytype(String proxytype)
	{
		this.proxytype	= proxytype;
	}

	/**
	 *  Get the binding.
	 *  @return The binding.
	 */
	public RequiredServiceBinding getBinding()
	{
		return binding;
	}

	/**
	 *  Set the binding.
	 *  @param binding The binding to set.
	 */
	public void setBinding(RequiredServiceBinding binding)
	{
		this.binding = binding;
	}
	
	/**
	 *  Get the string representation.
	 */
	public String toString()
	{
		return implementation!=null? SReflect.getInnerClassName(implementation): 
			expression!=null? expression: binding!=null? 
			binding.getComponentName()!=null? binding.getComponentName(): 
				binding.getComponentType(): "";
	}
}
