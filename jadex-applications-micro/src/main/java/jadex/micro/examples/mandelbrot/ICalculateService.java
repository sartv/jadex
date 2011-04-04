package jadex.micro.examples.mandelbrot;

import jadex.bridge.service.IService;
import jadex.commons.future.IFuture;

/**
 *  Interface for calculating an area of points.
 */
public interface ICalculateService	extends IService
{
	/**
	 *  Calculate colors for an area of points.
	 *  @param data	The area to be calculated.
	 *  @return	A future containing the calculated area.
	 */
	public IFuture calculateArea(AreaData data);
}
