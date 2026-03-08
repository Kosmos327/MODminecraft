# Seven Deadly Sins RPG — Minecraft Forge 1.20.1 Mod — Technical Report

**Mod ID:** `seven_sins`  
**Root Package:** `com.sevensins`  
**Platform:** Minecraft 1.20.1 · Forge 47.x · Java 17  
**Report Generated:** 2026-03-08

---

## 1. Packages and Folder Structure

```
src/main/java/com/sevensins/
├── SevenSinsMod.java                    ← Mod entry point
│
├── ability/                             ← Ability system (interfaces, manager, types)
│   ├── Ability.java
│   ├── AbilityBalanceData.java
│   ├── AbilityManager.java
│   ├── AbilityType.java
│   ├── AbstractAbility.java
│   ├── CooldownManager.java
│   ├── IAbility.java
│   ├── PassiveAbilityManager.java
│   ├── UltimateAbilityManager.java
│   └── impl/                           ← Concrete ability implementations
│       ├── DemonModeAbility.java
│       ├── DianeEarthSmashAbility.java
│       ├── MeliodasFullCounterAbility.java
│       ├── TheOneAbility.java
│       ├── gluttony/                   ← (misnamed; contains Diane/Envy abilities)
│       │   ├── MerlinArcaneBurstAbility.java
│       │   ├── MerlinInfinityMagicAbility.java
│       │   └── MerlinTeleportAbility.java
│       ├── greed/                      ← Ban abilities
│       │   ├── BanFoxHuntAbility.java
│       │   ├── BanHunterFestAbility.java
│       │   └── BanSnatchAbility.java
│       ├── lust/                       ← Gowther abilities
│       │   ├── GowtherIllusionBurstAbility.java
│       │   ├── GowtherMemoryRewriteAbility.java
│       │   └── GowtherMindControlAbility.java
│       ├── sloth/                      ← King abilities
│       │   ├── KingGuardianAbility.java
│       │   ├── KingIncreaseAbility.java
│       │   └── KingSpiritSpearAbility.java
│       └── wrath/                      ← Meliodas abilities
│           └── HellBlazeAbility.java
│
├── block/                              ← Custom block logic
│   ├── SacredForgeBlock.java
│   └── SinAltarBlock.java
│
├── boss/                               ← Boss system
│   ├── BossBalanceData.java
│   ├── BossManager.java
│   ├── BossPhase.java
│   └── BossRewardTable.java
│
├── character/                          ← Player character / progression
│   ├── CharacterProgressionManager.java
│   ├── CharacterStats.java
│   ├── CharacterType.java
│   ├── PlayerCharacterData.java
│   ├── capability/                     ← Forge capability wiring
│   │   ├── IPlayerCharacterData.java
│   │   ├── ISinData.java
│   │   ├── ModCapabilities.java
│   │   ├── PlayerCharacterDataImpl.java
│   │   ├── PlayerCharacterDataProvider.java
│   │   ├── SinData.java
│   │   └── SinDataProvider.java
│   └── skilltree/
│       ├── SkillTreeDefinition.java
│       ├── SkillTreeNode.java
│       ├── SkillTreeRegistry.java
│       └── SkillUnlockManager.java
│
├── client/                             ← Client-only code
│   ├── Keybinds.java
│   ├── event/
│   │   ├── ClientEventHandler.java
│   │   └── ModClientEventHandler.java
│   ├── hud/
│   │   ├── BossHealthOverlay.java
│   │   ├── CooldownHudOverlay.java
│   │   ├── ManaHudOverlay.java
│   │   └── SinHudOverlay.java
│   ├── model/
│   │   └── RedDemonModel.java
│   ├── renderer/
│   │   ├── DemonEntityRenderer.java
│   │   ├── MeliodasNpcRenderer.java
│   │   └── RedDemonRenderer.java
│   └── screen/
│       ├── CharacterSelectionScreen.java
│       ├── DialogueScreen.java
│       └── SkillTreeScreen.java
│
├── command/                            ← Server commands
│   ├── DebugCommand.java
│   ├── DungeonCommand.java
│   └── RaidCommand.java
│
├── common/data/
│   └── SinType.java                    ← The 7 sin enum (shared)
│
├── config/
│   ├── BalanceHelper.java
│   └── ModConfig.java
│
├── debug/
│   └── ProgressionDebugHelper.java
│
├── dialogue/                           ← NPC dialogue system
│   ├── DialogueManager.java
│   ├── DialogueNode.java
│   ├── DialogueSession.java
│   ├── DialogueTree.java
│   └── NPCDialogue.java
│
├── effect/
│   └── SinEffect.java
│
├── entity/                             ← Custom mob entities
│   ├── DemonCommanderEntity.java
│   ├── DemonKingEntity.java
│   ├── EstarossaEntity.java
│   ├── GrayDemonEntity.java
│   ├── MeliodasNpcEntity.java
│   ├── MythicRedDemonEntity.java
│   └── RedDemonEntity.java
│
├── event/                              ← Forge event handlers (server)
│   ├── CapabilityEventHandler.java
│   ├── CharacterDataEvents.java
│   ├── CommandRegistrationEvents.java
│   ├── DungeonEvents.java
│   ├── ManaRegenEvents.java
│   ├── PassiveAbilityEvents.java
│   ├── PlayerLoginEvents.java
│   ├── QuestEvents.java
│   ├── SacredTreasureEvents.java
│   └── SinProgressionEvents.java
│
├── item/                               ← Custom items
│   ├── ChastiefolItem.java
│   ├── CrownOfNightItem.java
│   ├── LegendaryArtifactItem.java
│   ├── LostvayneItem.java
│   ├── MagicScrollItem.java
│   ├── RhittaItem.java
│   ├── SacredTreasureData.java
│   ├── SacredTreasureItem.java
│   ├── SacredTreasureUpgradeHelper.java
│   ├── SinEmblemItem.java
│   └── SinFragmentItem.java
│
├── mana/
│   ├── ManaManager.java
│   └── ManaRegenService.java
│
├── network/                            ← Networking / packets
│   ├── DirtySyncTracker.java
│   ├── ModNetwork.java
│   ├── OpenCharacterSelectionPacket.java
│   └── packet/
│       ├── AdvanceDialoguePacket.java
│       ├── SelectCharacterPacket.java
│       ├── SinDataSyncPacket.java
│       ├── SyncBossStatePacket.java
│       ├── SyncCharacterDataPacket.java
│       ├── SyncCooldownPacket.java
│       ├── TriggerDialoguePacket.java
│       ├── UnlockSkillPacket.java
│       └── UseAbilityPacket.java
│
├── quest/                              ← Quest system
│   ├── PlayerQuestData.java
│   ├── Quest.java
│   ├── QuestManager.java
│   ├── QuestObjective.java
│   ├── QuestRegistry.java
│   └── QuestType.java
│
├── registry/                           ← Deferred registries
│   ├── ModBlocks.java
│   ├── ModCreativeTabs.java
│   ├── ModEffects.java
│   ├── ModEntities.java
│   ├── ModItems.java
│   └── ModSounds.java
│
├── server/event/
│   └── ServerEventHandler.java
│
├── story/                              ← Story / campaign system
│   ├── IPlayerStoryData.java
│   ├── PlayerStoryData.java
│   ├── StoryChapter.java
│   ├── StoryFlag.java
│   ├── StoryManager.java
│   └── StoryTriggerService.java
│
├── util/
│   └── PlaytestHelper.java
│
└── world/                              ← World / dungeon / raid
    ├── BossArenaManager.java
    ├── DungeonManager.java
    ├── DungeonRewardTable.java
    ├── DungeonType.java
    ├── NightRaidManager.java
    ├── RaidRewardTable.java
    └── RaidWave.java
```

