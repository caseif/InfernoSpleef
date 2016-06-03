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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Handler for the arena creation command.
 *
 * @author Max Roncacé
 */
public class CreateArenaCommand {

    public static BiMap<UUID, Integer> WIZARDS = HashBiMap.create();
    public static BiMap<UUID, Object[]> WIZARD_INFO = HashBiMap.create();

    public static final int WIZARD_ID = 0;
    public static final int WIZARD_NAME = 1;
    public static final int WIZARD_FIRST_BOUND = 2;
    public static final int WIZARD_SECOND_BOUND = 3;
    public static final int WIZARD_SPAWN_POINT = 4;

    public static void handle(CommandSender sender, String[] args) {
        if (sender.hasPermission("flintspleef.arena.create")) {
            if (sender instanceof Player) {
                if (!WIZARDS.containsKey(((Player) sender).getUniqueId())) {
                    WIZARDS.put(((Player) sender).getUniqueId(), 0);
                    WIZARD_INFO.put(((Player) sender).getUniqueId(), new Object[4]);
                    LOCALE_MANAGER.getLocalizable("message.info.command.create.welcome").withPrefix(PREFIX + INFO_COLOR)
                            .sendTo(sender);
                    LOCALE_MANAGER.getLocalizable("message.info.command.create.exit-note")
                            .withPrefix(PREFIX + INFO_COLOR).withReplacements(EM_COLOR
                                    + LOCALE_MANAGER.getLocalizable("message.info.command.create.cancel-keyword")
                                    .localizeFor(sender) + INFO_COLOR).sendTo(sender);
                } else {
                    LOCALE_MANAGER.getLocalizable("message.error.command.create.already")
                            .withPrefix(PREFIX + ERROR_COLOR).sendTo(sender);
                }
            } else {
                LOCALE_MANAGER.getLocalizable("message.error.general.in-game").withPrefix(PREFIX + ERROR_COLOR)
                        .sendTo(sender);
            }
        } else {
            LOCALE_MANAGER.getLocalizable("message.error.general.permission").withPrefix(PREFIX + ERROR_COLOR)
                    .sendTo(sender);
        }
    }

}
