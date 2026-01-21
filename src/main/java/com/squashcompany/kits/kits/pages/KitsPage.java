package com.squashcompany.kits.kits.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.squashcompany.kits.kits.config.KitsConfig;
import com.squashcompany.kits.kits.manager.KitManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Native Hytale UI page for the Kits selection menu. Uses
 * InteractiveCustomUIPage with UICommandBuilder and UIEventBuilder.
 *
 * Features: - Decorated container with runic header (Hytale style) - Two kit
 * cards (Basic and VIP) - Dynamic item display - Cooldown status indicators -
 * Confirmation popup - Real-time cooldown updates
 */
public class KitsPage extends InteractiveCustomUIPage<KitsPage.KitsPageData> {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    private final PlayerRef playerRef;
    private final Player player;
    private final String playerUuid;

    // UI State
    private boolean popupVisible = false;
    private String lastClaimedKit = null;

    // Real-time cooldown update task
    private ScheduledFuture<?> cooldownUpdateTask;

    public KitsPage(@Nonnull PlayerRef playerRef, @Nonnull Player player) {
        super(playerRef, CustomPageLifetime.CanDismiss, KitsPageData.CODEC);
        this.playerRef = playerRef;
        this.player = player;
        this.playerUuid = playerRef.getUuid().toString();

        // Start real-time cooldown update task (every 1 second)
        startCooldownUpdateTask();
    }