---

## 2. Implemented Systems

### 2.1 Ability System

**Core classes:** `ability/IAbility.java`, `ability/AbstractAbility.java`, `ability/Ability.java`, `ability/AbilityManager.java`, `ability/CooldownManager.java`, `ability/UltimateAbilityManager.java`, `ability/PassiveAbilityManager.java`

- `IAbility` — contract interface: `getType()`, `getManaCost()`, `getCooldownTicks()`, `activate(ServerPlayer)`.
- `AbstractAbility` — base class storing type, mana cost, and cooldown; concrete abilities extend `Ability` which extends `AbstractAbility`.
- `AbilityManager` — maps `AbilityType` → `IAbility` instance; dispatches `UseAbilityPacket` on key press.
- `CooldownManager` — per-player cooldown tracking, synced to client via `SyncCooldownPacket`.
- `UltimateAbilityManager` — tracks timed ultimate forms (Demon Mode, The One); applies/removes MobEffects on expiry.
- `PassiveAbilityManager` — centralizes passive stat bonuses per `CharacterType`; consulted by `PassiveAbilityEvents`.
- `AbilityBalanceData` — data class holding balance scalars (loaded from `ModConfig`).
- **Skill tree:** `SkillTreeRegistry` → `SkillTreeDefinition` (nodes in insertion order) → `SkillTreeNode` (ability, prerequisite, cost). `SkillUnlockManager` validates prerequisites and deducts skill points.

### 2.2 Entity System

Seven custom mob entities extend `Monster` (bosses) or `NPC`-style (Meliodas):

| Entity class               | Registry name         | Category | HP     | Dmg  | Notes                                  |
|----------------------------|-----------------------|----------|--------|------|----------------------------------------|
| `MeliodasNpcEntity`        | `meliodas_npc`        | MISC     | —      | —    | Mentor NPC, triggers dialogue          |
| `RedDemonEntity`           | `red_demon`           | MONSTER  | 500    | 12   | Chapter 3 boss, Phase 1/2              |
| `MythicRedDemonEntity`     | `mythic_red_demon`    | MONSTER  | 1 500  | 24   | Extends RedDemon; 3× HP, 2× dmg, 3× XP |
| `GrayDemonEntity`          | `gray_demon`          | MONSTER  | 800    | 18   | Chapter 5 boss; Phase 2 at 50% HP     |
| `DemonCommanderEntity`     | `demon_commander`     | MONSTER  | 1 500  | 22   | Chapter 6 boss; P2@70%, ENRAGED@30%, summons minions |
| `EstarossaEntity`          | `estarossa`           | MONSTER  | 3 000  | 28   | Chapter 7 boss; P2@65%, ENRAGED@30%   |
| `DemonKingEntity`          | `demon_king`          | MONSTER  | 10 000 | 40   | Chapter 8 final boss; 4 phases        |

Boss syncing is handled by `SyncBossStatePacket` (server → client, every 10 ticks).

### 2.3 Renderer System

