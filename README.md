# Jam's Skiller Guard

RuneLite plugin that protects level-3 skillers from account-breaking XP. It hides dangerous menu options, locks combat skills on lamps, labels named NPCs, and raises a large on-screen alarm if combat XP still slips through.

Do not treat this as a substitute for care. A missed hide or a new game update can still ruin an account.

## How it works

Guard is fail-closed and layered:

1. **Hide** risky menu options on `PostMenuSort` (left-click) and `MenuOpened` (right-click).
2. **Consume** the click if a hidden option still fires.
3. **Lock** Attack / Strength / Defence / Hitpoints / Ranged / Magic / Prayer on XP lamps.
4. **Warn** on combat-XP quest journals and named NPCs (`[SG]` overhead text).
5. **Alarm** if any of those skills actually gain XP while Guard is active.
6. **Update notes** in chat the first time you log in after a new Jam's Skiller Guard version. If you skipped a version (for example two bumps in one Hub PR), each unseen version is listed oldest first.

Walk here, Cancel, and **player** menu entries are never modified.

## Activation

- **Enable Jam's Skiller Guard** — turns the whole plugin on or off (on by default).
- **Always on** — protects you whenever the plugin is enabled (default).
- **Only if you are a level-3 skiller** — turns on automatically when Hitpoints is 10 or less and Attack, Strength, Defence, Ranged, Magic, and Prayer are 3 or less.

## Protections

| Protection | Default | What it does |
| --- | --- | --- |
| Block Prayer XP | On | Removes **Bury** / **Scatter** / **Take** on remains, offering at skiller-reachable altars (house, Chaos, Forthos bone burner, Varlamore, Camdozaal, Woodcutting Guild shrine), and Pyramid Plunder **Open** on a sarcophagus |
| Block combat training | On | Public and house dummy Attack/Hit, firing a dwarf cannon, Blast Furnace pump, barbarian **Use-rod**, barehanded **Harpoon** (Cage/Net stay; Harpoon stays if you are holding a harpoon), house lectern **Study** / tablet buttons, house ranging-game Play, and **every Magic-XP action on the spellbook**. Home Teleport, minigame grouping teleports, and Configure stay. Construction Build/Remove is never touched. Combat-gated methods (Warriors' Guild, MTA, Pest Control, and similar) are left alone. |
| Block NPC misclicks | **Off** | Talk-to / Attack / Pickpocket / Trick-or-treat on Man, Woman, Pirate, Mugger, Rat |
| Watch for XP traps | On | Always hides charging Kharedst's memoirs / Book of the dead at the Old Memorial (10 Magic XP per charge). **Reminisce** (teleport) stays. Warns on Juna, Tamayu, Otto, Elnock, Father Aereck, and the Varrock Museum information clerk (151 kudos includes Prayer XP). Historian Minas is left alone — his antique lamps can go on any skill, and lamp lockout blocks combat. |
| Lock combat skills on lamps | On | Hides those seven skills on the lamp interface |
| Quest warnings | On | Warns when you open a combat-XP quest journal, start that quest, or open it in Quest Helper (sidebar or right-click). The warning is a large on-screen banner, a red chat message, and the warning sound. |
| Warn for started quests | **Off** | Also warns at login if a listed quest is already in progress. Leave off if you have already started one (for example Recipe for Disaster). |
| Combat XP alarm | On | Red breach banner + chat + sound if combat/Prayer XP is gained |
| Warn about dangerous settings | On | Large warning while Auto Retaliate is on, or NPC/player Attack options are not Hidden. Player Attack is not warned on PvP or Deadman worlds. You can choose on-screen, sound, or both, and optionally flash the message. |
| Names above dangerous NPCs | On | `[SG]` one-liners on Tamayu, Juna, Otto Godblessed, Elnock, Father Aereck, and the museum information clerk — not Historian Minas, and not every Man/Woman |

XP traps such as talking to Tamayu show a `[SG]` line when you **right-click** them, instead of filling the chat while you stand nearby. Clicking the risky option still posts a one-off chat warning. You can also hide those options entirely.

Memoirs charging is always hidden while "Watch for XP traps" is on.

## Adding a catalog rule

Edit `SafetyIds` with `ItemID` / `NpcID` / `ObjectID` / `InterfaceID` constants. Identify the clicked entity by ID and `MenuAction`; use option text only to pick Bury vs Drop (or Fire vs Pick-up) on that entity. Spellbook Magic-XP clicks use `InterfaceID.MAGIC_SPELLBOOK`. NPC-type hides must stay in the opt-in NPC pack.

Named overhead NPCs go in `NamedNpcCatalog` (NpcID). Combat-XP quests go in `QuestDenylist` as `Quest` enum values.

## Plugin Hub notes

NPC-type menu hiding is opt-in and off by default. There is no freeform hide box. This plugin does not inject input, does not hide the auto-retaliate widget (that breaks the spec bar), and cannot flip your in-game Controls for you — it only warns until you set attack options to Hidden and auto-retaliate to Off.

## Development

Requires JDK 11+.

```bash
./gradlew test
./gradlew run
```

For Jagex accounts, follow [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

Confirm in-game before treating a build as done. A clean JVM start is not a passing test.

## License

BSD-2-Clause. See [LICENSE](LICENSE).
