/**
 * 
 */
package sodekovs.bikesharing.coordination;

import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.kernelbase.StatelessAbstractInterpreter;

import java.util.ArrayList;
import java.util.List;

import sodekovs.bikesharing.data.clustering.SuperCluster;
import deco4mas.distributed.coordinate.environment.CoordinationSpace;
import deco4mas.distributed.helper.Constants;
import deco4mas.distributed.mechanism.CoordinationInfo;
import deco4mas.distributed.mechanism.CoordinationMechanism;

/**
 * Station cluster based coordination mechanism.
 * 
 * @author Thomas Preisler
 */
public class ClusterMechanism extends CoordinationMechanism {
	
	/** The applications interpreter */
	protected StatelessAbstractInterpreter applicationInterpreter = null;
	
	/** The application environment used for proximity calculation */
	protected ContinuousSpace2D appSpace = null;

	private SuperCluster superCluster = null;

	/** The number of published events */
	protected Integer eventNumber = null;

	public ClusterMechanism(CoordinationSpace space) {
		super(space);

		this.applicationInterpreter = (StatelessAbstractInterpreter) space.getApplicationInternalAccess();
		this.appSpace = (ContinuousSpace2D) applicationInterpreter.getExtension("my2dspace");
		this.eventNumber = 0;
		
		this.superCluster = (SuperCluster) appSpace.getProperty("StationCluster");
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	@Override
	public void perceiveCoordinationEvent(Object obj) {
		CoordinationInfo coordInfo = (CoordinationInfo) obj;
		ClusterStationCoordData coordData = (ClusterStationCoordData) coordInfo.getValueByName(Constants.VALUE);
		if (coordData.getState().equals(ClusterStationCoordData.STATE_POLLING)) {
			coordinationPolling(coordInfo);
		} else if (coordData.getState().equals(ClusterStationCoordData.STATE_REPLY)) {
			coordinationReply(coordInfo);
		} else if (coordData.getState().equals(ClusterStationCoordData.STATE_ALTERNATIVES)) {
			coordinationAlternatives(coordInfo);
		}
	}

	private void coordinationAlternatives(CoordinationInfo coordInfo) {
		// TODO Auto-generated method stub
		
	}

	private void coordinationReply(CoordinationInfo coordInfo) {
		// TODO Auto-generated method stub
		
	}

	private void coordinationPolling(CoordinationInfo coordInfo) {
		ClusterStationCoordData coordData = (ClusterStationCoordData) coordInfo.getValueByName(Constants.VALUE);
		List<IComponentDescription> receiver = new ArrayList<IComponentDescription>();
		
		// only poll cluster stations
		List<String> clusterStations = superCluster.getClusterStationIDs(coordData.getSuperStationId());
		for (ISpaceObject spaceObj: appSpace.getSpaceObjectsByType("bikestation")) {
			String stationID = (String) spaceObj.getProperty("stationID");
			if (clusterStations.contains(stationID)) {
				receiver.add(appSpace.getOwner(spaceObj.getId()));
			}
		}
		
		space.publishCoordinationEvent(coordInfo, receiver, getRealisationName(), eventNumber++);
	}
}