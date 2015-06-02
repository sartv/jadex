package jadex.platform.service.dht;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.IService;
import jadex.bridge.service.annotation.Excluded;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceStart;
import jadex.bridge.service.component.IProvidedServicesFeature;
import jadex.bridge.service.component.IRequiredServicesFeature;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.dht.IDistributedKVStoreService;
import jadex.bridge.service.types.dht.IFinger;
import jadex.bridge.service.types.dht.IID;
import jadex.bridge.service.types.dht.IRingApplicationService;
import jadex.bridge.service.types.dht.RingNodeEvent;
import jadex.bridge.service.types.dht.StoreEntry;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.IntermediateDefaultResultListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service that allows storing of key/value pairs in a DHT ring.
 */
@Service
public class DistributedKVStoreService implements IDistributedKVStoreService
{
	/** Map that stores the actual data. Key -> StoreEntry **/
	protected Map<String, StoreEntry>	keyMap;
	
	/** Map that stores the actual data. ID -> StoreEntry **/
	protected Map<IID, StoreEntry>	idMap;
	
	/** The local Ring Node  to access the DHT Ring. **/
	protected IRingApplicationService ring;
	/** The local ID **/
	protected  IID	myId;
	
	/** The local agent access. **/
	@ServiceComponent
	protected IInternalAccess agent;

	/** The logger. **/
	protected  Logger	logger;

	/** Flag that indicates whether this Service is already usable. */
	protected boolean	initialized;
	
	/**
	 * Constructor.
	 */
	public DistributedKVStoreService()
	{
		this.keyMap = new HashMap<String, StoreEntry>();
		this.idMap = new HashMap<IID, StoreEntry>();
		this.logger = Logger.getLogger(this.getClass().getName());
	}
	
	/**
	 * Sets the initialized flag.
	 */
	public void setInitialized(boolean value)
	{
		this.initialized = value;
	}
	
	/**
	 * Gets the initialized flag.
	 */
	public boolean isInitialized()
	{
		return initialized;
	}
	
	@ServiceStart
	public void onServiceStarted() {
//		System.out.println("KVStoreService started");
	}

	/**
	 * Set the local ringNode.
	 * 
	 * @param ring the new ringNode
	 */
	public void setRingService(IRingApplicationService ring)
	{
		this.ring = ring;
		myId = ring.getId().get();
		ISubscriptionIntermediateFuture<RingNodeEvent> subscription = ring.subscribeForEvents();
		IntermediateDefaultResultListener<RingNodeEvent> eventListener = new IntermediateDefaultResultListener<RingNodeEvent>()
		{
			public void intermediateResultAvailable(RingNodeEvent event)
			{
				eventReceived(event);
			}
		};
		subscription.addIntermediateResultListener(eventListener);
	}
	
	/**
	 * Publish a key/value pair in the corresponding node.
	 * 
	 * @param key The Key.
	 * @param value The Value.
	 * @return The ID of the node this key was saved in.
	 */
	public IFuture<IID> publish(final String key, final Object value)
	{
		final Future<IID> ret = new Future<IID>();
		
		ring.findSuccessor(ID.get(key)).addResultListener(new DefaultResultListener<IFinger>()
		{

			@Override
			public void resultAvailable(IFinger result)
			{
				IID nodeId = result.getNodeId();
				// if (providerId.equals(myCid)) {
				if(nodeId.equals(myId))
				{
					// use local access
					storeLocal(key, value).addResultListener(new DelegationResultListener<IID>(ret));
				}
				else
				{
					getStoreService(result).addResultListener(new DefaultResultListener<IDistributedKVStoreService>()
					{
						@Override
						public void resultAvailable(IDistributedKVStoreService result)
						{
							logger.log(Level.INFO, myId + ": Storing key: " + key + "(hash: " + ID.get(key) + ")" + " in: " + result);
							IFuture<IID> publish = result.storeLocal(key, value);
//							IFuture<IID> publish = result.publish(key, value);
							publish.addResultListener(new DelegationResultListener<IID>(ret));
						}
					});
				}
			}
		});
		return ret;
	}
	
	/**
	 * Store a key/value pair in the local map.
	 * 
	 * @param key The key
	 * @param value The value
	 * @return the ID of the local node.
	 */
	public IFuture<IID> storeLocal(String key, Object value) {
		IID hash = ID.get(key);
		return storeLocal(hash, key, value);
	}
	