Registered in `ModClientEventHandler` on the MOD event bus.

| Entity                  | Renderer class         | Model                     | Texture                                      |
|-------------------------|------------------------|---------------------------|----------------------------------------------|
| `MeliodasNpcEntity`     | `MeliodasNpcRenderer`  | Vanilla Humanoid (Zombie) | `textures/entity/zombie/zombie.png` (placeholder) |
| `RedDemonEntity`        | `RedDemonRenderer`     | `RedDemonModel`           | `seven_sins:textures/entity/red_demon.png`   |
| `GrayDemonEntity`       | `DemonEntityRenderer`  | Vanilla Humanoid (Zombie) | `textures/entity/zombie/zombie.png` (placeholder) |
| `DemonCommanderEntity`  | `DemonEntityRenderer`  | Vanilla Humanoid (Zombie) | `textures/entity/zombie/zombie.png` (placeholder) |
| `MythicRedDemonEntity`  | `DemonEntityRenderer`  | Vanilla Humanoid (Zombie) | `textures/entity/zombie/zombie.png` (placeholder) |
| `EstarossaEntity`       | `DemonEntityRenderer`  | Vanilla Humanoid (Zombie) | `textures/entity/zombie/zombie.png` (placeholder) |
| `DemonKingEntity`       | `DemonEntityRenderer`  | Vanilla Humanoid (Zombie) | `textures/entity/zombie/zombie.png` (placeholder) |

### 2.4 Model System

Only one fully custom model exists:

- **`RedDemonModel`** (`client/model/RedDemonModel.java`)
  - Auto-generated via ForgeModelGenerator / Blockbench.
  - Texture size: 4096 × 4096.
  - Layer location: `seven_sins:red_demon / main`.
  - Parts: full body rig — legs with hoof armor and shin muscles, arms with claws/spikes/armor, detailed horned head (primary horns with 6 segments, spur horns), segmented tail with tip spikes, back spikes, chest and abs armor.
  - Animation stub present in `setupAnim`; no keyframe animation yet.

All other entities use the vanilla `HumanoidModel` (Zombie layer) as a placeholder.

### 2.5 Story System

**Core classes:** `story/StoryChapter.java`, `story/StoryFlag.java`, `story/StoryManager.java`, `story/StoryTriggerService.java`, `story/PlayerStoryData.java`, `story/IPlayerStoryData.java`

- `StoryChapter` enum maps to integer stage values stored in `PlayerCharacterData.personalStoryStage`.
- `StoryFlag` enum defines named milestone flags stored as strings in `PlayerQuestData.storyFlags`.
- `StoryTriggerService` (singleton) acts as the bridge between the quest system and chapter advancement. It is called by `QuestManager.onQuestCompleted` for each milestone quest.
- `StoryManager` handles NPC reunion logic and zone-based proximity checks; coordinates are hard-coded placeholders (see §10).
- `PlayerStoryData` / `IPlayerStoryData` — standalone story data store; not yet fully wired to the Forge capability/NBT system (see §10).

### 2.6 Quest System

**Core classes:** `quest/Quest.java`, `quest/QuestType.java`, `quest/QuestObjective.java`, `quest/QuestManager.java`, `quest/QuestRegistry.java`, `quest/PlayerQuestData.java`

- `Quest` — immutable record: id, title, description, `QuestType`, targetValue.
- `QuestType` — `TALK`, `KILL`, `COLLECT`, `REACH`, `STORY`, `DUNGEON_CLEAR`.
- `QuestManager` — tracks active quest progress; calls `StoryTriggerService.onQuestCompleted` on finish.
- `QuestRegistry` — static map of all pre-defined quests; throws on duplicate ID registration.
- `PlayerQuestData` — embedded in `PlayerCharacterData`; holds `activeQuestId`, `completedQuestIds`, `questProgress`, `storyFlags`.

### 2.7 Capability / Player Data System

Two Forge capabilities, both registered in `ModCapabilities`:

| Capability constant        | Interface                  | Implementation                    | Provider                        | Key                      |
|----------------------------|----------------------------|-----------------------------------|---------------------------------|--------------------------|
| `PLAYER_CHARACTER_DATA`    | `IPlayerCharacterData`     | `PlayerCharacterDataImpl`         | `PlayerCharacterDataProvider`   | `questData` (NBT)        |
| `SIN_DATA`                 | `ISinData`                 | `SinData`                         | `SinDataProvider`               | —                        |

`PlayerCharacterData` stores: `selectedCharacter`, `level`, `experience`, `mana`, `maxMana`, `skillPoints`, `joinedToMeliodasTeam`, `personalStoryStage`, `unlockedAbilities` (EnumSet), and `questData` (embedded `PlayerQuestData`).

`CharacterDataEvents` handles: `AttachCapabilitiesEvent`, `PlayerEvent.Clone`, `PlayerLoggedInEvent`, `PlayerRespawnEvent`, `PlayerChangedDimensionEvent`, `LivingDeathEvent` — ensuring data persists across death/dimension change.

---

## 3. All Entities Implemented

