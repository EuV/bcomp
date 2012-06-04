/**
* $Id$
*/

package com.ifmo.it.elements;

public class ValveSource extends Valve
{
	protected DataSource input;

	public ValveSource(DataSource input, DataSource ... ctrls)
	{
		super(input.getWidth(), ctrls);

		this.input = input;
	}

	public void setValue(int ctrl)
	{
		setValue(ctrl, input.getValue());
	}
}
