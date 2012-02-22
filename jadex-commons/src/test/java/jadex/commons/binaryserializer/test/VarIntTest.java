package jadex.commons.binaryserializer.test;

import jadex.commons.transformation.binaryserializer.VarInt;
import junit.framework.TestCase;

public class VarIntTest extends TestCase
{
	/** Corner case values. */
	protected static final long[] CORNER_CASES = 
			new long[] {0, 127, 128, 255, 256, 16383, 16384, 2097151, 2097152, 268435455, 268435456,
						34359738367L, 34359738368L, 4398046511103L, 4398046511104L, 562949953421311L, 562949953421312L};
	
	/** Expected encoded length of the corner cases */
	protected static final byte[] EXPECTED_LENGTHS = new byte[] {1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8};
	
	/**
	 * Self-Test.
	 * 
	 * @param args Arguments.
	 */
	public static final void main(String[] args)
	{
		VarIntTest t = new VarIntTest();
		try
		{
			t.testCornerCases();
			t.testOffsetHandling();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testCornerCases() throws Exception
	{
		for (int i = 0; i < CORNER_CASES.length; ++i)
		{
			long val = CORNER_CASES[i];
			byte explngth = EXPECTED_LENGTHS[i];
			
			System.out.print("Encoding " + val + ", encoded length is ");
			byte[] enc = VarInt.encode(val);
			System.out.println(enc.length + " bytes, should be " +
							 explngth + " bytes. ");
			long dec = VarInt.decode(enc);
			System.out.println("Decodes to " + dec + ".");
			
			assertTrue((enc.length == explngth) && (dec == val));
		}
	}
	
	public void testOffsetHandling() throws Exception
	{
		for (int i = 0; i < CORNER_CASES.length; ++i)
		{
			long val = CORNER_CASES[i];
			byte[] encvi = VarInt.encode(val);
			byte[] ba = new byte[5 + encvi.length];
			ba[0] = 42;
			ba[1] = 47;
			ba[2] = -11;
			System.arraycopy(encvi, 0, ba, 3, encvi.length);
			ba[encvi.length + 3] = 23;
			ba[encvi.length + 4] = -120;
			long result = VarInt.decode(ba, 3);
			assertTrue(result == val);
		}
	}
}
