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
import net.caseif.infernospleef.command.JoinArenaCommand;

import com.google.common.eventbus.Subscribe;
import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.event.lobby.PlayerClickLobbySignEvent;
import net.caseif.flint.event.round.RoundChangeLifecycleStageEvent;
import net.caseif.flint.event.round.RoundTimerTickEvent;
import net.caseif.flint.event.round.challenger.ChallengerJoinRoundEvent;
import net.caseif.flint.util.physical.Location3D;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Listener for minigame-related events.
 *
 * @author Max Roncace
 */
public class MinigameListener {

    @Subscribe
    public void onChallengerJoinRound(ChallengerJoinRoundEvent event) {
        // check if round is in progress
        if (event.getRound().getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
            event.getChallenger().setSpectating(true); // can't just join in the middle of a round
        } else if (event.getRound().getLifecycleStage().getId().equals(Main.WAITING_STAGE_ID)
                && event.getRound().getChallengers().size() >= Main.MIN_PLAYERS) {
            event.getRound().nextLifecycleStage(); // advance to preparation stage
        }
    }

    @Subscribe
    public void onRoundChangeLifecycleStage(RoundChangeLifecycleStageEvent event) {
        // check if round is in progress
        if (event.getStageAfter().getId().equals(Main.PLAYING_STAGE_ID)) {
            Text msg = Text.builder(Main.getString("message.info.event.start")).insert(0, Main.PREFIX)
                    .color(Main.INFO_COLOR).build();
            for (Challenger ch : event.getRound().getChallengers()) {
                Sponge.getServer().getPlayer(ch.getUniqueId()).get().sendMessage(msg);
                Sponge.getServer().getPlayer(ch.getUniqueId()).get().getInventory().offer(Main.SHOVEL);
            }
        } else if (event.getStageAfter().getId().equals(Main.PREPARING_STAGE_ID)) {
            Text msg = Text.builder(Main.getString("message.info.event.prepare")).insert(0, Main.PREFIX)
                    .color(Main.INFO_COLOR).build();
            for (Challenger ch : event.getRound().getChallengers()) {
                Sponge.getServer().getPlayer(ch.getUniqueId()).get().sendMessage(msg);
            }
        }
    }

    @Subscribe
    public void onRoundTimerTick(RoundTimerTickEvent event) {
        if (event.getRound().getRemainingTime() % 10 == 0 && event.getRound().getRemainingTime() > 0) {
            if (!event.getRound().getLifecycleStage().getId().equals(Main.WAITING_STAGE_ID)) {
                Text msg = Text.builder(Main.getString(
                        event.getRound().getLifecycleStage().getId().equals(Main.PREPARING_STAGE_ID)
                                ? "message.info.event.begin-countdown"
                                : "message.info.event.end-countdown",
                        event.getRound().getRemainingTime() + "")).insert(0, Main.PREFIX).color(Main.INFO_COLOR)
                        .build();
                for (Challenger ch : event.getRound().getChallengers()) {
                    Sponge.getServer().getPlayer(ch.getUniqueId()).get().sendMessage(msg);
                }
            }
        }

        // iterate the challengers once per round tick
        event.getRound().getChallengers().stream()
                .filter(challenger -> // check whether the challenger is below y=0 (the void)
                        Sponge.getServer().getPlayer(challenger.getUniqueId()).get().getLocation().getY() < 0)
                .forEach(challenger -> {
                    if (event.getRound().getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                        challenger.removeFromRound(); // they lost
                    } else {
                        Location3D spawn = event.getRound().getArena().getSpawnPoints().get(0);
                        Optional<World> w = Sponge.getServer().getWorld(spawn.getWorld().get());
                        assert w.isPresent();
                        Sponge.getServer().getPlayer(challenger.getUniqueId()).get()
                                .setLocation(new Location<>(w.get(), spawn.getX(), spawn.getY(), spawn.getZ()));
                    }
                });

        if (event.getRound().getChallengers().size() <= 1) {
            if (event.getRound().getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                if (event.getRound().getChallengers().size() == 1) {
                    Text msg = Text.builder(Main.getString("message.info.event.win",
                            event.getRound().getChallengers().toArray(new Challenger[1])[0].getName(),
                            event.getRound().getArena().getDisplayName())).insert(0, Main.PREFIX).color(Main.INFO_COLOR)
                            .build();
                    event.getRound().getChallengers()
                            .forEach(ch -> Sponge.getServer().getPlayer(ch.getUniqueId()).get().sendMessage(msg));

                    event.getRound().end();
                } else if (event.getRound().getChallengers().isEmpty()) {
                    event.getRound().end();
                }
            } else if (event.getRound().getLifecycleStage().getId().equals(Main.PREPARING_STAGE_ID)) {
                event.getRound().setLifecycleStage(event.getRound().getLifecycleStage(Main.WAITING_STAGE_ID).get());
            }
        }
    }

    @Subscribe
    public void onPlayerClickLobbySign(PlayerClickLobbySignEvent event) {
        if (Sponge.getServer().getPlayer(event.getPlayer()).get().hasPermission("infernospleef.play")) {
            // simulate a join command because I'm a lazy bastard
            CommandContext cmdCtx = new CommandContext();
            cmdCtx.putArg("arena", event.getLobbySign().getArena().getId());
            new JoinArenaCommand().execute(Sponge.getServer().getPlayer(event.getPlayer()).get(), cmdCtx);
        }
    }

}