	/**
	 * Store a key/value pair in the local map.
	 * 
	 * @param hash The hash
	 * @param key The key
	 * @param value The value
	 * @return the ID of the local node.
	 */
	protected IFuture<IID> storeLocal(IID hash, String key, Object value) {
		StoreEntry entry = new StoreEntry(hash, key, value);
		
		if (!isResponsibleFor(hash)) {
			logger.log(Level.WARNING, myId + ": storeLocal called even if i do not feel responsible for: " + hash + ". My successor is " + ring.getSuccessor().get().getNodeId());
		}
		
		logger.log(Level.INFO, myId + ": Storing key: " + key + "(hash: " + hash +")" + " locally.");
		keyMap.put(key, entry);
		idMap.put(hash, entry);
		return ring.getId();
	}

	/**
	 * Lookup a key and return the responsible Node ID.
	 * 
	 * @param key Requested key.
	 * @return IID of the responsible node.
	 */
	public IFuture<IID> lookupResponsibleStore(String key) {
		final Future<IID> ret = new Future<IID>();
//		final IExecutionFeature execFeature = agent.getComponentFeature(IExecutionFeature.class);
		ring.findSuccessor(ID.get(key)).addResultListener(new ExceptionDelegationResultListener<IFinger, IID>(ret)
		{

			@Override
			public void customResultAvailable(IFinger result) {
				ret.setResult(result.getNodeId());
				super.customResultAvailable(result);
			}
			
		});
		
		return ret;
	}
	
	/**
	 * Lookup a key in the ring and return the saved value, if any.
	 * 
	 * @param key Requested key.
	 * @return The retrieved value or null, if none.
	 */
	public IFuture<Object> lookup(String key) {
		return lookup(key, ID.get(key));
	}

	/**
	 * Lookup a key in the ring and return the saved value, if any.
	 * 
	 * @param key Requested key.
	 * @param idHash The hashed key to find the corresponding node.
	 * @return The retrieved value or null, if none.
	 */
	public IFuture<Object> lookup(final String key, final IID idHash)
	{
//		final Future<Object> ret = new Future<Object>();
		if (!initialized) {
			Future<Object> future = new Future<Object>();
			System.out.println("KVStore not initialized!");
			future.setResult(null);
			return future;
		}
		final IExecutionFeature execFeature = agent.getComponentFeature(IExecutionFeature.class);
		return execFeature.scheduleStep(new IComponentStep<Object>()
		{

			@Override
			public IFuture<Object> execute(IInternalAccess ia)
			{
				// TODO Auto-generated method stub
				final Future<Object> fut = new Future<Object>();
				ring.findSuccessor(idHash).addResultListener(new DefaultResultListener<IFinger>()
				{
				
					@Override
					public void resultAvailable(final IFinger finger)
					{
						logger.log(Level.INFO, myId + ": retrieving key: " +key+" (hash: " + idHash + ") from successor: " + finger.getNodeId());
						//	final IComponentIdentifier providerId = result.getSid().getProviderId();
						execFeature.scheduleStep(new IComponentStep<Object>()
						{
						
							@Override
							public IFuture<Object> execute(IInternalAccess ia)
							{
								final Future<Object> ret = new Future<Object>();;
								if(finger.getNodeId().equals(myId))
								{
									// use local access
									
									logger.info(myId + ": retrieving from local map: "  +key+ " (hash: " + idHash +")");
									if(!isResponsibleFor(idHash))
									{
										logger.log(Level.WARNING, myId + ": lookupLocal called even if i do not feel responsible for: " + idHash + ". My successor is " + ring.getSuccessor().get().getNodeId());
									}
									StoreEntry storeEntry = keyMap.get(key);
									if(storeEntry != null)
									{
										ret.setResult(storeEntry.getValue());
									}
									else
									{
										ret.setResult(null);
									}
								}
								else
								{
									// search for remote kvstore service
		//							System.out.println(myId + ": retrieving from remote: " + " (hash: " + idHash +")");
									IFuture<IDistributedKVStoreService> storeService = getStoreService(finger);
		//							IFuture<IDistributedKVStoreService> searchService = agent.getComponentFeature(IRequiredServicesFeature.class).searchService(IDistributedKVStoreService.class,
		//								providerId.getParent());
									storeService.addResultListener(new DefaultResultListener<IDistributedKVStoreService>()
									{
										@Override
										public void resultAvailable(IDistributedKVStoreService result)
										{
											IFuture<Object> value = result.lookup(key, idHash);
											value.addResultListener(new DefaultResultListener<Object>()
											{
											
												@Override
												public void resultAvailable(Object result)
												{
													ret.setResult(result);
												}
											});
										}
									});
								}
								return ret;
							}
						}).addResultListener(new DelegationResultListener<Object>(fut));
					}
				});
				return fut;
			}
		});
	}