| # | Entity class               | Registry name         | Type    | Chapter  | Description                                       |
|---|----------------------------|-----------------------|---------|----------|---------------------------------------------------|
| 1 | `MeliodasNpcEntity`        | `meliodas_npc`        | NPC     | All      | Mentor NPC; drives dialogue and story triggers    |
| 2 | `RedDemonEntity`           | `red_demon`           | Boss    | 3        | First boss; HP 500, dmg 12; Phase 1/2             |
| 3 | `MythicRedDemonEntity`     | `mythic_red_demon`    | Boss    | Endgame  | Extends RedDemon; HP 1500, dmg 24; Wave 3 of Night Raid |
| 4 | `GrayDemonEntity`          | `gray_demon`          | Boss    | 5        | HP 800, dmg 18; Phase 2 at 50%; corruption pulse  |
| 5 | `DemonCommanderEntity`     | `demon_commander`     | Boss    | 6        | HP 1500, dmg 22; P2@70%, ENRAGED@30%; summons Zombie minions, roar |
| 6 | `EstarossaEntity`          | `estarossa`           | Boss    | 7        | HP 3000, dmg 28; P2@65%, ENRAGED@30%; corruption aura + retaliation burst |
| 7 | `DemonKingEntity`          | `demon_king`          | Boss    | 8        | HP 10000, dmg 40; 4 phases (P2@70%, P3@45%, ENRAGED@30%, FINAL@15%); shockwave + corruption pulse |

---

## 4. All Abilities and Ability Types

### 4.1 AbilityType Enum — Full List

```
WRATH (Meliodas): HELL_BLAZE, FULL_COUNTER, DEMON_MARK, DEMON_MODE
PRIDE (Escanor):  CRUEL_SUN, SUPERNOVA, THE_ONE
GREED (Ban):      SNATCH, FOX_HUNT, HUNTER_FEST
SLOTH (King):     SPIRIT_SPEAR, GUARDIAN, INCREASE
LUST (Gowther):   MIND_CONTROL, ILLUSION_BURST, MEMORY_REWRITE
ENVY (Diane):     TELEPORT, ARCANE_BURST, INFINITY_MAGIC
GLUTTONY (Merlin):ENERGY_DRAIN, DEVOUR, ABYSS_SHIELD
Legacy / Other:   EARTH_SMASH, INVASION, INFINITY, SUNSHINE
Special:          NONE
```

### 4.2 Implemented Abilities — Detail Table

| Ability Class                   | AbilityType       | Character  | Mana | Cooldown (ticks) | Notes                                                       |
|---------------------------------|-------------------|------------|------|------------------|-------------------------------------------------------------|
| `HellBlazeAbility`              | HELL_BLAZE        | Meliodas   | 20   | 100 (5 s)        | Dark fire blast; dmg 8; ignites target                      |
| `MeliodasFullCounterAbility`    | FULL_COUNTER      | Meliodas   | 20   | 200 (~10 s)      | 3 s parry window; reflects incoming damage back             |
| `DemonModeAbility`              | DEMON_MODE        | Meliodas   | 80   | 2400 (120 s)     | Ultimate; Strength II + Speed II + Regen I for 30 s        |
| `TheOneAbility`                 | THE_ONE           | Escanor    | 100  | 3600 (180 s)     | Ultimate; Strength III + Resistance II for 25 s            |
| `BanSnatchAbility`              | SNATCH            | Ban        | 20   | 120 (6 s)        | Steals hearts; dmg 6                                        |
| `BanFoxHuntAbility`             | FOX_HUNT          | Ban        | 30   | 80 (4 s)         | Rapid multi-hit; dmg 12                                     |
| `BanHunterFestAbility`          | HUNTER_FEST       | Ban        | 50   | 200 (10 s)       | AoE heavy hunt strike; dmg 18                               |
| `KingSpiritSpearAbility`        | SPIRIT_SPEAR      | King       | 25   | 100 (5 s)        | Chastiefol spirit spear projectile; dmg 10                  |
| `KingGuardianAbility`           | GUARDIAN          | King       | 30   | 160 (8 s)        | Summons guardian construct                                  |
| `KingIncreaseAbility`           | INCREASE          | King       | 40   | 200 (10 s)       | Size/power increase buff                                    |
| `GowtherMindControlAbility`     | MIND_CONTROL      | Gowther    | 30   | 120 (6 s)        | Temporarily controls target mob                             |
| `GowtherIllusionBurstAbility`   | ILLUSION_BURST    | Gowther    | 25   | 80 (4 s)         | Illusion burst; dmg 9                                       |
| `GowtherMemoryRewriteAbility`   | MEMORY_REWRITE    | Gowther    | 50   | 200 (10 s)       | Rewrites target memory (resets aggro/confusion)             |
| `MerlinTeleportAbility`         | TELEPORT          | Diane*     | 20   | 60 (3 s)         | Short-range teleport (* see §10 naming issue)               |
| `MerlinArcaneBurstAbility`      | ARCANE_BURST      | Diane*     | 30   | 120 (6 s)        | Arcane damage burst; dmg 11                                 |
| `MerlinInfinityMagicAbility`    | INFINITY_MAGIC    | Diane*     | 70   | 300 (15 s)       | Wide-area magic detonation                                  |
| `DianeEarthSmashAbility`        | EARTH_SMASH       | Diane      | 25   | 160 (~8 s)       | AoE 4-block radius knockup; knock-back + slow               |

*\* These three abilities are in the `impl/gluttony` package under "Merlin" class names, but are mapped to `CharacterType.DIANE` (Envy) in `SkillTreeRegistry`. This is a naming inconsistency. See §11.3.*

### 4.3 Skill Tree Layout per Character

