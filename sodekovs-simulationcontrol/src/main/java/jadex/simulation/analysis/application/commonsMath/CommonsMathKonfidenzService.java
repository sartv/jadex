package jadex.simulation.analysis.application.commonsMath;

import jadex.bridge.IExternalAccess;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.ThreadSuspendable;
import jadex.simulation.analysis.common.data.parameter.IAMultiValueParameter;
import jadex.simulation.analysis.service.basic.analysis.ABasicAnalysisService;
import jadex.simulation.analysis.service.basic.analysis.ABasicAnalysisSessionService;
import jadex.simulation.analysis.service.continuative.computation.IAConfidenceService;

import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;

public class CommonsMathKonfidenzService extends ABasicAnalysisSessionService implements IAConfidenceService
{
	private TTest ttest = new TTestImpl();

	public CommonsMathKonfidenzService(IExternalAccess access)
	{
		super(access, IAConfidenceService.class, true);
	}

	@Override
	public IFuture computeTTest(IAMultiValueParameter parameter, Double intervallPercent)
	{
		if (parameter.getN() <2 )
		{
			throw new RuntimeException("TTest: n must be >= 2");
		}
		
		double[] values = new double[parameter.getN().intValue()];
		for (int i = 0; i < parameter.getN().intValue(); i++)
		{
			values[i] = parameter.getValues().get(i);
		}
		Double pvalue = 0.0;
		try
		{
			pvalue = 1-ttest.tTest(parameter.getMean()*(1-intervallPercent),values);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new Future(pvalue);
	}

	@Override
	public IFuture testTTest(IAMultiValueParameter parameter, Double intervallPercent, Double alpha)
	{
		if ((Double)computeTTest(parameter,intervallPercent).get(new ThreadSuspendable(this)) >= alpha)
		{
			return new Future(Boolean.TRUE);
		} else
		{
			return new Future(Boolean.FALSE);
		}
	}
	
	

}
