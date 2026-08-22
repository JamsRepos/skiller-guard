package com.skillerguard;

public enum XpTrapMode
{
	WARN_ONLY("Warn only"),
	HIDE_AND_WARN("Hide + warn");

	private final String label;

	XpTrapMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
