package com.sevensins.story;

import com.sevensins.common.CharacterType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for Act 1 story logic.
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>Singleton instance obtained via {@link #getInstance()}.</li>
 *   <li>Player story state is stored in an in-memory map keyed by UUID.
 *       TODO: wire this up to a proper Forge capability / NBT persistence
 *       so that state survives server restarts.</li>
 *   <li>The reunion zone coordinates are hard-coded placeholders.
 *       TODO: move them to ModConfig once the world layout is finalised.</li>
 * </ul>
 *
 * <h2>Quest keys</h2>
 * <ul>
 *   <li>{@code "meliodas_start"} – initial quest for Meliodas</li>
 *   <li>{@code "diane_survive"}  – initial quest for Diane</li>
 *   <li>{@code "reunion"}        – joint quest triggered when the reunion event fires</li>
 * </ul>
 */
public class StoryManager {

    // -----------------------------------------------------------------------
    // Quest key constants
    // -----------------------------------------------------------------------

    public static final String QUEST_MELIODAS_START = "meliodas_start";
    public static final String QUEST_DIANE_SURVIVE  = "diane_survive";
    public static final String QUEST_REUNION        = "reunion";

    // -----------------------------------------------------------------------
    // Reunion zone — placeholder coordinates (XZ square, any Y)
    // TODO: replace with configurable values from ModConfig
    // -----------------------------------------------------------------------

    /** Minimum X coordinate of the reunion trigger zone. */
    private static final double REUNION_ZONE_MIN_X = 0.0;
    /** Minimum Z coordinate of the reunion trigger zone. */
    private static final double REUNION_ZONE_MIN_Z = 0.0;
    /** Maximum X coordinate of the reunion trigger zone. */
    private static final double REUNION_ZONE_MAX_X = 100.0;
    /** Maximum Z coordinate of the reunion trigger zone. */
    private static final double REUNION_ZONE_MAX_Z = 100.0;

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------

    private static final StoryManager INSTANCE = new StoryManager();

    private StoryManager() {}

    /** Returns the singleton {@link StoryManager}. */
    public static StoryManager getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------
    // Player data store
    // NOTE: All story-manager methods are expected to be called from the
    //       Minecraft server tick thread.  ConcurrentHashMap guards against
    //       accidental cross-thread map corruption, but the higher-level
    //       check-then-act sequences (e.g. in tryTriggerReunion) are NOT
    //       atomic and must only be invoked from a single thread at a time.
    // TODO: replace with Forge capability so data is attached to the
    //       ServerPlayer object and persisted via NBT.
    // -----------------------------------------------------------------------

    private final Map<UUID, IPlayerStoryData> playerDataMap = new ConcurrentHashMap<>();

    /**
     * Returns the story data for the given player, creating a fresh
     * {@link PlayerStoryData} if none exists yet.
     */
    public IPlayerStoryData getOrCreateData(ServerPlayer player) {
        return playerDataMap.computeIfAbsent(player.getUUID(), id -> new PlayerStoryData());
    }

    // -----------------------------------------------------------------------
    // Story events
    // -----------------------------------------------------------------------

    /**
     * Called when a player has just selected their character.
     *
     * <ul>
     *   <li>If the player chose {@link CharacterType#MELIODAS}, assigns the
     *       {@value #QUEST_MELIODAS_START} quest.</li>
     *   <li>If the player chose {@link CharacterType#DIANE}, assigns the
     *       {@value #QUEST_DIANE_SURVIVE} quest.</li>
     * </ul>
     *
     * @param player the server-side player who selected a character
     * @param type   the chosen {@link CharacterType}
     */
    public void onCharacterSelected(ServerPlayer player, CharacterType type) {
        if (player == null || type == null || !type.isSelectable()) {
            return;
        }

        IPlayerStoryData data = getOrCreateData(player);
        data.setCharacterType(type);

        switch (type) {
            case MELIODAS -> {
                data.setActiveQuest(QUEST_MELIODAS_START);
                // TODO: send quest-start packet to client (e.g. ModNetwork.sendQuestStart)
            }
            case DIANE -> {
                data.setActiveQuest(QUEST_DIANE_SURVIVE);
                // TODO: send quest-start packet to client (e.g. ModNetwork.sendQuestStart)
            }
            default -> {
                // NONE or future characters — no quest assigned
            }
        }
    }

    /**
     * Attempts to trigger the <em>reunion</em> event between Meliodas and Diane.
     *
     * <p>Conditions that must all be true before the event fires:</p>
     * <ol>
     *   <li>The calling player is Meliodas.</li>
     *   <li>Meliodas is inside the {@link #isInReunionZone(ServerPlayer) reunion zone}.</li>
     *   <li>At least one Diane player exists in the same level and has not yet
     *       joined Meliodas's team ({@code joinedToMeliodasTeam == false}).</li>
     * </ol>
     *
     * <p>When all conditions are met:</p>
     * <ul>
     *   <li>Each eligible Diane's {@code joinedToMeliodasTeam} flag is set to
     *       {@code true}.</li>
     *   <li>Both the Meliodas player and every eligible Diane receive the
     *       {@value #QUEST_REUNION} quest.</li>
     * </ul>
     *
     * @param meliodasPlayer the {@link ServerPlayer} playing as Meliodas
     * @param level          the current server level (used to look up other players)
     */
    public void tryTriggerReunion(ServerPlayer meliodasPlayer, ServerLevel level) {
        if (meliodasPlayer == null || level == null) {
            return;
        }

        // Verify caller is actually playing as Meliodas
        IPlayerStoryData meliodasData = getOrCreateData(meliodasPlayer);
        if (meliodasData.getCharacterType() != CharacterType.MELIODAS) {
            return;
        }

        // Check reunion zone condition
        if (!isInReunionZone(meliodasPlayer)) {
            return;
        }

        // Find all Diane players in the same level who haven't yet joined
        List<ServerPlayer> eligibleDianes = findEligibleDianes(level);
        if (eligibleDianes.isEmpty()) {
            return;
        }

        // Trigger reunion for every eligible Diane
        for (ServerPlayer dianePlayer : eligibleDianes) {
            IPlayerStoryData dianeData = getOrCreateData(dianePlayer);
            dianeData.setJoinedToMeliodasTeam(true);
            dianeData.setActiveQuest(QUEST_REUNION);
            // TODO: send reunion notification/packet to Diane's client
        }

        // Assign reunion quest to Meliodas as well
        meliodasData.setActiveQuest(QUEST_REUNION);
        // TODO: send reunion notification/packet to Meliodas's client

        // TODO: fire a Forge event (ReunionEvent) so other subsystems can react
        //       (e.g. unlock joint abilities, update map markers, play cutscene)
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code player} is currently inside the
     * reunion trigger zone.
     *
     * <p>TODO: extend with Y-bounds and dimension check once world layout is
     * finalised. Consider reading bounds from {@code ModConfig}.</p>
     */
    private boolean isInReunionZone(ServerPlayer player) {
        double x = player.getX();
        double z = player.getZ();
        return x >= REUNION_ZONE_MIN_X && x <= REUNION_ZONE_MAX_X
            && z >= REUNION_ZONE_MIN_Z && z <= REUNION_ZONE_MAX_Z;
    }

    /**
     * Scans all players in {@code level} and returns those whose character
     * type is {@link CharacterType#DIANE} and who have not yet joined
     * Meliodas's team.
     *
     * <p>Works for both solo play (list will be empty — reunion won't fire
     * until a second player joins) and LAN / server play.</p>
     *
     * <p>We deliberately use a direct map lookup ({@code playerDataMap.get})
     * rather than {@link #getOrCreateData} so that we do not create empty
     * story-data entries for players who have not yet selected a character.</p>
     */
    private List<ServerPlayer> findEligibleDianes(ServerLevel level) {
        return level.players().stream()
            .filter(p -> {
                IPlayerStoryData data = playerDataMap.get(p.getUUID());
                return data != null
                    && data.getCharacterType() == CharacterType.DIANE
                    && !data.isJoinedToMeliodasTeam();
            })
            .toList();
    }
}
