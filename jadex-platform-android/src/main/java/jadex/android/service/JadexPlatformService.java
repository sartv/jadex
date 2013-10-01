package jadex.android.service;

import jadex.android.AndroidContextManager;
import jadex.android.IEventReceiver;
import jadex.android.commons.JadexPlatformOptions;
import jadex.android.exception.JadexAndroidPlatformNotStartedError;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.fipa.SFipa;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.message.IMessageService;
import jadex.bridge.service.types.message.MessageType;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Android Service to start/stop Jadex Platforms. Platforms are terminated on
 * destroy.
 */
public class JadexPlatformService extends Service implements JadexPlatformOptions
{

	private JadexPlatformManager jadexPlatformManager;
	
	private String[] platformKernels;
	private String platformOptions;
	private boolean platformAutostart;
	private String platformName;

	private IComponentIdentifier platformId;

	public JadexPlatformService()
	{
		jadexPlatformManager = JadexPlatformManager.getInstance();
		platformKernels = JadexPlatformManager.DEFAULT_KERNELS;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return new JadexPlatformBinder(jadexPlatformManager)
		{

			public IFuture<IComponentIdentifier> startMicroAgent(IComponentIdentifier platformId, String name, Class<?> clazz)
			{
				return startMicroAgent(platformId, name, clazz);
			}

			public IFuture<IComponentIdentifier> startComponent(final IComponentIdentifier platformId, String name, String modelPath)
			{
				return startComponent(platformId, name, modelPath);
			}

			public IFuture<IExternalAccess> startJadexPlatform()
			{
				return startJadexPlatform(JadexPlatformManager.DEFAULT_KERNELS);
			}

			public IFuture<IExternalAccess> startJadexPlatform(String[] kernels)
			{
				return startJadexPlatform(kernels, jadexPlatformManager.getRandomPlatformName());
			}

			public IFuture<IExternalAccess> startJadexPlatform(String[] kernels, String platformId)
			{
				return startJadexPlatform(kernels, platformId, "");
			}

			public IFuture<IExternalAccess> startJadexPlatform(String[] kernels, String platformId, String options)
			{
				return startJadexPlatform(kernels, platformId, options);
			}

			public IComponentIdentifier getPlatformId()
			{
				return getPlatformId();
			}
		};
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Context base = this.getBaseContext();
		if (base != null) {
			// if base is null, this service was not started through android
			AndroidContextManager.getInstance().setAndroidContext(base);
		}
		if (platformAutostart) {
			startPlatform();
		}
		// jadexAndroidContext.addContextChangeListener(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		jadexPlatformManager.shutdownJadexPlatforms();
		Context base = this.getBaseContext();
		if (base != null) {
			// if base is null, this service was not started through android
			AndroidContextManager.getInstance().setAndroidContext(null);
		}
		// jadexAndroidContext.removeContextChangeListener(this);
	}
	
	
	/**
	 * Returns whether the Jadex Platform is or has been started automatically.
	 * @return boolean
	 */
	protected boolean isPlatformAutostart()
	{
		return platformAutostart;
	}

	/**
	 * Sets the autostart parameter for this jadex platform.
	 * If true, the platform will be started during onCreate().
	 * Should be set in constructor, as this is the only method called
	 * before onCreate().
	 * @param autostart
	 */
	protected void setPlatformAutostart(boolean autostart) {
		if (platformId != null || !jadexPlatformManager.isPlatformRunning(platformId)) {
			this.platformAutostart = autostart;
		} else {
			throw new IllegalStateException("Cannot set autostart, platform already running!");
		}
	}
	
	
	/**
	 * Gets the Kernels.
	 * See {@link JadexPlatformManager} constants for available Kernels.
	 * @return String[] of kernels.
	 */
	protected String[] getPlatformKernels()
	{
		return platformKernels;
	}

	/**
	 * Sets the Kernels.
	 * See {@link JadexPlatformManager} constants for available Kernels.
	 * @param kernels
	 */
	protected void setPlatformKernels(String ... kernels) {
		this.platformKernels = kernels;
	}
	
	/**
	 * Returns the platform options of newly created platforms.
	 * @return String[] of options
	 */
	protected String getPlatformOptions()
	{
		return platformOptions;
	}

