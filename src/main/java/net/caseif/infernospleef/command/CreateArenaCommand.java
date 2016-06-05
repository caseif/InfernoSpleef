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

import net.caseif.infernospleef.Main;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;

/**
 * Handler for the arena creation command.
 *
 * @author Max Roncacé
 */
public class CreateArenaCommand implements CommandExecutor {

    public static final BiMap<UUID, Integer> WIZARDS = HashBiMap.create();
    public static final BiMap<UUID, Object[]> WIZARD_INFO = HashBiMap.create();

    public static final int WIZARD_ID = 0;
    public static final int WIZARD_NAME = 1;
    public static final int WIZARD_FIRST_BOUND = 2;
    public static final int WIZARD_SECOND_BOUND = 3;
    public static final int WIZARD_SPAWN_POINT = 4;

    @Override
    public CommandResult execute(CommandSource sender, CommandContext args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.builder(Main.getString("message.error.general.in-game")).insert(0, Main.PREFIX)
                    .color(Main.ERROR_COLOR).build());
            return CommandResult.empty();
        }

        if (WIZARDS.containsKey(((Player) sender).getUniqueId())) {
            sender.sendMessage(Text.builder(Main.getString("message.error.command.create.already"))
                    .insert(0, Main.PREFIX).color(Main.ERROR_COLOR).build());
            return CommandResult.empty();
        }

        WIZARDS.put(((Player) sender).getUniqueId(), 0);
        WIZARD_INFO.put(((Player) sender).getUniqueId(), new Object[4]);
        sender.sendMessage(Text.builder(Main.getString("message.info.command.create.welcome")).insert(0, Main.PREFIX)
                .color(Main.INFO_COLOR).build());
        sender.sendMessage(Text.builder(Main.getString("message.info.command.create.exit-note",
                Main.getString("message.info.command.create.cancel-keyword"))).insert(0, Main.PREFIX)
                .color(Main.INFO_COLOR).build());

        return CommandResult.success();
    }

}
