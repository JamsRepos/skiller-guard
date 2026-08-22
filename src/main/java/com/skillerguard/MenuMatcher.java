package com.skillerguard;

import java.util.Locale;
import java.util.regex.Pattern;
import net.runelite.client.util.Text;

/**
 * MES-style option/target matching with {@code *} wildcards.
 * NPC name globs like {@code man*} match {@code Man} and {@code Man (bearded)},
 * but not {@code Manager}.
 */
public final class MenuMatcher
{
	private MenuMatcher()
	{
	}

	public static String strip(String s)
	{
		return s == null ? "" : Text.removeTags(s).trim();
	}

	public static boolean isProtectedOption(String option)
	{
		String o = strip(option);
		return o.equalsIgnoreCase("Walk here") || o.equalsIgnoreCase("Cancel");
	}

	public static boolean matches(String pattern, String value)
	{
		if (pattern == null || pattern.isEmpty() || "*".equals(pattern))
		{
			return true;
		}
		return glob(pattern, strip(value));
	}

	/**
	 * {@code man*} matches Man / Man (...) but not Manager or Human.
	 */
	public static boolean matchesNpcTarget(String pattern, String npcName)
	{
		String name = strip(npcName);
		if (pattern == null || pattern.isEmpty() || "*".equals(pattern))
		{
			return true;
		}
		if (!pattern.endsWith("*"))
		{
			return glob(pattern, name);
		}
		String prefix = pattern.substring(0, pattern.length() - 1);
		if (prefix.isEmpty())
		{
			return true;
		}
		if (name.equalsIgnoreCase(prefix))
		{
			return true;
		}
		String lower = name.toLowerCase(Locale.ROOT);
		String p = prefix.toLowerCase(Locale.ROOT);
		return lower.startsWith(p + " ") || lower.startsWith(p + " (");
	}

	static boolean glob(String pattern, String value)
	{
		String p = pattern.toLowerCase(Locale.ROOT);
		String v = value.toLowerCase(Locale.ROOT);
		StringBuilder re = new StringBuilder("^");
		for (int i = 0; i < p.length(); i++)
		{
			char c = p.charAt(i);
			if (c == '*')
			{
				re.append(".*");
			}
			else if (".[]{}()+-^$|\\?".indexOf(c) >= 0)
			{
				re.append('\\').append(c);
			}
			else
			{
				re.append(c);
			}
		}
		re.append('$');
		return Pattern.compile(re.toString()).matcher(v).matches();
	}
}
