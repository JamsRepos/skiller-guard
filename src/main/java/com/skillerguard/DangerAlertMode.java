package com.skillerguard;

public enum DangerAlertMode
{
	BANNERS("On-screen"),
	SOUND("Sound"),
	BANNERS_AND_SOUND("Both");

	private final String label;

	DangerAlertMode(String label)
	{
		this.label = label;
	}

	public boolean showBanners()
	{
		return this != SOUND;
	}

	public boolean playSound()
	{
		return this != BANNERS;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
