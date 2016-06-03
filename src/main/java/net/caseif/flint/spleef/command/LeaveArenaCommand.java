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

import static net.caseif.flint.spleef.Main.INFO_COLOR;
import static net.caseif.flint.spleef.Main.LOCALE_MANAGER;
import static net.caseif.flint.spleef.Main.PREFIX;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.spleef.Main;

import com.google.common.base.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handler for the arena leave command.
 *
 * @author Max Roncacé
 */
public class LeaveArenaCommand {

    public static void handle(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Optional<Challenger> challenger = Main.getMinigame().getChallenger(((Player) sender).getUniqueId());
            if (challenger.isPresent()) {
                challenger.get().removeFromRound();
                LOCALE_MANAGER.getLocalizable("message.info.command.leave.success").withPrefix(PREFIX + INFO_COLOR)
                        .sendTo(sender);
            } else {
                sender.sendMessage(LOCALE_MANAGER.getLocalizable("message.error.general.in-game")
                        .withPrefix(PREFIX + INFO_COLOR).localize());
            }
        }
    }

}