	/**
	 * Sets platform options.
	 * @param options
	 */
	protected void setPlatformOptions(String options) {
		this.platformOptions = options;
	}
	
	/**
	 * Returns the name which is used to create the next jadex platform.
	 * @return {@link String} name
	 */
	public String getPlatformName()
	{
		return platformName;
	}

	/**
	 * Sets the name of the platform that is started by this activity.
	 * @param name
	 */
	protected void setPlatformName(String name) {
		this.platformName = name;
	}
	
	final protected IFuture<IExternalAccess> startPlatform()
	{
		return startJadexPlatform(platformKernels, platformName, platformOptions);
	}
	
	protected void stopPlatforms()
	{
		jadexPlatformManager.shutdownJadexPlatforms();
	}
	
	protected IExternalAccess getPlatformAccess(IComponentIdentifier platformId) {
		checkIfPlatformIsRunning(platformId, "getPlatformAccess");
		return jadexPlatformManager.getExternalPlatformAccess(platformId);
	}
	
	protected IFuture<IMessageService> getMS()
	{
		return jadexPlatformManager.getMS(platformId);
	}

	protected IFuture<IComponentManagementService> getCMS()
	{
		return jadexPlatformManager.getCMS(platformId);
	}
	
	/**
	 * Start a new micro agent on a given platform.
	 * 
	 * @param platformId
	 *            Identifier of the jadex platform
	 * @param name
	 *            name of the newly created agent
	 * @param clazz
	 *            class of the agent to instantiate
	 * @return ComponentIdentifier of the created agent.
	 * 
	 * @deprecated Use startComponent() instead for all agent types.
	 */
	public IFuture<IComponentIdentifier> startMicroAgent(final IComponentIdentifier platformId, final String name, final Class<?> clazz)
	{
		return startComponent(platformId, name, clazz);
	}
	
	/**
	 * Start a new Component on a given platform with default {@link CreationInfo}.
	 * If available, the belief "androidContext" will be set to <b>this</b>. 
	 * 
	 * @param platformId
	 *            Identifier of the jadex platform
	 * @param name
	 *            name of the newly created agent
	 * @param modelPath
	 *            Path to the bpmn model file of the new agent
	 * @return ComponentIdentifier of the created agent.
	 */
	public IFuture<IComponentIdentifier> startComponent(final IComponentIdentifier platformId, final String name, final Class<?> clazz)
	{
		return startComponent(platformId, name, clazz, new CreationInfo());
	}
	
	/**
	 * Start a new Component on a given platform.
	 * If available, the belief "androidContext" will be set to <b>this</b>. 
	 * 
	 * @param platformId
	 *            Identifier of the jadex platform
	 * @param name
	 *            name of the newly created component
	 * @param clazz
	 *            Class of the new component
	 * @param creationInfo
	 * 			  {@link CreationInfo} to pass to the started Component.
	 * @return ComponentIdentifier of the created agent.
	 */
	public IFuture<IComponentIdentifier> startComponent(final IComponentIdentifier platformId, final String name, final Class<?> clazz, final CreationInfo creationInfo)
	{
		String modelPath = clazz.getName().replaceAll("\\.", "/") + ".class";
		return startComponent(platformId, name, modelPath, creationInfo);
	}
	

	/**
	 * Start a new Component on a given platform with default {@link CreationInfo}.
	 * If available, the belief "androidContext" will be set to <b>this</b>. 
	 * 
	 * @param platformId
	 *            Identifier of the jadex platform
	 * @param name
	 *            name of the newly created component
	 * @param modelPath
	 *            Path to the model file of the new component
	 * @return ComponendIdentifier of the created agent.
	 */
	public IFuture<IComponentIdentifier> startComponent(final IComponentIdentifier platformId, final String name, final String modelPath)
	{
		return startComponent(platformId, name, modelPath, new CreationInfo());
	}
	
