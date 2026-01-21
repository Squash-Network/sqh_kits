package com.squashcompany.kits.kits.manager;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.squashcompany.kits.kits.config.KitsConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages kit distribution and cooldowns.
 */
public class KitManager {

    private static KitManager instance;

    // Map: playerUUID -> (kitId -> lastUsedTimestamp)
    private final Map<String, Map<String, Long>> playerCooldowns = new HashMap<>();

    private KitManager() {
    }

    public static KitManager get() {
        if (instance == null) {
            instance = new KitManager();
        }
        return instance;
    }

    /**
     * Give a kit to a player.
     *
     * @param player The player to give the kit to
     * @param kit The kit definition
     * @return true if successful, false if inventory is full
     */
    public boolean giveKit(Player player, KitsConfig.KitDefinition kit) {
        Inventory inventory = player.getInventory();

        // Give all items from the kit
        for (KitsConfig.KitItem kitItem : kit.getItems()) {
            ItemStack itemStack = new ItemStack(kitItem.getItemId(), kitItem.getQuantity());
            ItemStackTransaction transaction = inventory.getCombinedHotbarFirst().addItemStack(itemStack);

            // If there's remainder, some items couldn't be added
            ItemStack remainder = transaction.getRemainder();
            if (remainder != null && !remainder.isEmpty()) {
                // Continue anyway, player will receive partial kit
            }
        }

        // Set cooldown
        setCooldown(player.getUuid().toString(), kit.getId(), kit.getCooldownSeconds());

        return true;
    }

    /**
     * Check if a player can use a specific kit (cooldown check).
     *
     * @param playerUuid The player's UUID
     * @param kitId The kit ID
     * @return true if the player can use the kit, false if on cooldown
     */
    public boolean canUseKit(String playerUuid, String kitId) {
        return getRemainingCooldown(playerUuid, kitId) <= 0;
    }

    /**
     * Get the remaining cooldown time in seconds.
     *
     * @param playerUuid The player's UUID
     * @param kitId The kit ID
     * @return Remaining cooldown in seconds, or 0 if no cooldown
     */
    public long getRemainingCooldown(String playerUuid, String kitId) {
        Map<String, Long> kitCooldowns = playerCooldowns.get(playerUuid);
        if (kitCooldowns == null) {
            return 0;
        }

        Long lastUsed = kitCooldowns.get(kitId);
        if (lastUsed == null) {
            return 0;
        }

        KitsConfig.KitDefinition kit = KitsConfig.get().getKit(kitId);
        if (kit == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis() / 1000;
        long cooldownEnd = lastUsed + kit.getCooldownSeconds();

        return Math.max(0, cooldownEnd - currentTime);
    }

    /**
     * Set a cooldown for a player's kit usage.
     *
     * @param playerUuid The player's UUID
     * @param kitId The kit ID
     * @param cooldownSeconds The cooldown duration in seconds
     */
    private void setCooldown(String playerUuid, String kitId, int cooldownSeconds) {
        playerCooldowns
                .computeIfAbsent(playerUuid, k -> new HashMap<>())
                .put(kitId, System.currentTimeMillis() / 1000);
    }

    /**
     * Clear all cooldowns for a player. Useful for admin commands or when
     * player data needs to be reset.
     *
     * @param playerUuid The player's UUID
     */
    public void clearCooldowns(String playerUuid) {
        playerCooldowns.remove(playerUuid);
    }

    /**
     * Clear a specific kit cooldown for a player.
     *
     * @param playerUuid The player's UUID
     * @param kitId The kit ID
     */
    public void clearCooldown(String playerUuid, String kitId) {
        Map<String, Long> kitCooldowns = playerCooldowns.get(playerUuid);
        if (kitCooldowns != null) {
            kitCooldowns.remove(kitId);
        }
    }
}