	/**
	 * Returns all keys stored in this node.
	 * 
	 * @return Set of Keys.
	 */
	@Override
	public IFuture<Set<String>> getLocalKeySet()
	{
		Set<String> hashSet = keyMap.keySet();
		return new Future<Set<String>>(hashSet);
	}
	
	/**
	 * Returns all IDs stored in this node.
	 * 
	 * @return Set of Keys.
	 */
//	public IFuture<Set<IID>> getLocalIds() {
//		return new Future<Set<IID>>(idMap.keySet());
//	}
	
	/**
	 * Returns all entries that belong to the given node Id
	 * and deletes them on this node.
	 * @param targetNodeId
	 * @return Set of all matching entries.
	 */
	public IFuture<Set<StoreEntry>> moveEntries(IID targetNodeId) {
		// Another node requests entries. I store only entries with: predecessor.id < entry.id <= myId.
		// The target node must have: predecessor.id < target.id < myId, because it has me as its successor.
		// In consequence, i can pass all entries with: myId < entry.id < targetNodeId (because we are in a circle).
		
		HashSet<StoreEntry> result = new HashSet<StoreEntry>();
		
		Iterator<StoreEntry> it = keyMap.values().iterator();

		while(it.hasNext())
		{
			StoreEntry entry = (StoreEntry)it.next();
			if (entry.getIdHash().isInInterval(myId, targetNodeId, false, true)) {
				result.add(entry);
				it.remove();
			}
		}
		
		return new Future<Set<StoreEntry>>(result);
	}

	/**
	 * Checks whether this store service is responsible for saving/retrieving a
	 * key with the given hash value.
	 * 
	 * @param hash
	 * @return true, if this store service is responsible, else false.
	 */
	private boolean isResponsibleFor(IID hash)
	{
		IFinger suc = ring.getSuccessor().get();
		return (suc == null) ? true : (myId.isInInterval(hash, suc.getNodeId(), true, false));
	}

	/**
	 * Returns the local ring node.
	 * 
	 * @return The local ringnode.
	 */
	public IFuture<IRingApplicationService> getRingService()
	{
		return new Future<IRingApplicationService>(ring);
	}
	
	/**
	 * Lookup the storage service for a given finger.
	 * @param finger
	 * @return {@link IDistributedKVStoreService}
	 */
	public IFuture<IDistributedKVStoreService> getStoreService(IFinger finger) {
		IFuture<IDistributedKVStoreService> searchService = SServiceProvider.getService(agent, finger.getSid().getProviderId(), IDistributedKVStoreService.class);
		return searchService;
	}
	
	/**
	 * Called upon events received from the ring service.
	 * @param event
	 */
	protected void eventReceived(RingNodeEvent event)
	{
		switch(event.type)
		{
			case JOIN:
				// move data with id in (predecessor, myId] from successor,
				// so get everything < myId.
				IFinger successor = event.newFinger;
				getStoreService(successor).addResultListener(new DefaultResultListener<IDistributedKVStoreService>()
				{

					@Override
					public void resultAvailable(IDistributedKVStoreService sucStore)
					{
						sucStore.moveEntries(myId).addResultListener(new DefaultResultListener<Set<StoreEntry>>()
						{

							@Override
							public void resultAvailable(Set<StoreEntry> result)
							{
								for(StoreEntry storeEntry : result)
								{
									storeLocal(storeEntry.getIdHash(), storeEntry.getKey(), storeEntry.getValue());
								}
							}
						});
					}
				});
				break;
			case PART:
				break;
			case FINGERTABLE_CHANGE:
				break;
			case PREDECESSOR_CHANGE:
				break;
			default:
				break;
		}
	}
}
