/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016, Max Roncace <me@caseif.net>
 * Copyright (c) 2016, contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.caseif.infernospleef.listener;

import net.caseif.infernospleef.Main;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

/**
 * Listener for block-related events.
 *
 * @author Max Roncacé
 */
public class BlockListener {

    @Listener
    public void onInteractBlock(InteractBlockEvent event) {
        java.util.Optional<Player> player = event.getCause().first(Player.class);
        if (!player.isPresent()) {
            return;
        }

        // check if the damager is a challenger
        Optional<Challenger> challenger = Main.getMinigame().getChallenger(player.get().getUniqueId());
        if (challenger.isPresent()) { // damager is a challenger
            // check if the round hasn't started yet
            if (!challenger.get().getRound().getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                event.setCancelled(true); // can't break blocks in advance
            }
            // check if they're holding a shovel
            if (!player.get().getItemInHand(HandTypes.MAIN_HAND).isPresent()
                    || !Main.SHOVELS.contains(player.get().getItemInHand(HandTypes.MAIN_HAND).get().getItem())) {
                event.setCancelled(true); // can't break blocks without a shovel
            }
        }
    }

    @Listener
    public void onBreakBlock(ChangeBlockEvent.Break event) {
        java.util.Optional<Player> player = event.getCause().first(Player.class);
        if (!player.isPresent()) {
            return;
        }

        // check if the damager is a challenger
        Optional<Challenger> challenger = Main.getMinigame().getChallenger(player.get().getUniqueId());
        if (challenger.isPresent()) {
            if (player.get().getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                if (player.get().getItemInHand(HandTypes.MAIN_HAND).get().getValue(Keys.ITEM_DURABILITY).isPresent()) {
                    player.get().getItemInHand(HandTypes.MAIN_HAND).get().getValue(Keys.ITEM_DURABILITY).get().set(0);
                }
            }
        }
    }

    @Listener
    public void onDropItem(DropItemEvent.Destruct event) {
        java.util.Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent() && event.getCause().root() instanceof BlockSpawnCause) {
            Optional<Challenger> challenger = Main.getMinigame().getChallenger(player.get().getUniqueId());
            if (challenger.isPresent()) {
                event.setCancelled(true);
            }
        }
    }

}
