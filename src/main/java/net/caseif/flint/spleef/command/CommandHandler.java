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

import static net.caseif.flint.spleef.Main.ERROR_COLOR;
import static net.caseif.flint.spleef.Main.PREFIX;

import net.caseif.flint.spleef.Main;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

/**
 * Handler for all FlintSpleef commands.
 *
 * @author Max Roncacé
 */
//TODO: no idea how commands work
public class CommandHandler implements CommandExecutor {

    public CommandResult execute(CommandSource src, CommandContext args) {
        String usage;
        String badArgsMsg = Main.getString("message.error.general.bad-args");
        String tooFewArgsMsg = Main.getString("message.error.general.too-few-args");
        if (args.getOne("subcommand").isPresent()) {
            String subCmd = args.<String>getOne("subcommand").get();
            if (subCmd.equalsIgnoreCase("arena")) {
                usage = getUsageMessage("/fs arena [command]");
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("create")) {
                        CreateArenaCommand.handle(src,  args);
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        RemoveArenaCommand.handle(src, args);
                    } else {
                        sender.sendMessage(PREFIX + ERROR_COLOR
                                + badArgsMsg.localizeFor(sender) + " " + usageMsg.localizeFor(sender));
                    }
                } else {
                    sender.sendMessage(PREFIX + ERROR_COLOR
                            + tooFewArgsMsg.localizeFor(sender) + " " + usageMsg.localizeFor(sender));
                }
            } else if (subCmd.equalsIgnoreCase("join")) {
                JoinArenaCommand.handle(src, args);
            } else if (subCmd.equalsIgnoreCase("leave")) {
                LeaveArenaCommand.handle(src, args);
            } else {
                sender.sendMessage(PREFIX + ERROR_COLOR
                        + badArgsMsg.localizeFor(sender) + " " + usageMsg.localizeFor(sender));
            }
        } else {
            sender.sendMessage(PREFIX + ERROR_COLOR
                    + tooFewArgsMsg.localizeFor(sender) + " " + usageMsg.localizeFor(sender));
        }
        return true;
    }

    private String getUsageMessage(String usage) {
        return Main.getString("message.error.general.usage", "/fs [command]", usage);
    }

}
