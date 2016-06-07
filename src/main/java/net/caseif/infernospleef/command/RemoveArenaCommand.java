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

import static net.caseif.infernospleef.Main.EM_COLOR;
import static net.caseif.infernospleef.Main.ERROR_COLOR;
import static net.caseif.infernospleef.Main.INFO_COLOR;
import static net.caseif.infernospleef.Main.getString;
import static net.caseif.infernospleef.Main.withPrefix;

import net.caseif.infernospleef.Main;

import com.google.common.base.Optional;
import net.caseif.flint.arena.Arena;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for the remove arena command.
 *
 * @author Max Roncacé
 */
public class RemoveArenaCommand implements CommandExecutor {

    private static final List<String> warned = new ArrayList<>();

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) {
        String arenaId = args.<String>getOne("arena").orElse(null);
        if (arenaId == null) {
            sender.sendMessage(withPrefix(Text.builder(getString("message.error.general.too-few-args"))
                    .append(Text.of(getString("message.error.general.usage", "/fs removearena [arena]")))
                    .color(ERROR_COLOR).build()));
            return CommandResult.empty();
        }

        Optional<Arena> arena = Main.getMinigame().getArena(arenaId);
        if (!arena.isPresent()) {
            sender.sendMessage(Text.builder("Arena with ID ").append(Text.builder(arenaId).color(EM_COLOR).build())
                    .append(Text.of(" does not exist")).color(ERROR_COLOR).build());
            return CommandResult.empty();
        }

        if (arena.get().getRound().isPresent() && !warned.contains(sender.getName())) {
            sender.sendMessage(withPrefix(Text.builder(getString("message.info.command.remove.contains-round",
                    arena.get().getDisplayName()))
                    .color(ERROR_COLOR).build()));
            warned.add(sender.getName());
            return CommandResult.empty();
        }

        if (arena.get().getRound().isPresent()) {
            sender.sendMessage(withPrefix(Text.builder(getString("message.info.command.remove.round-end"))
                    .color(INFO_COLOR).build()));
            arena.get().getRound().get().end();
        }
        warned.remove(sender.getName());
        String id = arena.get().getId();
        String name = arena.get().getDisplayName();
        Main.getMinigame().removeArena(arena.get());
        sender.sendMessage(withPrefix(Text.builder(getString("message.info.command.remove.success", name, id))
                .color(INFO_COLOR).build()));
        return CommandResult.success();
    }

}
