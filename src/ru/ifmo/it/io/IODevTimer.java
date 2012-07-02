/*
 * $Id$
 */

package ru.ifmo.it.io;

/**
 *
 * @author dima
 */

public class IODevTimer {
	private final IOCtrl ctrl;
	private Thread timer;
	private volatile boolean running = true;

	public IODevTimer(IOCtrl ctrl) {
		this.ctrl = ctrl;
	}

	public void start() {
		timer = new Thread(new Runnable() {
			public void run() {
				int countdown = 0;
				int value;

				while (running) {
					try {
						Thread.sleep(1000);
					} catch (Exception ex) { }

					value = ctrl.getData();

					if (countdown != 0)
						if (countdown <= value) {
							if ((--countdown) == 0)
								ctrl.setFlag();
							else
								continue;
						}

					countdown = value;
				}
			}
		});

		timer.start();
	}

	public void done() {
		running = false;

		try {
			timer.join();
		} catch (Exception ex) {
			System.out.println("Can't join thread: " + ex.getMessage());
		}
	}
}
