package util;

/**
*	A VERY simple timer and WD handling.
*
*	A little excures in modulo calculation:
*
*		get the difference of to values (on wrap over):
*
*	thats ok:		val-Native.rd(Native.IO_CNT) < 0;
*
*	thats WRONG:	val < Native.rd(Native.IO_CNT);
*/

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

public class Timer {

	private static SysDevice sys = IOFactory.getFactory().getSysDevice();

	private static boolean blink;

	public static int cnt() {

		return sys.cntInt;
	}

	public static int us() {

		return sys.uscntTimer;
	}

	public static boolean timeout(int val) {

		return val-sys.uscntTimer < 0;
	}
	public static int getTimeoutMs(int msOff) {

		return sys.uscntTimer + 1000*msOff;
	}
	public static int getTimeoutSec(int sec) {

		return sys.uscntTimer + 1000*1000*sec;
	}

	public static void wd() {

		if (blink) {
			sys.wd = 1;
			blink = false;
		} else {
			sys.wd = 0;
			blink = true;
		}
	}

/**
*	simple wait val t (no wd!, no next increment).
*/
	// for Amd.progam()
	public static void usleep(int t) {

		int j = sys.uscntTimer;
		j += t;
		while (j-sys.uscntTimer >= 0)
			;
	}

	//
	//	A very simple clock.
	//	Call loop() sometimes to update the clock;
	//
	private static boolean started;
	private static int day, second, s;
	private static int next;
	
	/**
	 * Call this more than once a second to get the second
	 * timer function.
	 */
	public static void loop() {
		
		if (!started) init();
		if (next-sys.uscntTimer < 0) {
			++s; ++second;
			if (second==86400) {
				second = 0;
				++day;
			}
			next += 1000000;
		}
	}
	
	public static int getSec() {
		return s;
	}
	
	/**
	 * A timeout with integer as second counter is
	 * good for intervals shorter than 68 years.
	 * @param val
	 * @return
	 */
	public static boolean secTimeout(int val) {

		return val-s < 0;
	}
	
	private static void init() {
		
		day = second = s = 0;
		next = sys.uscntTimer+1000000;
		started = true;
	}
}
