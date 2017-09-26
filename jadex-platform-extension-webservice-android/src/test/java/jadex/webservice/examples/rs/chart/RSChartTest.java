package jadex.webservice.examples.rs.chart;

import static org.junit.Assert.assertNotNull;
import jadex.base.Starter;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.commons.SReflect;
import jadex.commons.future.IFuture;
import jadex.commons.future.ThreadSuspendable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


@RunWith(RobolectricTestRunner.class)
public class RSChartTest
{
	private IExternalAccess extAcc;
	
	@Before
	public void setUp() throws Exception
	{
		new SReflectSub().setIsAndroid(true);
		IFuture<IExternalAccess> fut = Starter.createPlatform(new String[]
		{
				"-gui", "false",
				"-awareness", "false", "-relaytransport", "false", "-tcptransport", "false",
//				"-componentfactory", "jadex.component.ComponentComponentFactory",
//				"-conf", "jadex/platform/Platform.component.xml",
//				"-logging", "true",
//				"-deftimeout", "-1",
				"-component", "jadex/webservice/examples/rs/chart/ChartProvider.component.xml"
		});

		extAcc = fut.get();
	}
	
	@Test
	public void testAccessRestService() throws InterruptedException
	{
		IFuture<IChartService> fut = SServiceProvider.getService(extAcc, IChartService.class, RequiredServiceInfo.SCOPE_PLATFORM);
		IChartService hs = fut.get();
		double[][] data = new double[][] {{30, 50, 20, 90}, {55, 88, 11, 14}};
		byte[] result = hs.getLineChart(250, 100, data, new String[]{"a", "b", "c", "d"} , new Integer[]{0xFF000000, 0xFF0000FF, 0xFF00FFFF, 0xFFFFFF00}).get();
		
		
//		RestTemplate rt = new RestTemplate();
//		HashMap<String, String> params = new HashMap<String,String>();
//		params.put("chs", "250x100");
//		params.put("chd", "t:30.0,50.0,20.0,90.0|55.0,88.0,11.0,14.0");
//		params.put("chco", "000000,0000ff,00ffff,ffff00");
//		params.put("cht", "lc");
//		params.put("chl", "a|b|c|d");
//		BufferedImage result = rt.getForObject("http://chart.googleapis.com/chart?chs={chs}&chd={chd}&chco={chco}&cht={cht}&chl={chl}", BufferedImage.class, params);

		
//		JFrame frame = new JFrame();
//		JLabel label = new JLabel();
//		frame.getContentPane().add(label);
//		label.setIcon(new ImageIcon(result));
//		frame.setSize(300,300);
//		frame.setVisible(true);
//		Thread.sleep(10000);

		assertNotNull(result);
		System.out.println("Response: " + result);
	}
	
	private class SReflectSub extends SReflect {
		public void setIsAndroid(Boolean b) {
			isAndroid = b;
		}
	}
}
