package com.squashcompany.kits.kits.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the Kits plugin. Contains kit definitions with items and
 * cooldowns.
 */
public class KitsConfig {

    private static KitsConfig instance;

    // Kit configurations
    private final List<KitDefinition> kits = new ArrayList<>();

    private KitsConfig() {
        loadDefaultKits();
    }

    public static KitsConfig get() {
        if (instance == null) {
            instance = new KitsConfig();
        }
        return instance;
    }

    /**
     * Load default kit configurations. These can be modified or loaded from a
     * config file in the future.
     */
    private void loadDefaultKits() {
        // Basic Kit - Available to all players
        KitDefinition basicKit = new KitDefinition(
                "basic",
                "Kit Basico",
                "Kit inicial com ferramentas de ferro",
                60 * 5, // 5 minutes cooldown (in seconds)
                false, // Not VIP
                "Weapon_Sword_Iron" // Icon item
        );
        basicKit.addItem("Weapon_Sword_Iron", 1);
        basicKit.addItem("Tool_Pickaxe_Iron", 1);
        basicKit.addItem("Tool_Hatchet_Iron", 1);
        basicKit.addItem("Plant_Fruit_Apple", 16);
        basicKit.addItem("Furniture_Crude_Torch", 32);
        kits.add(basicKit);

        // VIP Kit - Work in Progress
        KitDefinition vipKit = new KitDefinition(
                "vip",
                "Kit VIP",
                "Kit exclusivo com equipamentos de mithril",
                60 * 3, // 3 minutes cooldown (in seconds)
                true, // Is VIP
                "Weapon_Sword_Mithril" // Icon item
        );
        vipKit.addItem("Weapon_Sword_Mithril", 1);
        vipKit.addItem("Tool_Pickaxe_Mithril", 1);
        vipKit.addItem("Tool_Hatchet_Mithril", 1);
        vipKit.addItem("Plant_Fruit_Apple", 64);
        vipKit.addItem("Furniture_Crude_Torch", 64);
        kits.add(vipKit);
    }

    public List<KitDefinition> getKits() {
        return kits;
    }

    public KitDefinition getKit(String id) {
        return kits.stream()
                .filter(kit -> kit.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Represents a single kit definition.
     */
    public static class KitDefinition {

        private final String id;
        private final String displayName;
        private final String description;
        private final int cooldownSeconds;
        private final boolean isVip;
        private final String iconItem;
        private final List<KitItem> items = new ArrayList<>();

        public KitDefinition(String id, String displayName, String description, int cooldownSeconds, boolean isVip, String iconItem) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.cooldownSeconds = cooldownSeconds;
            this.isVip = isVip;
            this.iconItem = iconItem;
        }

        public void addItem(String itemId, int quantity) {
            items.add(new KitItem(itemId, quantity));
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public int getCooldownSeconds() {
            return cooldownSeconds;
        }

        public boolean isVip() {
            return isVip;
        }

        public String getIconItem() {
            return iconItem;
        }

        public List<KitItem> getItems() {
            return items;
        }
    }

    /**
     * Represents an item in a kit.
     */
    public static class KitItem {

        private final String itemId;
        private final int quantity;

        public KitItem(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }

        public String getItemId() {
            return itemId;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