| Character | Root Ability | Tier 2             | Tier 3          | Tier 4 (Ultimate)  |
|-----------|--------------|--------------------|-----------------|--------------------|
| MELIODAS  | HELL_BLAZE   | FULL_COUNTER       | DEMON_MARK      | DEMON_MODE         |
| ESCANOR   | CRUEL_SUN    | SUPERNOVA          | THE_ONE         | —                  |
| BAN       | SNATCH       | FOX_HUNT           | HUNTER_FEST     | —                  |
| KING      | SPIRIT_SPEAR | GUARDIAN           | INCREASE        | —                  |
| GOWTHER   | MIND_CONTROL | ILLUSION_BURST     | MEMORY_REWRITE  | —                  |
| DIANE     | TELEPORT     | ARCANE_BURST       | INFINITY_MAGIC  | —                  |
| MERLIN    | ENERGY_DRAIN | DEVOUR             | ABYSS_SHIELD    | —                  |

---

## 5. All Story Chapters

| Stage | Enum constant     | Title                        |
|-------|-------------------|------------------------------|
| 0     | NONE              | (No chapter / not selected)  |
| 1     | AWAKENING         | The Awakening of Sin         |
| 2     | FIRST_DEMONS      | The First Demons             |
| 3     | RED_DEMON         | The Red Demon                |
| 4     | DEMON_CAVE        | Into the Demon Cave          |
| 5     | GRAY_DEMON        | The Gray Demon               |
| 6     | DEMON_COMMANDER   | The Demon Commander          |
| 7     | ESTAROSSA         | Estarossa                    |
| 8     | DEMON_KING        | The Demon King               |
| 9     | NIGHT_RAID        | Night Raid (endgame content) |

---

## 6. All Story Flags

| Flag ID                           | Enum constant                  | Trigger / Meaning                                               |
|-----------------------------------|--------------------------------|-----------------------------------------------------------------|
| `awakening_trial_complete`        | AWAKENING_TRIAL_COMPLETE       | Player completes the Trial of Awakening quest                   |
| `talked_to_meliodas`              | TALKED_TO_MELIODAS             | Player first talks to mentor NPC (Meliodas)                     |
| `first_demons_started`            | FIRST_DEMONS_STARTED           | Chapter 2 demon-hunt quest started                              |
| `first_demons_complete`           | FIRST_DEMONS_COMPLETE          | Chapter 2 demon-hunt quest completed                            |
| `red_demon_slain`                 | RED_DEMON_SLAIN                | Player defeats Red Demon boss                                   |
| `demon_cave_started`              | DEMON_CAVE_STARTED             | Player first enters a Demon Cave dungeon                        |
| `demon_cave_cleared`              | DEMON_CAVE_CLEARED             | Player clears a Demon Cave dungeon                              |
| `obtained_lostvayne`              | OBTAINED_LOSTVAYNE             | Player first picks up Lostvayne                                 |
| `obtained_rhitta`                 | OBTAINED_RHITTA                | Player first picks up Rhitta                                    |
| `obtained_chastiefol`             | OBTAINED_CHASTIEFOL            | Player first picks up Chastiefol                                |
| `gray_demon_slain`                | GRAY_DEMON_SLAIN               | Player defeats Gray Demon boss                                  |
| `gray_demon_encountered`          | GRAY_DEMON_ENCOUNTERED         | Gray Demon first spawned near player                            |
| `gray_demon_phase2_seen`          | GRAY_DEMON_PHASE2_SEEN         | Gray Demon first enters Phase 2                                 |
| `demon_commander_slain`           | DEMON_COMMANDER_SLAIN          | Player defeats Demon Commander boss                             |
| `demon_commander_encountered`     | DEMON_COMMANDER_ENCOUNTERED    | Demon Commander first spawned near player                       |
| `demon_commander_phase2_seen`     | DEMON_COMMANDER_PHASE2_SEEN    | Demon Commander first enters Phase 2                            |
| `demon_commander_summons_seen`    | DEMON_COMMANDER_SUMMONS_SEEN   | Demon Commander first summons minions                           |
| `sacred_treasure_upgraded`        | SACRED_TREASURE_UPGRADED       | Player upgrades a Sacred Treasure for the first time            |
| `demon_king_encountered`          | DEMON_KING_ENCOUNTERED         | Player enters range of Demon King                               |
| `demon_king_phase_2_seen`         | DEMON_KING_PHASE_2_SEEN        | Demon King first enters Phase 2                                 |
| `demon_king_phase_3_seen`         | DEMON_KING_PHASE_3_SEEN        | Demon King first enters Phase 3                                 |
| `demon_king_final_phase_seen`     | DEMON_KING_FINAL_PHASE_SEEN    | Demon King first enters Final Phase                             |
| `demon_king_slain`                | DEMON_KING_SLAIN               | Player defeats Demon King                                       |
| `estarossa_encountered`           | ESTAROSSA_ENCOUNTERED          | Estarossa first spawned near player                             |
| `estarossa_phase_2_seen`          | ESTAROSSA_PHASE_2_SEEN         | Estarossa first enters Phase 2                                  |
| `estarossa_enraged_seen`          | ESTAROSSA_ENRAGED_SEEN         | Estarossa first enters Enraged phase                            |
| `estarossa_slain`                 | ESTAROSSA_SLAIN                | Player defeats Estarossa                                        |
| `night_raid_complete`             | NIGHT_RAID_COMPLETE            | Player survives a full Night Demon Raid                         |
| `legendary_artifact_obtained`     | LEGENDARY_ARTIFACT_OBTAINED    | Player first obtains a Legendary Artifact                       |
| `campaign_complete`               | CAMPAIGN_COMPLETE              | Player defeats the Demon King (full campaign finished)          |

