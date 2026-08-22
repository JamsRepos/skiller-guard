package com.skillerguard;

import java.time.Instant;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;

@Singleton
@Getter
@Setter
public class GuardState
{
	private String lastWarning;
	private Instant lastWarningAt;
	private String breachMessage;
	private Instant breachUntil;
	private String questWarning;
	private Instant questWarningUntil;
	private boolean questDisclaimer;
	private boolean lampCombatOnly;
	private boolean autoRetaliateOn;
	private boolean npcAttackOptionsOn;
	private boolean playerAttackOptionsOn;
	private boolean passivePrayerGear;

	public void warn(String message)
	{
		this.lastWarning = message;
		this.lastWarningAt = Instant.now();
	}

	public void breach(String message, Instant until)
	{
		this.breachMessage = message;
		this.breachUntil = until;
	}

	public boolean hasActiveBreach()
	{
		return breachMessage != null && breachUntil != null && Instant.now().isBefore(breachUntil);
	}

	public void questAlert(String message, Instant until, boolean disclaimer)
	{
		this.questWarning = message;
		this.questWarningUntil = until;
		this.questDisclaimer = disclaimer;
	}

	public boolean hasActiveQuestWarning()
	{
		return questWarning != null && questWarningUntil != null && Instant.now().isBefore(questWarningUntil);
	}
}
