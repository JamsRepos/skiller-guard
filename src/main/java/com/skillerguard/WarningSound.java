package com.skillerguard;

import net.runelite.api.SoundEffectID;

public enum WarningSound
{
	TOWN_CRIER("Town crier", SoundEffectID.TOWN_CRIER_BELL_DING),
	BELL("Bell", SoundEffectID.TOWN_CRIER_BELL_DONG),
	GE_DING("GE ding", SoundEffectID.GE_ADD_OFFER_DINGALING),
	PRAYER_OUT("Prayer out", SoundEffectID.PRAYER_DEPLETE_TWINKLE),
	CLICK("Click", SoundEffectID.UI_BOOP),
	TELEPORT("Teleport", SoundEffectID.TELEPORT_VWOOP);

	private final String label;
	private final int id;

	WarningSound(String label, int id)
	{
		this.label = label;
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