---

## 7. All Quests and Quest IDs

| Quest ID                    | Title                     | Type          | Target | Description                                                       |
|-----------------------------|---------------------------|---------------|--------|-------------------------------------------------------------------|
| `awakening_trial`           | Trial of Awakening        | KILL          | 5      | Defeat 5 hostile creatures to awaken your Sin. (Chapter 1)       |
| `first_demon_hunt`          | The First Demons          | KILL          | 3      | Defeat 3 powerful hostile creatures (maxHealth ≥ 20). (Chapter 2)|
| `slay_red_demon`            | The Red Demon             | KILL          | 1      | Defeat the Red Demon. (Chapter 3)                                 |
| `clear_demon_cave`          | Into the Demon Cave       | DUNGEON_CLEAR | 1      | Enter and clear a Demon Cave. (Chapter 4)                         |
| `slay_gray_demon`           | The Gray Demon            | KILL          | 1      | Defeat the Gray Demon. (Chapter 5)                                |
| `slay_demon_commander`      | The Demon Commander       | KILL          | 1      | Defeat the Demon Commander. (Chapter 6)                           |
| `slay_estarossa`            | Estarossa                 | KILL          | 1      | Defeat Estarossa. (Chapter 7)                                     |
| `slay_demon_king`           | The Demon King            | KILL          | 1      | Defeat the Demon King. (Chapter 8)                                |
| `survive_night_raid`        | Night Demon Raid          | DUNGEON_CLEAR | 1      | Survive a full Night Demon Raid (all 3 waves). (Endgame)          |
| `slay_mythic_demon`         | The Mythic Demon          | KILL          | 1      | Defeat the Mythic Red Demon. (Endgame)                            |
| `obtain_legendary_artifact` | Legendary Artifact        | DUNGEON_CLEAR | 1      | Obtain a Legendary Artifact from a Night Raid. (Endgame)          |

### Scaffold-only Quest IDs (constants declared, not registered)
- `obtain_rhitta` — Obtain Divine Axe Rhitta
- `obtain_chastiefol` — Obtain Spirit Spear Chastiefol

---

## 8. Events Handled in the Event System

### Server-Side Event Classes

| Event Class               | Forge Events Subscribed                                         | Purpose                                                       |
|---------------------------|-----------------------------------------------------------------|---------------------------------------------------------------|
| `CharacterDataEvents`     | `AttachCapabilitiesEvent<Entity>`, `PlayerEvent.Clone`, `PlayerEvent.PlayerLoggedInEvent`, `PlayerEvent.PlayerRespawnEvent`, `PlayerEvent.PlayerChangedDimensionEvent`, `LivingDeathEvent` | Capability attach, clone, death/respawn data persistence |
| `QuestEvents`             | `LivingDeathEvent`                                             | Tracks mob kills for KILL-type quests; handles boss kill rewards (MythicRedDemon checked before RedDemon) |
| `SinProgressionEvents`    | `LivingDeathEvent`                                             | Tracks kill XP and sin level-up                               |
| `PassiveAbilityEvents`    | `LivingHurtEvent` (×3, HIGH/NORMAL/LOW priority), `TickEvent.PlayerTickEvent` | Full Counter parry, passive damage bonuses, passive tick effects |
| `ManaRegenEvents`         | `TickEvent.PlayerTickEvent`                                    | Mana regeneration tick; applies ENVY bonus                    |
| `DungeonEvents`           | `LivingDeathEvent` (LOW priority)                              | Tracks mob deaths inside dungeon encounters                    |
| `SacredTreasureEvents`    | `EntityItemPickupEvent`                                        | Sets story flags when Sacred Treasures are first picked up    |
| `PlayerLoginEvents`       | `PlayerEvent.PlayerLoggedInEvent`, `PlayerEvent.PlayerLoggedOutEvent` | Session tracking on login/logout                        |
| `CommandRegistrationEvents` | `RegisterCommandsEvent`                                      | Registers `/sevensins dungeon` and `/sevensins debug` commands |
| `CapabilityEventHandler`  | `AttachCapabilitiesEvent<Entity>`                              | Attaches `SinDataProvider` to player entities                 |

### Client-Side Event Classes

| Event Class               | Events                                                          | Purpose                                                       |
|---------------------------|-----------------------------------------------------------------|---------------------------------------------------------------|
| `ModClientEventHandler`   | `EntityRenderersEvent.RegisterLayerDefinitions`, `EntityRenderersEvent.RegisterRenderers` | Registers model layer + entity renderers                |
| `ClientEventHandler`      | `InputEvent.Key`, `RenderGuiOverlayEvent`, `ClientTickEvent`   | Keybind handling, HUD overlay rendering                       |

---

## 9. Entity Renderers and Models

### Renderers

| Renderer Class          | Extends                                | Entity                    | Model Used          |
|-------------------------|----------------------------------------|---------------------------|---------------------|
| `RedDemonRenderer`      | `MobRenderer`                          | `RedDemonEntity`          | `RedDemonModel`     |
| `MeliodasNpcRenderer`   | `HumanoidMobRenderer`                  | `MeliodasNpcEntity`       | Vanilla Humanoid    |
| `DemonEntityRenderer<T>`| `HumanoidMobRenderer` (generic)        | All other boss entities   | Vanilla Humanoid    |