	/**
	 * Start a new Component on a given platform.
	 * 
	 * @param platformId
	 *            Identifier of the jadex platform
	 * @param name
	 *            name of the newly created component
	 * @param modelPath
	 *            Path to the model file of the new component
	 * @param creationInfo
	 * 			  {@link CreationInfo} to pass to the started Component.
	 * @return ComponentIdentifier of the created agent.
	 */
	public IFuture<IComponentIdentifier> startComponent(final IComponentIdentifier platformId, final String name, final String modelPath, final CreationInfo creationInfo)
	{
		checkIfPlatformIsRunning(platformId, "startComponent()");
		Map<String, Object> arguments = creationInfo.getArguments();
		if (!arguments.containsKey("androidContext")) {
			arguments.put("androidContext", this);
		}
		
		final Future<IComponentIdentifier> ret = new Future<IComponentIdentifier>();
		jadexPlatformManager.getCMS(platformId)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, IComponentIdentifier>(ret)
		{
			public void customResultAvailable(IComponentManagementService cms)
			{
				cms.createComponent(name, modelPath, creationInfo, null)
					.addResultListener(new DelegationResultListener<IComponentIdentifier>(ret));
			}
		});

		return ret;
	}

	public void registerEventReceiver(String eventName, IEventReceiver<?> rec)
	{
		AndroidContextManager.getInstance().registerEventListener(eventName, rec);
	}

	public void unregisterEventReceiver(String eventName, IEventReceiver<?> rec)
	{
		AndroidContextManager.getInstance().unregisterEventListener(eventName, rec);
	}

	/**
	 * Called right before the platform startup.
	 */
	protected void onPlatformStarting()
	{
	}

	/**
	 * Called right after the platform is started.
	 * 
	 * @param result
	 *            The external access to the platform
	 */
	protected void onPlatformStarted(IExternalAccess platform)
	{
		this.platformId = platform.getComponentIdentifier();
	}

	/**
	 * Sends a FIPA Message to the specified receiver. The Sender is
	 * automatically set to the Platform.
	 * 
	 * @param message
	 * @param receiver
	 * @return Future<Void>
	 */
	protected Future<Void> sendMessage(final Map<String, Object> message, IComponentIdentifier receiver)
	{
		message.put(SFipa.FIPA_MESSAGE_TYPE.getReceiverIdentifier(), receiver);
		IComponentIdentifier cid = jadexPlatformManager.getExternalPlatformAccess(receiver.getRoot()).getComponentIdentifier();
		message.put(SFipa.FIPA_MESSAGE_TYPE.getSenderIdentifier(), cid);
		return sendMessage(message, SFipa.FIPA_MESSAGE_TYPE, receiver);
	}

	/**
	 * Sends a Message to a Component on the Jadex Platform.
	 * 
	 * @param message
	 * @param type
	 * @return Future<Void>
	 */
	protected Future<Void> sendMessage(final Map<String, Object> message, final MessageType type, final IComponentIdentifier receiver)
	{
		final IComponentIdentifier platform = receiver.getRoot();
		checkIfPlatformIsRunning(platform, "sendMessage");

		final Future<Void> ret = new Future<Void>();

		jadexPlatformManager.getMS(platform).addResultListener(new ExceptionDelegationResultListener<IMessageService, Void>(ret)
		{
			public void customResultAvailable(IMessageService ms)
			{
				ms.sendMessage(message, type, jadexPlatformManager.getExternalPlatformAccess(platform).getComponentIdentifier(), null, receiver, null)
						.addResultListener(new DelegationResultListener<Void>(ret));
			}
		});

		return ret;
	}
	
	//---------------- helper ----------------
	
	final private IFuture<IExternalAccess> startJadexPlatform(String[] kernels, String platformId, String options)
	{
		onPlatformStarting();
		IFuture<IExternalAccess> fut = jadexPlatformManager.startJadexPlatform(kernels, platformId, options);
		fut.addResultListener(new DefaultResultListener<IExternalAccess>()
		{
			public void resultAvailable(IExternalAccess result)
			{
				JadexPlatformService.this.onPlatformStarted(result);
			}
			
			public void exceptionOccurred(Exception exception)
			{
				exception.printStackTrace();
//				super.exceptionOccurred(exception);
			}
		});
		return fut;
	}
	
	private void checkIfPlatformIsRunning(final IComponentIdentifier platformId, String caller)
	{
		if (!jadexPlatformManager.isPlatformRunning(platformId))
		{
			throw new JadexAndroidPlatformNotStartedError(caller);
		}
	}

	@Override
	public void attachBaseContext(Context baseContext)
	{
		super.attachBaseContext(baseContext);
	}
}
