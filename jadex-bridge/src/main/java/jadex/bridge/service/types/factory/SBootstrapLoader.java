package jadex.bridge.service.types.factory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jadex.bridge.ClassInfo;
import jadex.bridge.modelinfo.ConfigurationInfo;
import jadex.bridge.modelinfo.IArgument;
import jadex.bridge.modelinfo.IModelInfo;
import jadex.bridge.modelinfo.UnparsedExpression;
import jadex.commons.SReflect;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;

/**
 *  Helper methods for loading component models without a running platform.
 *  Also separates the class loader from the custom platform class loader to support
 *  loading different versions.
 */
public class SBootstrapLoader
{
	//-------- static methods --------
	
	/**
	 *  Load a component model.
	 */
	public static IFuture<IModelInfo>	loadModel(final ClassLoader cl, String model, String factory)
	{
		final Future<IModelInfo> ret = new Future<IModelInfo>();
		try
		{
			Class<?> cfclass = SReflect.classForName(factory, cl);
			Class<?> rlclass = SReflect.classForName(IResultListener.class.getName(), cl);
			Object cfac = cfclass.getConstructor(new Class[]{String.class}).newInstance(new Object[]{"dummy"});
			
			// cfac.loadModel(model, null, null)//rid)
			Object	fut	= SReflect.getMethods(cfclass, "loadModel")[0].invoke(cfac, new Object[]{model, null, null});
			// .addResultListener(new DelegationResultListener<IModelInfo>(ret)
			fut.getClass().getMethod("addResultListener", new Class<?>[]{rlclass}).invoke(fut, new Object[]{
				Proxy.newProxyInstance(cl, new Class<?>[]{rlclass}, new InvocationHandler()
			{
				public Object invoke(Object proxy, Method method, Object[] args)	throws Throwable
				{
					if(method.getName().equals("resultAvailable"))
					{
						final Object	model	= args[0];
						// if(model.getReport()!=null)
						Object	report	= model.getClass().getMethod("getReport").invoke(model);
						if(report!=null)
						{
							// throw new RuntimeException("Error loading model:\n"+model.getReport().getErrorText());
							ret.setException(new RuntimeException("Error loading model:\n"+report.getClass().getMethod("getErrorText").invoke(report)));
						}
						else
						{
							// Wrapper for model info from different class loaders.
							ret.setResult((IModelInfo)Proxy.newProxyInstance(SBootstrapLoader.class.getClassLoader(),
								new Class<?>[]{IModelInfo.class}, new InvocationHandler()
							{
								public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
								{
									Object	ret	= model.getClass().getMethod(method.getName(), method.getParameterTypes())
										.invoke(model, args);
									
									// Wrap arguments also...
									if(method.getName().equals("getArguments"))
									{
										IArgument[]	iargs	= new IArgument[Array.getLength(ret)];
										for(int i=0; i<iargs.length; i++)
										{
											final Object	arg	= Array.get(ret, i);
											iargs[i]	= (IArgument)Proxy.newProxyInstance(SBootstrapLoader.class.getClassLoader(),
												new Class<?>[]{IArgument.class}, new InvocationHandler()
											{
												public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
												{
													Object	ret	= arg.getClass().getMethod(method.getName(), method.getParameterTypes())
														.invoke(arg, args);
													
													// Wrap ClassInfos...
													if(method.getName().equals("getClazz"))
													{
														ret	= new ClassInfo((String)ret.getClass().getMethod("getTypeName").invoke(ret));
													}
													
													return ret;
												}
											});
										}
										ret	= iargs;
									}
									return ret;
								}
							}));
						}
					}
					else // if(method.getName().equals("exceptionOccurred"))
					{
						ret.setException((Exception)args[0]);
					}
					return null;
				}
			})});
		}
		catch(Exception e)
		{
			ret.setException(e);
		}		
		return ret;
	}
	
	/**
	 *  Get an argument expression string from the model.
	 */
	public static String	getArgumentString(String name, IModelInfo model, String configname)
	{
		ConfigurationInfo	config	= configname!=null
			? model.getConfiguration(configname) 
			: model.getConfigurations().length>0 ? model.getConfigurations()[0] : null;
		
		String	ret	= null;
		if(config!=null)
		{
			UnparsedExpression[]	upes	= config.getArguments();
			for(int i=0; ret==null && i<upes.length; i++)
			{
				if(name.equals(upes[i].getName()))
				{
					ret	= upes[i].getValue();
				}
			}
		}
		if(ret==null)
		{
			IArgument	arg	= model.getArgument(name);
			if(arg!=null)
			{
				Object	argval	= arg.getDefaultValue();
				ret	= argval instanceof UnparsedExpression
					? ((UnparsedExpression)argval).getValue() : argval!=null
					? ""+argval : "";
			}
		}
		return ret;
	}
}

