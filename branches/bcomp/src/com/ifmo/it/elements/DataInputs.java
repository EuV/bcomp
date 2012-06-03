/**
* $Id$
*/

package com.ifmo.it.elements;

public class DataInputs extends DataWidth
{
	public DataInputs(int width, DataSource ... inputs)
	{
		super(width);

		for (DataSource input : inputs)
			if (input instanceof DataHandler)
				((DataHandler)input).addDestination((DataDestination)this);
	}
}
