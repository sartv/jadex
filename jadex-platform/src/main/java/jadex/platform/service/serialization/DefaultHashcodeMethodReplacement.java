package jadex.platform.service.serialization;

import java.lang.reflect.Proxy;

import jadex.bridge.component.impl.remotecommands.IMethodReplacement;


/**
 *  Default replacement code for hashCode() method. 
 */
public class DefaultHashcodeMethodReplacement implements IMethodReplacement
{
	/**
	 *  Invoke the method on the given object with the given args.
	 */
	public Object invoke(Object obj, Object[] args)
	{
		// Todo: hash code of proxy info instead of invocation handler?
		return Integer.valueOf(Proxy.getInvocationHandler(obj).hashCode());
	}
}