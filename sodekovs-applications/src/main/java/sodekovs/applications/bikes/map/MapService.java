package sodekovs.applications.bikes.map;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.BasicService;
import jadex.commons.future.IFuture;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class MapService extends BasicService implements IMapService {
	
	private MapAgent agent = null;

	public MapService(MapAgent agent) {
		super(agent.getServiceProvider().getId(), IMapService.class, null);
		this.agent = agent;
	}

	@Override
	public void registerComponent(final IComponentIdentifier identifier, final GeoPosition position) {
		agent.scheduleStep(new IComponentStep<Void>() {
			
			@Override
			public IFuture<Void> execute(IInternalAccess ia) {
				agent.putPosition(identifier, position);
				return IFuture.DONE;
			}
		});
	}	
}