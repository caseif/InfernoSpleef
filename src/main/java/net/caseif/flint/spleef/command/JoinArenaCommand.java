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

package net.caseif.flint.spleef.command;

import static net.caseif.flint.spleef.Main.EM_COLOR;
import static net.caseif.flint.spleef.Main.ERROR_COLOR;
import static net.caseif.flint.spleef.Main.INFO_COLOR;
import static net.caseif.flint.spleef.Main.LOCALE_MANAGER;
import static net.caseif.flint.spleef.Main.PREFIX;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.Round;
import net.caseif.flint.spleef.Main;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handler for the arena join command.
 *
 * @author Max Roncacé
 */
public class JoinArenaCommand {

    public static void handle(CommandSender sender, String[] args) {
        if (sender.hasPermission("flintspleef.play")) {
            if (sender instanceof Player) {
                if (args.length > 1) {
                    String[] idArray = new String[args.length - 1];
                    System.arraycopy(args, 1, idArray, 0, idArray.length);
                    String arenaId = Joiner.on(" ").join(idArray);
                    Optional<Arena> arena = Main.getMinigame().getArena(arenaId);
                    if (arena.isPresent()) {
                        Round round = arena.get().getRound().orNull();
                        if (round == null) {
                            round = arena.get().createRound();
                        }
                        if (!round.getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                            try {
                                round.addChallenger(((Player) sender).getUniqueId());
                                LOCALE_MANAGER.getLocalizable("message.info.command.join.success")
                                        .withPrefix(PREFIX + INFO_COLOR)
                                        .withReplacements(EM_COLOR + arena.get().getName()).sendTo(sender);
                            } catch (RoundJoinException ex) {
                                LOCALE_MANAGER.getLocalizable("message.error.command.join.exception")
                                        .withPrefix(PREFIX + ERROR_COLOR).withReplacements(ex.getMessage())
                                        .sendTo(sender);
                            }
                        } else {
                            LOCALE_MANAGER.getLocalizable("message.error.command.join.progress")
                                    .withPrefix(PREFIX + ERROR_COLOR).sendTo(sender);
                        }
                    } else {
                        LOCALE_MANAGER.getLocalizable("message.error.command.join.not-found")
                                .withPrefix(PREFIX + ERROR_COLOR).withReplacements(EM_COLOR + arenaId + ERROR_COLOR)
                                .sendTo(sender);
                    }
                } else {
                    LOCALE_MANAGER.getLocalizable("message.error.general.too-few-args").withPrefix(PREFIX + ERROR_COLOR)
                            .sendTo(sender);
                    LOCALE_MANAGER.getLocalizable("message.error.general.usage").withPrefix(PREFIX + ERROR_COLOR)
                            .withReplacements("/fs join [arena]").sendTo(sender);
                }
            } else {
                sender.sendMessage(LOCALE_MANAGER.getLocalizable("message.error.general.in-game")
                        .withPrefix(PREFIX + ERROR_COLOR).localize());
            }
        } else {
            LOCALE_MANAGER.getLocalizable("message.error.general.permission").withPrefix(PREFIX + ERROR_COLOR)
                    .sendTo(sender);
        }
    }

}
