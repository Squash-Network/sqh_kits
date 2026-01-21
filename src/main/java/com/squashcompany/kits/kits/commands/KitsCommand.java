package com.squashcompany.kits.kits.commands;

import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.GroupBuilder;
import au.ellie.hyui.builders.InterfaceBuilder;
import au.ellie.hyui.builders.PageBuilder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.squashcompany.kits.kits.config.KitsConfig;
import com.squashcompany.kits.kits.manager.KitManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Command /kits - Opens the kits selection menu using HyUI.
 */
public class KitsCommand extends AbstractAsyncCommand {

    public KitsCommand() {
        super("kits", "Abre o menu de selecao de kits");
        this.setPermissionGroup(GameMode.Adventure); // Available to all players
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        var sender = commandContext.sender();
        if (!(sender instanceof Player player)) {
            commandContext.sendMessage(Message.raw("Este comando so pode ser usado por jogadores!"));
            return CompletableFuture.completedFuture(null);
        }

        player.getWorldMapTracker().tick(0);
        Ref<EntityStore> ref = player.getReference();
        if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();

            return CompletableFuture.runAsync(() -> {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef != null) {
                    // Build the kits page
                    PageBuilder page = buildKitsPage(playerRef, player);

                    // Open the page on world thread
                    world.execute(() -> {
                        page.open(playerRef, store);
                    });
                }
            }, world);
        } else {
            commandContext.sendMessage(Message.raw("Voce precisa estar em um mundo para usar este comando!"));
            return CompletableFuture.completedFuture(null);
        }
    }

    private PageBuilder buildKitsPage(PlayerRef playerRef, Player player) {
        String playerUuid = player.getUuid().toString();

        // Get kit info
        KitsConfig.KitDefinition basicKit = KitsConfig.get().getKit("basic");
        KitsConfig.KitDefinition vipKit = KitsConfig.get().getKit("vip");

        // Check cooldowns
        long basicCooldown = KitManager.get().getRemainingCooldown(playerUuid, "basic");
        long vipCooldown = KitManager.get().getRemainingCooldown(playerUuid, "vip");

        boolean basicEnabled = basicCooldown <= 0;
        boolean vipEnabled = vipCooldown <= 0;

        // Build items display HTML
        String basicItemIcons = buildItemIconsHtml(basicKit, "basic");
        String vipItemIcons = buildItemIconsHtml(vipKit, "vip");

        // Build HTML for the kits menu with improved design
        String html = """
            <div class="page-overlay">
                <div class="container" data-hyui-title="Selecao de Kits" style="anchor-width: 760; anchor-height: 560;">
                    <div class="container-contents">
                        
                        <!-- Title -->
                        <div style="anchor-left: 0; anchor-right: 0; anchor-top: 10; anchor-height: 40; layout: Middle;">
                            <p style="font-size: 24; render-bold: true; text-color: #F0E68C;">KITS DISPONIVEIS</p>
                        </div>
                        
                        <!-- Kits Container (horizontal layout) -->
                        <div style="anchor-left: 20; anchor-right: 20; anchor-top: 60; anchor-bottom: 20; layout: Left;">
                            
                            <!-- Basic Kit Card -->
                            <div style="anchor-width: 340; anchor-height: 440; background-color: #1a2332; background-corner-radius: 12;">
                                
                                <!-- Card Header with Banner Image -->
                                <div style="anchor-left: 0; anchor-right: 0; anchor-top: 0; anchor-height: 165; background-corner-radius-top-left: 12; background-corner-radius-top-right: 12; layout: Middle;">
                                    <img src="basico.png" width="190" height="165"/>
                                </div>
                                
                                <!-- Kit Name -->
                                <div style="anchor-left: 10; anchor-right: 10; anchor-top: 170; anchor-height: 30; layout: Middle;">
                                    <p style="font-size: 20; render-bold: true; text-color: #7CB342;">%s</p>
                                </div>
                                
                                <!-- Description -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 200; anchor-height: 25; layout: Middle;">
                                    <p style="font-size: 12; text-color: #90A4AE;">%s</p>
                                </div>
                                
                                <!-- Items Label -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 225; anchor-height: 20;">
                                    <p style="font-size: 11; text-color: #B0BEC5; render-bold: true;">Itens do Kit:</p>
                                </div>
                                
                                <!-- Item Icons Container -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 245; anchor-height: 60; layout: Left;">
                                    %s
                                </div>
                                
                                <!-- Cooldown Info -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 315; anchor-height: 25; layout: Middle;">
                                    <p style="font-size: 13; text-color: #FFA726;">Cooldown: %s</p>
                                </div>
                                
                                <!-- Status Indicator -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 340; anchor-height: 30; layout: Middle;">
                                    <p style="font-size: 14; render-bold: true; text-color: %s;">%s</p>
                                </div>
                                
                                <!-- Claim Button -->
                                <div style="anchor-left: 20; anchor-right: 20; anchor-bottom: 15; anchor-height: 45; layout: Middle;">
                                    <button id="basicKitBtn" style="anchor-width: 280; anchor-height: 40;">%s</button>
                                </div>
                            </div>
                            
                            <!-- Spacer -->
                            <div style="anchor-width: 20;"></div>
                            
                            <!-- VIP Kit Card -->
                            <div style="anchor-width: 340; anchor-height: 440; background-color: #1a2332; background-corner-radius: 12;">
                                
                                <!-- Card Header with Banner Image -->
                                <div style="anchor-left: 0; anchor-right: 0; anchor-top: 0; anchor-height: 165; background-corner-radius-top-left: 12; background-corner-radius-top-right: 12; layout: Middle;">
                                    <img src="vip.png" width="190" height="165"/>
                                </div>
                                
                                <!-- Kit Name -->
                                <div style="anchor-left: 10; anchor-right: 10; anchor-top: 170; anchor-height: 30; layout: Middle;">
                                    <p style="font-size: 20; render-bold: true; text-color: #FFD700;">%s</p>
                                </div>
                                
                                <!-- Description -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 200; anchor-height: 25; layout: Middle;">
                                    <p style="font-size: 12; text-color: #90A4AE;">%s</p>
                                </div>
                                
                                <!-- Items Label -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 225; anchor-height: 20;">
                                    <p style="font-size: 11; text-color: #B0BEC5; render-bold: true;">Itens do Kit:</p>
                                </div>
                                
                                <!-- Item Icons Container -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 245; anchor-height: 60; layout: Left;">
                                    %s
                                </div>
                                
                                <!-- Cooldown Info -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 315; anchor-height: 25; layout: Middle;">
                                    <p style="font-size: 13; text-color: #FFA726;">Cooldown: %s</p>
                                </div>
                                
                                <!-- Status Indicator -->
                                <div style="anchor-left: 15; anchor-right: 15; anchor-top: 340; anchor-height: 30; layout: Middle;">
                                    <p style="font-size: 14; render-bold: true; text-color: %s;">%s</p>
                                </div>
                                
                                <!-- Claim Button -->
                                <div style="anchor-left: 20; anchor-right: 20; anchor-bottom: 15; anchor-height: 45; layout: Middle;">
                                    <button id="vipKitBtn" style="anchor-width: 280; anchor-height: 40;">%s</button>
                                </div>
                            </div>
                            
                        </div>
                        
                    </div>
                </div>
            </div>
            """.formatted(
                // Basic Kit
                basicKit != null ? basicKit.getDisplayName() : "Kit Basico",
                basicKit != null ? basicKit.getDescription() : "Kit inicial",
                basicItemIcons,
                formatCooldownDisplay(basicKit != null ? basicKit.getCooldownSeconds() : 300),
                basicEnabled ? "#4CAF50" : "#F44336",
                basicEnabled ? "DISPONIVEL" : "EM COOLDOWN: " + formatCooldown(basicCooldown),
                basicEnabled ? "RESGATAR KIT" : formatCooldown(basicCooldown),
                // VIP Kit
                vipKit != null ? vipKit.getDisplayName() : "Kit VIP",
                vipKit != null ? vipKit.getDescription() : "Kit exclusivo",
                vipItemIcons,
                formatCooldownDisplay(vipKit != null ? vipKit.getCooldownSeconds() : 180),
                vipEnabled ? "#FFD700" : "#F44336",
                vipEnabled ? "DISPONIVEL" : "EM COOLDOWN: " + formatCooldown(vipCooldown),
                vipEnabled ? "RESGATAR KIT VIP" : formatCooldown(vipCooldown)
        );

        PageBuilder page = PageBuilder.detachedPage()
                .withLifetime(CustomPageLifetime.CanDismiss)
                .fromHtml(html);
        
        // Add event listener for basic kit button
        page.getById("basicKitBtn", ButtonBuilder.class).ifPresent(button -> {
            button.addEventListener(CustomUIEventBindingType.Activating, event -> {
                handleKitSelection(playerRef, player, "basic");
            });
        });

        // VIP kit button
        page.getById("vipKitBtn", ButtonBuilder.class).ifPresent(button -> {
            button.addEventListener(CustomUIEventBindingType.Activating, event -> {
                handleKitSelection(playerRef, player, "vip");
            });
        });

        return page;
    }

    private String buildItemIconsHtml(KitsConfig.KitDefinition kit, String prefix) {
        if (kit == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (KitsConfig.KitItem item : kit.getItems()) {
            String shortName = getItemShortName(item.getItemId());
            // Create styled item boxes with item name and quantity
            sb.append(String.format("""
                <div style="anchor-width: 58; anchor-height: 55; background-color: #2a3a4f; background-corner-radius: 6; layout: Middle;">
                    <p style="font-size: 8; text-color: #B0BEC5; wrap: true; anchor-top: 5;">%s</p>
                    <p style="font-size: 11; text-color: #FFFFFF; render-bold: true; anchor-bottom: 5;">x%d</p>
                </div>
                """, shortName, item.getQuantity()));
        }
        return sb.toString();
    }

    private String getItemShortName(String itemId) {
        // Convert item ID to short display name
        return itemId
                .replace("Weapon_Sword_", "")
                .replace("Tool_Pickaxe_", "")
                .replace("Tool_Hatchet_", "")
                .replace("Plant_Fruit_", "")
                .replace("Furniture_Crude_", "")
                .replace("_", " ");
    }

    private void handleKitSelection(PlayerRef playerRef, Player player, String kitId) {
        KitsConfig.KitDefinition kit = KitsConfig.get().getKit(kitId);

        if (kit == null) {
            player.sendMessage(Message.raw("Kit nao encontrado!").color("#FF0000"));
            return;
        }

        String playerUuid = player.getUuid().toString();

        // Check cooldown
        long remainingCooldown = KitManager.get().getRemainingCooldown(playerUuid, kitId);
        if (remainingCooldown > 0) {
            String cooldownText = formatCooldown(remainingCooldown);
            player.sendMessage(Message.raw("Aguarde " + cooldownText + " para usar este kit novamente!").color("#FF6B6B"));
            return;
        }

        // Give the kit items
        boolean success = KitManager.get().giveKit(player, kit);

        if (success) {
            String color = kit.isVip() ? "#FFD700" : "#4CAF50";
            player.sendMessage(Message.raw("Voce recebeu o " + kit.getDisplayName() + "!").color(color));
        } else {
            player.sendMessage(Message.raw("Erro ao entregar o kit. Verifique seu inventario!").color("#FF0000"));
        }
    }

    private String formatCooldown(long seconds) {
        if (seconds <= 0) {
            return "Pronto!";
        } else if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "m " + secs + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }

    private String formatCooldownDisplay(int seconds) {
        if (seconds < 60) {
            return seconds + " segundos";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return minutes + " minuto" + (minutes > 1 ? "s" : "");
        } else {
            int hours = seconds / 3600;
            return hours + " hora" + (hours > 1 ? "s" : "");
        }
    }

    private String formatItemName(String itemId) {
        // Convert item ID to readable name
        // e.g., "Weapon_Sword_Iron" -> "Espada de Ferro"
        return itemId
                .replace("Weapon_Sword_", "Espada ")
                .replace("Tool_Pickaxe_", "Picareta ")
                .replace("Tool_Hatchet_", "Machado ")
                .replace("Plant_Fruit_", "")
                .replace("Furniture_Crude_", "")
                .replace("Iron", "de Ferro")
                .replace("Mithril", "de Mithril")
                .replace("Apple", "Maca")
                .replace("Torch", "Tocha");
    }
}
