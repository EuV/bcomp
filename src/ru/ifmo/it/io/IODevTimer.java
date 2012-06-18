/*
 * $Id$
 */

package ru.ifmo.it.io;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dima
 */

public class IODevTimer implements Runnable {
	private IOCtrl ctrl;
	private Thread timer;
	private volatile boolean running = true;

	public IODevTimer(IOCtrl ctrl) {
		this.ctrl = ctrl;

		timer = new Thread(this);
		timer.start();
	}

	public void done() {
		running = false;

		try {
			timer.join();
		} catch (Exception ex) {
			System.out.println("Can't join thread");
		}
	}

	public void run() {
		int countdown = 0;

		while (running) {
			if (countdown != 0) {
				if ((--countdown) == 0) {
					ctrl.setFlag();
					countdown = ctrl.getData();
				}
			} else
				countdown = ctrl.getData();

			try	{
				Thread.sleep(1000);
			} catch (Exception ex) { }
		}
	}
}