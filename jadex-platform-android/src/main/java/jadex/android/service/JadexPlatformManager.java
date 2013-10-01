package jadex.android.service;

import jadex.android.AndroidContextManager;
import jadex.android.commons.Logger;
import jadex.android.exception.JadexAndroidPlatformNotStartedError;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.library.ILibraryService;
import jadex.bridge.service.types.message.IMessageService;
import jadex.bridge.service.types.platform.IJadexPlatformManager;
import jadex.commons.SUtil;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.ThreadSuspendable;
import jadex.commons.transformation.annotations.Classname;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import android.util.Log;

/**
 * Singleton to manage all running jadex platforms and start new ones.
 */
public class JadexPlatformManager implements IJadexPlatformManager
{
	public static final String DEFAULT_OPTIONS = "-logging_level java.util.logging.Level.INFO" 
			+ " -extensions null" 
			//+ " -awareness false"
			+ " -wspublish false" + " -rspublish false" + " -android true" + " -binarymessages true"
			+ " -conf jadex.platform.PlatformAgent" +
			// " -tcptransport false" +
			// " -niotcptransport false" +
			// " -relaytransport true" +
			// " -relayaddress \"http://134.100.11.200:8080/jadex-platform-relay-web/\""
			// +
			" -autoshutdown false" + " -saveonexit true" + " -gui false" + " -chat false";

	// --------- attributes -----------
	private Map<IComponentIdentifier, IExternalAccess> runningPlatforms;

	private Map<String,ClassLoader> platformClassLoaders;
	
	private String defaultAppPath;

	private static JadexPlatformManager instance = new JadexPlatformManager();

	public static JadexPlatformManager getInstance()
	{
		return instance;
	}

	private JadexPlatformManager()
	{
		runningPlatforms = new HashMap<IComponentIdentifier, IExternalAccess>();
		platformClassLoaders = new HashMap<String, ClassLoader>();
		instance = this;
	}

	/**
	 * Returns the Jadex External Platform Access object for a given platform Id
	 * 
	 * @param platformID
	 * @return IExternalAccess
	 */
	public IExternalAccess getExternalPlatformAccess(IComponentIdentifier platformID)
	{
		return runningPlatforms.get(platformID);
	}

	/**
	 * Returns true if given jadex platform is running.
	 * 
	 */
	public boolean isPlatformRunning(IComponentIdentifier platformID)
	{
		return runningPlatforms.containsKey(platformID);
	}

	/**
	 * Sets a classLoader that will be used to load custom Jadex Components.
	 * 
	 * @param cl
	 */
	public void setAppClassLoader(String appPath, ClassLoader cl)
	{
		platformClassLoaders.put(appPath, cl);
		defaultAppPath = appPath;
	}
	
	public ClassLoader getClassLoader(String apkPath)
	{
		if (apkPath == null) {
			apkPath = defaultAppPath;
		}
		return platformClassLoaders.get(apkPath);
	}
	
	/**
	 * Looks up a service and returns it synchronously.
	 * 
	 * @param platformId Id of the platform to use for lookup
	 * @param serviceClazz Class of the service (interface) to find
	 * @return the service
	 */
	public <S> S getsService(IComponentIdentifier platformId, final Class<S> serviceClazz) {
		S s = getService(platformId, serviceClazz).get(new ThreadSuspendable());
		return s;
	}
	
	/**
	 * Looks up a service using RequiredServiceInfo.SCOPE_PLATFORM as scope.
	 * 
	 * @see getsService
	 * 
	 * @param platformId Id of the platform to use for lookup
	 * @param serviceClazz Class of the service (interface) to find
	 * @return Future of the service.
	 */
	public <S> IFuture<S> getService(IComponentIdentifier platformId, final Class<S> serviceClazz) {
		return getService(platformId, serviceClazz, RequiredServiceInfo.SCOPE_PLATFORM);
	}
	
	/**
	 * Looks up a service.
	 * 
	 * @see getsService
	 * 
	 * @param platformId Id of the platform to use for lookup
	 * @param serviceClazz Class of the service (interface) to find
	 * @param scope Search scope. See {@link RequiredServiceInfo} constants.
	 * @return Future of the service.
	 */
	public <S> IFuture<S> getService(IComponentIdentifier platformId, final Class<S> serviceClazz, final String scope) {
		return getExternalPlatformAccess(platformId).scheduleStep(new IComponentStep<S>()
		{
			@Classname("create-component")
			public IFuture<S> execute(IInternalAccess ia)
			{
				Future<S> ret = new Future<S>();
				SServiceProvider
						.getService(ia.getServiceContainer(), serviceClazz, scope)
						.addResultListener(ia.createResultListener(new DelegationResultListener<S>(ret)));

				return ret;
			}
		});
	}
	
	/**
	 * Retrieves the CMS of the Platform with the given ID.
	 * @param platformID Id of the platform
	 * 
	 * @deprecated use getService() or the synchronous getsService() instead. 
	 */
	public IFuture<IComponentManagementService> getCMS(IComponentIdentifier platformID)
	{
		return getService(platformID, IComponentManagementService.class);
	}