### Model Layers

| Model Class      | Layer Location                          | Texture                                       | Status           |
|------------------|-----------------------------------------|-----------------------------------------------|------------------|
| `RedDemonModel`  | `seven_sins:red_demon / main`          | `seven_sins:textures/entity/red_demon.png`    | Fully modelled; animation stub only |
| Vanilla Zombie   | `minecraft:zombie` (shared)             | `textures/entity/zombie/zombie.png`           | Placeholder      |

### HUD Overlays

| Class                  | Overlay Type          | Description                                         |
|------------------------|-----------------------|-----------------------------------------------------|
| `BossHealthOverlay`    | `IGuiOverlay`         | Boss HP bar; phase label color: orange=P2, purple=ENRAGED |
| `CooldownHudOverlay`   | `IGuiOverlay`         | Ability cooldown icons                              |
| `ManaHudOverlay`       | `IGuiOverlay`         | Mana bar display                                    |
| `SinHudOverlay`        | `IGuiOverlay`         | Active sin type and level display                   |

---

## 10. All Registries

### `ModEntities` — Entity Types (`DeferredRegister<EntityType<?>>`)

| Registry Name         | Entity Class               | Mob Category | Size (W × H)   |
|-----------------------|----------------------------|--------------|----------------|
| `meliodas_npc`        | `MeliodasNpcEntity`        | MISC         | 0.6 × 1.95     |
| `red_demon`           | `RedDemonEntity`           | MONSTER      | 0.8 × 2.5      |
| `gray_demon`          | `GrayDemonEntity`          | MONSTER      | 0.9 × 2.8      |
| `demon_commander`     | `DemonCommanderEntity`     | MONSTER      | 1.0 × 3.0      |
| `mythic_red_demon`    | `MythicRedDemonEntity`     | MONSTER      | 0.8 × 2.5      |
| `estarossa`           | `EstarossaEntity`          | MONSTER      | 0.9 × 2.8      |
| `demon_king`          | `DemonKingEntity`          | MONSTER      | 1.2 × 3.5      |

### `ModItems` — Items (`DeferredRegister<Item>`)

| Registry Name                  | Class                  | Category            | Rarity  |
|--------------------------------|------------------------|---------------------|---------|
| `sin_fragment`                 | `SinFragmentItem`      | Dungeon reward      | Common  |
| `magic_scroll`                 | `MagicScrollItem`      | Dungeon reward      | Common  |
| `demon_fragment`               | `Item`                 | Legacy              | Common  |
| `sacred_scroll`                | `Item`                 | Legacy              | Common  |
| `wrath_emblem`                 | `SinEmblemItem`        | Sin alignment       | Common  |
| `greed_emblem`                 | `SinEmblemItem`        | Sin alignment       | Common  |
| `sloth_emblem`                 | `SinEmblemItem`        | Sin alignment       | Common  |
| `pride_emblem`                 | `SinEmblemItem`        | Sin alignment       | Common  |
| `lust_emblem`                  | `SinEmblemItem`        | Sin alignment       | Common  |
| `envy_emblem`                  | `SinEmblemItem`        | Sin alignment       | Common  |
| `gluttony_emblem`              | `SinEmblemItem`        | Sin alignment       | Common  |
| `lostvayne`                    | `LostvayneItem`        | Sacred Treasure     | EPIC    |
| `rhitta`                       | `RhittaItem`           | Sacred Treasure     | EPIC    |
| `chastiefol`                   | `ChastiefolItem`       | Sacred Treasure     | EPIC    |
| `crown_of_night`               | `CrownOfNightItem`     | Legendary Artifact  | EPIC    |
| `red_demon_spawn_egg`          | Spawn egg              | Debug/Creative      | Common  |
| `gray_demon_spawn_egg`         | Spawn egg              | Debug/Creative      | Common  |
| `demon_commander_spawn_egg`    | Spawn egg              | Debug/Creative      | Common  |
| `mythic_red_demon_spawn_egg`   | Spawn egg              | Debug/Creative      | Common  |
| `estarossa_spawn_egg`          | Spawn egg              | Debug/Creative      | Common  |
| `demon_king_spawn_egg`         | Spawn egg              | Debug/Creative      | Common  |
| `meliodas_npc_spawn_egg`       | Spawn egg              | Debug/Creative      | Common  |
| `sin_altar` (block item)       | `BlockItem`            | Block               | Common  |
| `sacred_forge` (block item)    | `BlockItem`            | Block               | Common  |

### `ModBlocks` — Blocks (`DeferredRegister<Block>`)

| Registry Name    | Class              | Description                                        |
|------------------|--------------------|----------------------------------------------------|
| `sin_altar`      | `SinAltarBlock`    | Central sin alignment interaction block; light level 7 |
| `sacred_forge`   | `SacredForgeBlock` | Sacred Treasure upgrade station; light level 10    |

### `ModEffects` — Mob Effects (`DeferredRegister<MobEffect>`)

