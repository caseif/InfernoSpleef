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

package net.caseif.infernospleef.command;

import static net.caseif.infernospleef.Main.ERROR_COLOR;
import static net.caseif.infernospleef.Main.INFO_COLOR;
import static net.caseif.infernospleef.Main.PREFIX;
import static net.caseif.infernospleef.Main.getString;

import net.caseif.infernospleef.Main;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import net.caseif.flint.round.JoinResult;
import net.caseif.flint.round.Round;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * Handler for the arena join command.
 *
 * @author Max Roncacé
 */
public class JoinArenaCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.builder(getString("message.error.general.in-game")).insert(0, PREFIX)
                    .color(ERROR_COLOR).build());
            return CommandResult.empty();
        }

        String arenaId = args.<String>getOne("arena").orElse(null);
        if (arenaId == null) {
            sender.sendMessage(Text.builder(getString("message.error.general.too-few-args")).insert(0, PREFIX)
                    .color(ERROR_COLOR).build());
            sender.sendMessage(Text.builder(getString("message.error.general.usage", "/fs join [arena]"))
                    .insert(0, PREFIX).color(ERROR_COLOR).build());
            return CommandResult.empty();
        }

        Optional<Arena> arena = Main.getMinigame().getArena(arenaId);
        if (!arena.isPresent()) {
            sender.sendMessage(Text.builder(getString("message.error.command.join.not-found", arenaId))
                    .insert(0, PREFIX).color(ERROR_COLOR).build());
            return CommandResult.empty();
        }

        Round round = arena.get().getRound().orNull();
        if (round == null) {
            round = arena.get().createRound();
        }

        if (round.getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
            sender.sendMessage(Text.builder(getString("message.error.command.join.progress")).insert(0, PREFIX)
                    .color(ERROR_COLOR).build());
            return CommandResult.empty();
        }

        JoinResult result = round.addChallenger(((Player) sender).getUniqueId());
        sender.sendMessage(Text.builder(getString("message.info.command.join.success",
                arena.get().getDisplayName())).insert(0, PREFIX).color(INFO_COLOR).build());
        switch (result.getStatus()) {
            case SUCCESS:
                return CommandResult.success();
            case ALREADY_IN_ROUND:
                sender.sendMessage(Text.builder("You are already in a round!").insert(0, PREFIX).color(ERROR_COLOR)
                        .build());
                break;
            case ROUND_FULL:
                sender.sendMessage(Text.builder("The round is full!").insert(0, PREFIX).color(ERROR_COLOR).build());
                break;
            case PLAYER_OFFLINE:
                sender.sendMessage(Text.builder("You are... offline? What?").insert(0, PREFIX).color(ERROR_COLOR)
                        .build());
                break;
            case INTERNAL_ERROR:
                //noinspection ThrowableResultOfMethodCallIgnored
                sender.sendMessage(Text.builder(getString("message.error.command.join.exception",
                        result.getThrowable().getMessage())).insert(0, PREFIX).color(ERROR_COLOR).build());
                break;
            default:
                throw new AssertionError();
        }

        return CommandResult.empty();
    }

}
