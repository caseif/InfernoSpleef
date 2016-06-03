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

package net.caseif.flint.spleef.listener;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.spleef.Main;

import com.google.common.base.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

/**
 * Listener for block-related events.
 *
 * @author Max Roncacé
 */
public class BlockListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        // check if the damager is a challenger
        Optional<Challenger> challenger = Main.getMinigame().getChallenger(event.getPlayer().getUniqueId());
        if (challenger.isPresent()) { // damager is a challenger
            // check if the round hasn't started yet
            if (!challenger.get().getRound().getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                event.setCancelled(true); // can't break blocks in advance
            }
            // check if they're holding a shovel
            if (!Main.SHOVELS.contains(event.getItemInHand().getType())) {
                event.setCancelled(true); // can't break blocks without a shovel
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // check if the damager is a challenger
        Optional<Challenger> challenger = Main.getMinigame().getChallenger(event.getPlayer().getUniqueId());
        if (challenger.isPresent()) { // damager is a challenger
            event.getBlock().getDrops().clear(); // clear the drops
            event.getPlayer().getItemInHand().setDurability((short) 0); // avoid damaging the shovel
        }
    }

}
