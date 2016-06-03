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

import static net.caseif.flint.spleef.Main.EM_COLOR;
import static net.caseif.flint.spleef.Main.ERROR_COLOR;
import static net.caseif.flint.spleef.Main.INFO_COLOR;
import static net.caseif.flint.spleef.Main.LOCALE_MANAGER;
import static net.caseif.flint.spleef.Main.PREFIX;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARDS;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_FIRST_BOUND;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_ID;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_INFO;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_NAME;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_SECOND_BOUND;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_SPAWN_POINT;

import net.caseif.flint.spleef.Main;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player-related events.
 *
 * @author Max Roncacé
 */
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
            int stage = WIZARDS.get(event.getPlayer().getUniqueId());
            if (event.getMessage().equalsIgnoreCase(LOCALE_MANAGER
                    .getLocalizable("message.info.command.create.cancel-keyword")
                    .localizeFor(event.getPlayer()))) {
                event.setCancelled(true);
                WIZARDS.remove(event.getPlayer().getUniqueId());
                WIZARD_INFO.remove(event.getPlayer().getUniqueId());
                LOCALE_MANAGER.getLocalizable("message.info.command.create.cancelled").withPrefix(PREFIX + ERROR_COLOR)
                        .sendTo(event.getPlayer());
                return;
            }
            event.setCancelled(true);
            switch (stage) {
                case WIZARD_ID:
                    if (!Main.getMinigame().getArena(event.getMessage()).isPresent()) {
                        if (!event.getMessage().contains(".")) {
                            increment(event.getPlayer());
                            WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_ID] = event.getMessage();
                            LOCALE_MANAGER.getLocalizable("message.info.command.create.id")
                                    .withPrefix(PREFIX + INFO_COLOR)
                                    .withReplacements(EM_COLOR + event.getMessage().toLowerCase() + INFO_COLOR)
                                    .sendTo(event.getPlayer());
                        } else {
                            LOCALE_MANAGER.getLocalizable("message.error.command.create.invalid-id")
                                    .withPrefix(PREFIX + ERROR_COLOR).sendTo(event.getPlayer());
                        }
                    } else {
                        LOCALE_MANAGER.getLocalizable("message.error.command.create.id-already-exists")
                                .withPrefix(PREFIX + ERROR_COLOR).sendTo(event.getPlayer());
                    }
                    break;
                case WIZARD_NAME:
                    increment(event.getPlayer());
                    WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_NAME] = event.getMessage();
                    LOCALE_MANAGER.getLocalizable("message.info.command.create.name").withPrefix(PREFIX + INFO_COLOR)
                            .withReplacements(EM_COLOR + event.getMessage() + INFO_COLOR).sendTo(event.getPlayer());
                    break;
                case WIZARD_SPAWN_POINT:
                    if (event.getMessage().equalsIgnoreCase(
                            LOCALE_MANAGER.getLocalizable("message.info.command.create.ok-keyword")
                                    .localizeFor(event.getPlayer()))) {
                        if (event.getPlayer().getWorld().getName().equals(
                                ((Location3D) WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_FIRST_BOUND])
                                        .getWorld().get()
                        )) {
                            Location3D spawn = new Location3D(event.getPlayer().getWorld().getName(),
                                    event.getPlayer().getLocation().getX(),
                                    event.getPlayer().getLocation().getY(),
                                    event.getPlayer().getLocation().getZ());
                            Object[] info = WIZARD_INFO.get(event.getPlayer().getUniqueId());
                            Main.getMinigame().createArena((String) info[WIZARD_ID], (String) info[WIZARD_NAME],
                                    spawn, new Boundary((Location3D) info[WIZARD_FIRST_BOUND],
                                            (Location3D) info[WIZARD_SECOND_BOUND]));
                            LOCALE_MANAGER.getLocalizable("message.info.command.create.success")
                                    .withPrefix(PREFIX + INFO_COLOR).withReplacements(EM_COLOR + "/fs join "
                                    + ((String) info[WIZARD_ID]).toLowerCase() + INFO_COLOR).
                                    sendTo(event.getPlayer());
                            WIZARDS.remove(event.getPlayer().getUniqueId());
                            WIZARD_INFO.remove(event.getPlayer().getUniqueId());
                        } else {
                            LOCALE_MANAGER.getLocalizable("message.error.command.create.bad-spawn")
                                    .withPrefix(PREFIX + ERROR_COLOR).sendTo(event.getPlayer());
                        }
                        break;
                    }
                    // fall-through is intentional
                default:
                    event.setCancelled(false);
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
                int stage = WIZARDS.get(event.getPlayer().getUniqueId());
                event.setCancelled(true);
                Block c = event.getClickedBlock();
                switch (stage) {
                    case WIZARD_FIRST_BOUND:
                        increment(event.getPlayer());
                        WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_FIRST_BOUND]
                                = new Location3D(c.getWorld().getName(), c.getX(), 0, c.getZ());
                        LOCALE_MANAGER.getLocalizable("message.info.command.create.bound-1")
                                .withPrefix(PREFIX + INFO_COLOR)
                                .withReplacements(EM_COLOR + "(x=" + c.getX() + ", z=" + c.getZ() + ")" + INFO_COLOR)
                                .sendTo(event.getPlayer());
                        break;
                    case WIZARD_SECOND_BOUND:
                        if (c.getWorld().getName().equals(
                                ((Location3D) WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_FIRST_BOUND])
                                        .getWorld().get()
                        )) {
                            increment(event.getPlayer());
                            WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_SECOND_BOUND]
                                    = new Location3D(c.getWorld().getName(), c.getX(), c.getWorld().getMaxHeight(),
                                    c.getZ());
                            LOCALE_MANAGER.getLocalizable("message.info.command.create.bound-2")
                                    .withPrefix(PREFIX + INFO_COLOR)
                                    .withReplacements(EM_COLOR + "(x=" + c.getX() + ", z=" + c.getZ() + ")"
                                            + INFO_COLOR, EM_COLOR
                                            + LOCALE_MANAGER.getLocalizable("message.info.command.create.ok-keyword")
                                            .localizeFor(event.getPlayer()) + INFO_COLOR)
                                    .sendTo(event.getPlayer());
                        } else {
                            LOCALE_MANAGER.getLocalizable("message.error.command.create.bad-bound")
                                    .withPrefix(PREFIX + ERROR_COLOR).sendTo(event.getPlayer());
                        }
                        break;
                    default:
                        event.setCancelled(false);
                        break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            if (Main.getMinigame().getChallenger(event.getEntity().getUniqueId()).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
            WIZARDS.remove(event.getPlayer().getUniqueId());
            WIZARD_INFO.remove(event.getPlayer().getUniqueId());
        }
    }

    private void increment(Player player) {
        WIZARDS.put(player.getUniqueId(), WIZARDS.get(player.getUniqueId()) + 1);
    }

}