| Registry Name | Sin    | Category   | Color     |
|---------------|--------|------------|-----------|
| `wrath`       | WRATH  | BENEFICIAL | `0xFF4500`|
| `greed`       | GREED  | BENEFICIAL | `0xFFD700`|
| `sloth`       | SLOTH  | HARMFUL    | `0x8B8682`|
| `pride`       | PRIDE  | BENEFICIAL | `0xE8E8E8`|
| `lust`        | LUST   | BENEFICIAL | `0xFF1493`|
| `envy`        | ENVY   | BENEFICIAL | `0x228B22`|
| `gluttony`    | GLUTTONY| NEUTRAL   | `0xFF8C00`|

### `ModSounds` — Sound Events (`DeferredRegister<SoundEvent>`)

| Registry Name    | Trigger                                    |
|------------------|--------------------------------------------|
| `sin_align`      | Player aligns sin using a Sin Emblem       |
| `sin_level_up`   | Player's sin level increases               |
| `altar_activate` | Player interacts with the Sin Altar        |

---

## 11. Incomplete or Placeholder Systems

The following systems are partially implemented, contain placeholders, or have open TODOs:

### 11.1 Renderers / Models — Placeholder Textures
- **All boss entities except RedDemon** use the vanilla zombie skin and humanoid model as explicit placeholders. `DemonEntityRenderer` and `MeliodasNpcRenderer` both reference `textures/entity/zombie/zombie.png` until custom Blockbench assets are added.
- **`RedDemonModel.setupAnim`** has a comment "Generated stub. Add animation logic here." No keyframe animations are wired yet.

### 11.2 Ability Implementations — Missing Abilities
The following `AbilityType` values declared in the enum have **no `impl/` class**:

| AbilityType    | Assigned Character | Status                          |
|----------------|--------------------|---------------------------------|
| `DEMON_MARK`   | Meliodas (Wrath)   | Enum + skill tree node; no impl |
| `CRUEL_SUN`    | Escanor (Pride)    | Enum + skill tree node; no impl |
| `SUPERNOVA`    | Escanor (Pride)    | Enum + skill tree node; no impl |
| `ENERGY_DRAIN` | Merlin (Gluttony)  | Enum + skill tree node; no impl |
| `DEVOUR`       | Merlin (Gluttony)  | Enum + skill tree node; no impl |
| `ABYSS_SHIELD` | Merlin (Gluttony)  | Enum + skill tree node; no impl |
| `INVASION`     | (Legacy)           | Enum only; unassigned           |
| `INFINITY`     | (Legacy)           | Enum only; unassigned           |
| `SUNSHINE`     | (Legacy)           | Enum only; unassigned           |

### 11.3 Ability Package Naming Inconsistency
- `impl/gluttony/` contains classes `MerlinTeleportAbility`, `MerlinArcaneBurstAbility`, `MerlinInfinityMagicAbility`.
- However, in `AbilityType` these are listed under the **ENVY (Diane)** block (`TELEPORT`, `ARCANE_BURST`, `INFINITY_MAGIC`), and in `SkillTreeRegistry` they are mapped to `CharacterType.DIANE`.
- The folder should be `impl/envy/` and the class names should reference Diane, not Merlin.
- Meanwhile, **Merlin (Gluttony)**'s actual abilities (`ENERGY_DRAIN`, `DEVOUR`, `ABYSS_SHIELD`) have no implementation classes at all.

### 11.4 Story / Player Story Data — Incomplete Persistence Wiring
- `PlayerStoryData` has two open `TODO` comments:
  1. *"register this class as a Forge `ICapabilityProvider`"* — it is not yet attached via the Forge capability system.
  2. *"serialise/deserialise fields using `CompoundTag` (NBT)"* — story data is not persisted across server restarts.
- `StoryManager` has multiple `TODO` markers:
  - Reunion zone coordinates are hard-coded (`TODO: move to ModConfig`).
  - No client notification packets sent on reunion events (`TODO: send reunion notification/packet`).
  - Story-start packets not fired (`TODO: send quest-start packet to client`).

### 11.5 Keybind — Reserved Ability Slot
- `Keybinds.java`: Key `V` is commented as *"reserved for ability three (TODO)"* — the third ability slot keybind is not yet functional.

### 11.6 Sacred Treasure Quests — Scaffold Only
- `QuestRegistry` declares `OBTAIN_RHITTA_ID` and `OBTAIN_CHASTIEFOL_ID` as public constants but does **not** register `Quest` objects for them in the static initializer. These are explicitly marked as "scaffold for future story chapters."

### 11.7 Quest Types — Partially Used
- `QuestType.TALK`, `COLLECT`, `REACH`, `STORY` are defined in the enum but **no registered quests use them**. All active quests use only `KILL` or `DUNGEON_CLEAR`.

### 11.8 DungeonType — Single Type Only
- `DungeonType` enum has only `DEMON_CAVE`. The JavaDoc says *"Version 1 ships only DEMON_CAVE. Additional types can be added here."*

### 11.9 BossPhase PHASE_3 / FINAL_PHASE — Limited Use
- `BossPhase` declares `PHASE_3` and `FINAL_PHASE`.
- Only `DemonKingEntity` uses all five phases. All other bosses use at most Phase 1/2/ENRAGED.

### 11.10 Sound Assets — Not Bundled
- `ModSounds` registers three sound events (`sin_align`, `sin_level_up`, `altar_activate`), but the corresponding audio files (`assets/seven_sins/sounds/*.ogg`) and `sounds.json` are not present in the repository. Sound events will silently fail at runtime until assets are added.

---

*End of Technical Report*
