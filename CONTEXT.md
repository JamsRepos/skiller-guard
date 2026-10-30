# Jam's Skiller Guard

Protects a level-3 skiller's real, permanent account from gaining combat or Prayer XP.

## Language

**Guard**:
The plugin's protection as a whole, which is either active or stood down.
_Avoid_: Skiller Guard (the product name), protection mode

**Exempt world**:
A world whose character is separate from the player's main account (Seasonal, Deadman, Quest Speedrunning), so Guard stands down entirely.
_Avoid_: Ignored world, excluded world

**No-XP PvP area**:
A place where the game forces Attack onto players but player combat grants no real XP (Wilderness/Bounty Hunter, Last Man Standing, Emir's Arena). Guard stays on; only the Player Attack warning is muted.
_Avoid_: Minigame, PvP zone

**XP-toggle minigame**:
A PvP minigame where player combat grants real combat XP unless the player switches it off in-game (Castle Wars, Soul Wars, Clan Wars, TzHaar Fight Pit). Guard stays on, mutes the Player Attack warning, and warns about the XP toggle instead.
_Avoid_: Safe minigame, PvP minigame

**XP toggle**:
The in-game per-minigame setting that disables combat XP gain, reached through a specific NPC or portal.
_Avoid_: XP lock (Guard's own lamp lockout is a different thing)

**Splash weapon**:
A cosmetic weapon with -100 attack and strength bonuses that can never deal damage, so melee attacks with it grant no combat XP (Cursed goblin hammer, Hunting knife, Carrot sword, Festive and Sweet Nutcracker staff). Wielding one mutes only the NPC Attack warning. The '24-carat' sword (gold Carrot sword) is not one: its -10 strength bonus lets it hit.
_Avoid_: Safe weapon, no-XP weapon
