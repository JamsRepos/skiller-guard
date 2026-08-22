package com.skillerguard;

public enum ActivationMode
{
	ALWAYS("Always on"),
	AUTO_LEVEL_3("Level-3 only");

	private final String label;

	ActivationMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
