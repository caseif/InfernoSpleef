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

import static net.caseif.infernospleef.command.CreateArenaCommand.WIZARDS;
import static net.caseif.infernospleef.command.CreateArenaCommand.WIZARD_FIRST_BOUND;
import static net.caseif.infernospleef.command.CreateArenaCommand.WIZARD_ID;
import static net.caseif.infernospleef.command.CreateArenaCommand.WIZARD_INFO;
import static net.caseif.infernospleef.command.CreateArenaCommand.WIZARD_NAME;
import static net.caseif.infernospleef.command.CreateArenaCommand.WIZARD_SECOND_BOUND;
import static net.caseif.infernospleef.command.CreateArenaCommand.WIZARD_SPAWN_POINT;

import net.caseif.infernospleef.Main;

import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Listener for player-related events.
 *
 * @author Max Roncacé
 */
public class PlayerListener {

    @Listener
    public void onMessageChannel(MessageChannelEvent event) {
        java.util.Optional<Player> plOpt = event.getCause().first(Player.class);
        if (!plOpt.isPresent()) {
            return;
        }
        Player pl = plOpt.get();

        if (WIZARDS.containsKey(pl.getUniqueId())) {
            int stage = WIZARDS.get(pl.getUniqueId());
            if (event.getMessage().toPlain()
                    .equalsIgnoreCase(Main.getString("message.info.command.create.cancel-keyword"))) {
                event.setMessageCancelled(true);
                WIZARDS.remove(pl.getUniqueId());
                WIZARD_INFO.remove(pl.getUniqueId());
                pl.sendMessage(Text.builder(Main.getString("message.info.command.create.cancelled"))
                        .insert(0, Main.PREFIX).color(Main.ERROR_COLOR).build());
                return;
            }
            event.setMessageCancelled(true);
            switch (stage) {
                case WIZARD_ID:
                    if (!Main.getMinigame().getArena(event.getMessage().toPlain()).isPresent()) {
                        increment(pl);
                        WIZARD_INFO.get(pl.getUniqueId())[WIZARD_ID] = event.getMessage();
                        pl.sendMessage(Text.builder(Main.getString("message.info.command.create.id",
                                event.getMessage().toPlain().toLowerCase()))
                                .insert(0, Main.PREFIX).color(Main.INFO_COLOR).build());
                    } else {
                        pl.sendMessage(Text.builder(Main.getString("message.error.command.create.id-already-exists"))
                                .insert(0, Main.PREFIX).color(Main.ERROR_COLOR).build());
                    }
                    break;
                case WIZARD_NAME:
                    increment(pl);
                    WIZARD_INFO.get(pl.getUniqueId())[WIZARD_NAME] = event.getMessage();
                    pl.sendMessage(Text.builder(Main.getString("message.info.command.create.name",
                            event.getMessage().toPlain())).insert(0, Main.PREFIX).color(Main.INFO_COLOR).build());
                    break;
                case WIZARD_SPAWN_POINT:
                    if (event.getMessage().toPlain()
                            .equalsIgnoreCase(Main.getString("message.info.command.create.ok-keyword"))) {
                        if (pl.getWorld().getName().equals(
                                ((Location3D) WIZARD_INFO.get(pl.getUniqueId())[WIZARD_FIRST_BOUND])
                                        .getWorld().get()
                        )) {
                            Location3D spawn = new Location3D(pl.getWorld().getName(),
                                    pl.getLocation().getX(),
                                    pl.getLocation().getY(),
                                    pl.getLocation().getZ());
                            Object[] info = WIZARD_INFO.get(pl.getUniqueId());
                            Main.getMinigame().createArena((String) info[WIZARD_ID], (String) info[WIZARD_NAME],
                                    spawn, new Boundary((Location3D) info[WIZARD_FIRST_BOUND],
                                            (Location3D) info[WIZARD_SECOND_BOUND]));
                            pl.sendMessage(Text.builder(Main.getString("message.info.command.create.success",
                                    "/is join " + ((String) info[WIZARD_ID]).toLowerCase() + Main.INFO_COLOR))
                                    .insert(0, Main.PREFIX).color(Main.INFO_COLOR).build());
                            WIZARDS.remove(pl.getUniqueId());
                            WIZARD_INFO.remove(pl.getUniqueId());
                        } else {
                            pl.sendMessage(Text.builder(Main.getString("message.error.command.create.bad-spawn"))
                                    .insert(0, Main.PREFIX).color(Main.ERROR_COLOR).build());
                        }
                        break;
                    }
                    // fall-through is intentional
                default:
                    event.setMessageCancelled(false);
                    break;
            }
        }
    }

    @Listener
    public void onInteract(InteractBlockEvent event) {
        java.util.Optional<Player> plOpt = event.getCause().first(Player.class);
        if (!plOpt.isPresent()) {
            return;
        }
        Player pl = plOpt.get();

        if (WIZARDS.containsKey(pl.getUniqueId())) {
            int stage = WIZARDS.get(pl.getUniqueId());
            event.setCancelled(true);

            java.util.Optional<Location<World>> blockOpt = event.getTargetBlock().getLocation();
            assert blockOpt.isPresent();
            Location<World> block = blockOpt.get();

            switch (stage) {
                case WIZARD_FIRST_BOUND:
                    increment(pl);
                    WIZARD_INFO.get(pl.getUniqueId())[WIZARD_FIRST_BOUND]
                            = new Location3D(block.getExtent().getName(), block.getX(), 0, block.getZ());
                    pl.sendMessage(Text.builder(Main.getString("message.info.command.create.bound-1",
                            "(x=" + block.getX() + ", z=" + block.getZ() + ")"))
                            .insert(0, Main.PREFIX).color(Main.INFO_COLOR).build());
                    break;
                case WIZARD_SECOND_BOUND:
                    if (block.getExtent().getName().equals(
                            ((Location3D) WIZARD_INFO.get(pl.getUniqueId())[WIZARD_FIRST_BOUND])
                                    .getWorld().get()
                    )) {
                        increment(pl);
                        WIZARD_INFO.get(pl.getUniqueId())[WIZARD_SECOND_BOUND]
                                = new Location3D(block.getExtent().getName(), block.getX(),
                                block.getExtent().getBlockMax().getY(), block.getZ());
                        pl.sendMessage(Text.builder(Main.getString("message.info.command.create.bound-2",
                                "(x=" + block.getX() + ", z=" + block.getZ() + ")",
                                Main.getString("message.info.command.create.ok-keyword")))
                                .insert(0, Main.PREFIX).color(Main.INFO_COLOR).build());
                    } else {
                        pl.sendMessage(Text.builder(Main.getString("message.error.command.create.bad-bound"))
                                .insert(0, Main.PREFIX).color(Main.ERROR_COLOR).build());
                    }
                    break;
                default:
                    event.setCancelled(false);
                    break;
            }
        }
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event) {
        if (event.getTargetEntity().getType() == EntityTypes.PLAYER) {
            if (Main.getMinigame().getChallenger(event.getTargetEntity().getUniqueId()).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event) {
        if (WIZARDS.containsKey(event.getTargetEntity().getUniqueId())) {
            WIZARDS.remove(event.getTargetEntity().getUniqueId());
            WIZARD_INFO.remove(event.getTargetEntity().getUniqueId());
        }
    }

    private void increment(Player player) {
        WIZARDS.put(player.getUniqueId(), WIZARDS.get(player.getUniqueId()) + 1);
    }

}
