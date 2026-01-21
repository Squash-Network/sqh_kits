package com.squashcompany.kits.kits.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.squashcompany.kits.kits.pages.KitsPage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

/**
 * Command /kits - Opens the kits selection menu using native Hytale UI.
 *
 * Uses the native InteractiveCustomUIPage API for a professional Hytale-style
 * interface.
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

        if (ref == null || !ref.isValid()) {
            commandContext.sendMessage(Message.raw("Voce precisa estar em um mundo para usar este comando!"));
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

            if (playerRef == null) {
                commandContext.sendMessage(Message.raw("Erro ao obter referencia do jogador!"));
                return;
            }

            // Open the kits page on the world thread
            world.execute(() -> {
                // Create and open the native Hytale UI page
                KitsPage kitsPage = new KitsPage(playerRef, player);
                player.getPageManager().openCustomPage(
                        player.getReference(),
                        store,
                        kitsPage
                );
            });
        }, world);
    }
}
