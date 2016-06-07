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
import static net.caseif.infernospleef.Main.getString;
import static net.caseif.infernospleef.Main.withPrefix;

import net.caseif.infernospleef.Main;

import com.google.common.base.Optional;
import net.caseif.flint.challenger.Challenger;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * Handler for the arena leave command.
 *
 * @author Max Roncacé
 */
public class LeaveArenaCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(withPrefix(Text.builder(getString("message.error.general.in-game"))
                    .color(ERROR_COLOR).build()));
            return CommandResult.empty();
        }

        Optional<Challenger> challenger = Main.getMinigame().getChallenger(((Player) sender).getUniqueId());
        if (!challenger.isPresent()) {
            sender.sendMessage(withPrefix(Text.builder("You are not in a round!")
                    .color(ERROR_COLOR).build()));
            return CommandResult.empty();
        }

        challenger.get().removeFromRound();
        sender.sendMessage(withPrefix(Text.builder(getString("message.info.command.leave.success"))
                .color(INFO_COLOR).build()));
        return CommandResult.success();
    }

}