    /**
     * Start the real-time cooldown update task.
     */
    private void startCooldownUpdateTask() {
        cooldownUpdateTask = SCHEDULER.scheduleWithFixedDelay(() -> {
            try {
                updateCooldownsRealtime();
            } catch (Exception e) {
                // Ignore errors during update
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Update cooldown displays in real-time.
     */
    private void updateCooldownsRealtime() {
        UICommandBuilder builder = new UICommandBuilder();
        boolean hasUpdates = false;

        // Update Basic Kit cooldown
        KitsConfig.KitDefinition basicKit = KitsConfig.get().getKit("basic");
        if (basicKit != null) {
            long remaining = KitManager.get().getRemainingCooldown(playerUuid, "basic");
            if (remaining > 0) {
                builder.set("#BasicStatusLabel.Text", "COOLDOWN: " + formatCooldown(remaining));
                builder.set("#BtnClaimBasic.Text", formatCooldown(remaining));
                hasUpdates = true;
            } else {
                builder.set("#BasicStatusLabel.Text", "DISPONIVEL");
                builder.set("#BasicStatusLabel.Style.TextColor", "#7dd87d");
                builder.set("#BtnClaimBasic.Text", "RESGATAR KIT");
                hasUpdates = true;
            }
        }

        // Update VIP Kit cooldown
        KitsConfig.KitDefinition vipKit = KitsConfig.get().getKit("vip");
        if (vipKit != null) {
            long remaining = KitManager.get().getRemainingCooldown(playerUuid, "vip");
            if (remaining > 0) {
                builder.set("#VipStatusLabel.Text", "COOLDOWN: " + formatCooldown(remaining));
                builder.set("#BtnClaimVip.Text", formatCooldown(remaining));
                hasUpdates = true;
            } else {
                builder.set("#VipStatusLabel.Text", "DISPONIVEL");
                builder.set("#VipStatusLabel.Style.TextColor", "#7dd87d");
                builder.set("#BtnClaimVip.Text", "RESGATAR KIT VIP");
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            sendUpdate(builder);
        }
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        // Cancel the cooldown update task when page is closed
        if (cooldownUpdateTask != null && !cooldownUpdateTask.isCancelled()) {
            cooldownUpdateTask.cancel(false);
        }
        super.onDismiss(ref, store);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder,
            @Nonnull Store<EntityStore> store) {

        // Load the UI file
        uiCommandBuilder.append("Pages/KitsPage.ui");

        // Get kit definitions
        KitsConfig.KitDefinition basicKit = KitsConfig.get().getKit("basic");
        KitsConfig.KitDefinition vipKit = KitsConfig.get().getKit("vip");

        // Setup Basic Kit card
        if (basicKit != null) {
            setupKitCard(uiCommandBuilder, "Basic", basicKit);
        }

        // Setup VIP Kit card
        if (vipKit != null) {
            setupKitCard(uiCommandBuilder, "Vip", vipKit);
        }

        // Setup popup state
        uiCommandBuilder.set("#ConfirmPopup.Visible", this.popupVisible);

        // ===== BUTTON EVENT BINDINGS =====
        // Basic kit claim button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BtnClaimBasic",
                EventData.of("Button", "ClaimBasic"),
                false
        );

        // VIP kit claim button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BtnClaimVip",
                EventData.of("Button", "ClaimVip"),
                false
        );

        // Popup close button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BtnPopupClose",
                EventData.of("Button", "ClosePopup"),
                false
        );
    }

    /**
     * Setup a kit card with dynamic data.
     */
    private void setupKitCard(UICommandBuilder builder, String prefix, KitsConfig.KitDefinition kit) {
        // Kit name and description
        builder.set("#" + prefix + "KitName.Text", kit.getDisplayName().toUpperCase());
        builder.set("#" + prefix + "KitDescription.Text", kit.getDescription());

        // Cooldown display
        builder.set("#" + prefix + "CooldownValue.Text", formatCooldownDisplay(kit.getCooldownSeconds()));

        // Status and button state
        long remainingCooldown = KitManager.get().getRemainingCooldown(playerUuid, kit.getId());
        boolean isAvailable = remainingCooldown <= 0;

        if (isAvailable) {
            builder.set("#" + prefix + "StatusLabel.Text", "DISPONIVEL");
            builder.set("#" + prefix + "StatusLabel.Style.TextColor", "#7dd87d");
            builder.set("#BtnClaim" + prefix + ".Text", kit.isVip() ? "RESGATAR KIT VIP" : "RESGATAR KIT");
        } else {
            builder.set("#" + prefix + "StatusLabel.Text", "COOLDOWN: " + formatCooldown(remainingCooldown));
            builder.set("#" + prefix + "StatusLabel.Style.TextColor", "#d87d7d");
            builder.set("#BtnClaim" + prefix + ".Text", formatCooldown(remainingCooldown));
        }

        // Setup item displays with ItemGrid and ItemGridSlot
        List<KitsConfig.KitItem> items = kit.getItems();
        for (int i = 0; i < 5 && i < items.size(); i++) {
            KitsConfig.KitItem item = items.get(i);
            String itemNum = String.valueOf(i + 1);

            // Create ItemGridSlot with ItemStack to show item icon
            ItemStack itemStack = new ItemStack(item.getItemId(), item.getQuantity());
            ItemGridSlot[] slots = new ItemGridSlot[]{new ItemGridSlot(itemStack)};
            builder.set("#" + prefix + "Item" + itemNum + "Grid.Slots", slots);

            // Set quantity label
            builder.set("#" + prefix + "Item" + itemNum + "Qty.Text", "x" + item.getQuantity());
        }

        // Hide unused item slots
        for (int i = items.size(); i < 5; i++) {
            String itemNum = String.valueOf(i + 1);
            builder.set("#" + prefix + "Item" + itemNum + ".Visible", false);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull KitsPageData data) {
        super.handleDataEvent(ref, store, data);

        if (data.button == null) {
            return;
        }

        switch (data.button) {
            case "ClaimBasic" ->
                handleKitClaim("basic");
            case "ClaimVip" ->
                handleKitClaim("vip");
            case "ClosePopup" ->
                closePopup();
        }
    }

    /**
     * Handle kit claim request.
     */
    private void handleKitClaim(String kitId) {
        KitsConfig.KitDefinition kit = KitsConfig.get().getKit(kitId);

        if (kit == null) {
            playerRef.sendMessage(Message.raw("§c[Kits] Kit nao encontrado!"));
            return;
        }

        // Check cooldown
        long remainingCooldown = KitManager.get().getRemainingCooldown(playerUuid, kitId);
        if (remainingCooldown > 0) {
            playerRef.sendMessage(Message.raw("§c[Kits] Aguarde " + formatCooldown(remainingCooldown) + " para usar este kit novamente!"));
            return;
        }

        // Give the kit
        boolean success = KitManager.get().giveKit(player, playerRef, kit);

        if (success) {
            lastClaimedKit = kitId;

            // Show success popup
            UICommandBuilder builder = new UICommandBuilder();
            builder.set("#ConfirmPopup.Visible", true);
            builder.set("#PopupTitle.Text", "KIT RESGATADO!");
            builder.set("#PopupMessage.Text", "Voce recebeu o " + kit.getDisplayName() + " com sucesso!");

            // Update kit card status
            String prefix = kitId.equals("basic") ? "Basic" : "Vip";
            long newCooldown = kit.getCooldownSeconds();
            builder.set("#" + prefix + "StatusLabel.Text", "COOLDOWN: " + formatCooldown(newCooldown));
            builder.set("#" + prefix + "StatusLabel.Style.TextColor", "#d87d7d");
            builder.set("#BtnClaim" + prefix + ".Text", formatCooldown(newCooldown));

            sendUpdate(builder);
            popupVisible = true;

            // Send chat message
            String color = kit.isVip() ? "§6" : "§a";
            playerRef.sendMessage(Message.raw(color + "[Kits] §fVoce recebeu o " + kit.getDisplayName() + "!"));
        } else {
            playerRef.sendMessage(Message.raw("§c[Kits] Erro ao entregar o kit. Verifique seu inventario!"));
        }
    }

    /**
     * Close the confirmation popup.
     */
    private void closePopup() {
        popupVisible = false;
        UICommandBuilder builder = new UICommandBuilder();
        builder.set("#ConfirmPopup.Visible", false);
        sendUpdate(builder);
    }

    /**
     * Get a short display name for an item ID.
     */
    private String getItemShortName(String itemId) {
        return itemId
                .replace("Weapon_Sword_", "Espada ")
                .replace("Tool_Pickaxe_", "Picareta ")
                .replace("Tool_Hatchet_", "Machado ")
                .replace("Plant_Fruit_", "")
                .replace("Furniture_Crude_", "")
                .replace("Iron", "Ferro")
                .replace("Mithril", "Mithril")
                .replace("Apple", "Maca")
                .replace("Torch", "Tocha");
    }

    /**
     * Format cooldown for display (e.g., "5m 30s").
     */
    private String formatCooldown(long seconds) {
        if (seconds <= 0) {
            return "Pronto!";
        } else if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            if (secs == 0) {
                return minutes + "m";
            }
            return minutes + "m " + secs + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            if (minutes == 0) {
                return hours + "h";
            }
            return hours + "h " + minutes + "m";
        }
    }

    /**
     * Format cooldown for kit info display (e.g., "5 minutos").
     */
    private String formatCooldownDisplay(int seconds) {
        if (seconds < 60) {
            return seconds + " segundo" + (seconds != 1 ? "s" : "");
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return minutes + " minuto" + (minutes != 1 ? "s" : "");
        } else {
            int hours = seconds / 3600;
            return hours + " hora" + (hours != 1 ? "s" : "");
        }
    }

    /**
     * Data codec class for UI events.
     */
    public static class KitsPageData {

        private static final String KEY_BUTTON = "Button";

        public static final BuilderCodec<KitsPageData> CODEC = BuilderCodec
                .<KitsPageData>builder(KitsPageData.class, KitsPageData::new)
                .addField(
                        new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                        (data, s) -> data.button = s,
                        data -> data.button
                )
                .build();

        private String button;
    }
}
