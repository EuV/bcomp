/**
 * $Id$
 */

package ru.ifmo.cs.bcomp;

import ru.ifmo.cs.elements.Register;

public class MicroIP extends Register {
	public MicroIP(int width) {
		super(width);

		super.setValue(1);
	}

	/**
	 * Sets a new value of MicroIP.
	 *
	 * @param value New value for MicroIP. If value is zero, MicroIP incremented
	 */
	public void setValue(int value) {
		super.setValue(value == 0 ? this.value + 1 : value);
	}
}