	/**
	 * Retrieves the MS of the Platform with the given ID.
	 * @param platformID Id of the platform
	 * 
	 * @deprecated use getService() or the synchronous getsService() instead. 
	 */
	public IFuture<IMessageService> getMS(IComponentIdentifier platformID)
	{
		return getService(platformID, IMessageService.class);
	}

	public IFuture<IExternalAccess> startJadexPlatform(final String[] kernels, final String platformName, final String options)
	{
		return startJadexPlatform(kernels, platformName, options, true);
	}

	public synchronized IFuture<IExternalAccess> startJadexPlatform(final String[] kernels, final String platformName, final String options,
			final boolean chat)
	{
		final Future<IExternalAccess> ret = new Future<IExternalAccess>();
		new Thread(new Runnable()
		{
			public void run()
			{
				final StringBuffer kernelString = new StringBuffer("\"");
				String sep = "";
				for (int i = 0; i < kernels.length; i++)
				{
					kernelString.append(sep);
					kernelString.append(kernels[i]);
					sep = ",";
				}
				kernelString.append("\"");

				final String usedOptions = DEFAULT_OPTIONS + " -kernels " + kernelString.toString() + " -platformname "
						+ (platformName != null ? platformName : getRandomPlatformName()) + (options == null ? "" : " " + options);

				IFuture<IExternalAccess> future = createPlatformWithClassloader((usedOptions).split("\\s+"), this.getClass().getClassLoader());

				future.addResultListener(new IResultListener<IExternalAccess>()
				{
					public void resultAvailable(final IExternalAccess platformAccess)
					{
						// set custom class loader
						IFuture<ILibraryService> service = SServiceProvider.getService(platformAccess.getServiceProvider(), ILibraryService.class);
						service.addResultListener(new DefaultResultListener<ILibraryService>()
						{

							@Override
							public void resultAvailable(ILibraryService result)
							{
								Logger.d("Setting base class loader...");
								if (defaultAppPath !=null) {
									try
									{
										URL url = SUtil.androidUtils().urlFromApkPath(defaultAppPath);
										result.addTopLevelURL(url);
									}
									catch (MalformedURLException e)
									{
										e.printStackTrace();
									}
								}
								
								runningPlatforms.put(platformAccess.getComponentIdentifier(), platformAccess);
								ret.setResult(platformAccess);
							}

							@Override
							public void exceptionOccurred(Exception exception)
							{
								super.exceptionOccurred(exception);
								exception.printStackTrace();
							}
							
							
						});
						
					}

					public void exceptionOccurred(Exception exception)
					{
						ret.setException(exception);
					}
				});
			}
		}).start();

		// if (chat) {
		// ret.addResultListener(new DefaultResultListener<IExternalAccess>()
		// {
		//
		// @Override
		// public void resultAvailable(IExternalAccess result)
		// {
		// JadexChat jadexChat = new JadexChat(result);
		// jadexChat.start();
		// }
		//
		// });
		// }

		return ret;
	}

	@SuppressWarnings("unchecked")
	protected static IFuture<IExternalAccess> createPlatformWithClassloader(String[] options, ClassLoader cl)
	{
		try
		{
			Class<?> starter = cl.loadClass("jadex.base.Starter");
			Method createPlatform = starter.getMethod("createPlatform", String[].class); // =
																							// Starter.createPlatform();
			return (IFuture<IExternalAccess>) createPlatform.invoke(null, (Object) options);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Terminates all running jadex platforms.
	 */
	public synchronized void shutdownJadexPlatforms()
	{
		Log.d("jadex-android", "Shutting down all platforms");
		IComponentIdentifier[] platformIds = runningPlatforms.keySet().toArray(new IComponentIdentifier[runningPlatforms.size()]);
		for (IComponentIdentifier platformId : platformIds)
		{
			shutdownJadexPlatform(platformId);
		}
	}

	/**
	 * Terminates the running jadex platform with the given ID.
	 * 
	 * @param platformID
	 *            Platform to terminate.
	 */
	public synchronized void shutdownJadexPlatform(IComponentIdentifier platformID)
	{
		Log.d("jadex-android", "Starting platform shutdown: " + platformID.toString());
		ThreadSuspendable sus = new ThreadSuspendable();
		// long start = System.currentTimeMillis();
		// long timeout = 4500;
		runningPlatforms.get(platformID).killComponent().get(sus);
		runningPlatforms.remove(platformID);
		Log.d("jadex-android", "Platform shutdown completed: " + platformID.toString());
	}

	public String getRandomPlatformName()
	{
		StringBuilder sb = new StringBuilder(AndroidContextManager.getInstance().getUniqueDeviceName());
		UUID randomUUID = UUID.randomUUID();
		sb.append("_");
		sb.append(randomUUID.toString().substring(0, 3));
		return sb.toString();
	}

}
