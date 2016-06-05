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

package net.caseif.infernospleef;

import net.caseif.infernospleef.command.CreateArenaCommand;
import net.caseif.infernospleef.command.JoinArenaCommand;
import net.caseif.infernospleef.command.LeaveArenaCommand;
import net.caseif.infernospleef.command.RemoveArenaCommand;
import net.caseif.infernospleef.listener.BlockListener;
import net.caseif.infernospleef.listener.MinigameListener;
import net.caseif.infernospleef.listener.PlayerListener;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import net.caseif.flint.FlintCore;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.LifecycleStage;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.dispatcher.SimpleDispatcher;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Plugin(id = "infernospleef", name = "InfernoSpleef", version = "1.0.0-SNAPSHOT",
        dependencies = @Dependency(id = "inferno", version = "[1.2.0,)"))

public class Main {

    @Inject private Logger logger;

    private static final int MIN_FLINT_VERSION = 1;

    public static final String WAITING_STAGE_ID = "waiting";
    public static final String PREPARING_STAGE_ID = "preparing";
    public static final String PLAYING_STAGE_ID = "playing";

    public static final Text PREFIX = Text.builder("[InfernoSpleef] ").color(TextColors.GREEN).build();
    public static final TextColor INFO_COLOR = TextColors.DARK_AQUA;
    public static final TextColor ERROR_COLOR = TextColors.RED;
    public static final TextColor EM_COLOR = TextColors.GOLD;

    public static ArrayList<ItemType> SHOVELS = new ArrayList<>();
    public static ItemStack SHOVEL;

    private static Main instance;
    private static Minigame mg;

    private static final Map<String, String> STRINGS = new HashMap<>();

    public static int MIN_PLAYERS = Integer.MAX_VALUE;

    static {
        SHOVELS.add(ItemTypes.WOODEN_SHOVEL);
        SHOVELS.add(ItemTypes.STONE_SHOVEL);
        SHOVELS.add(ItemTypes.IRON_SHOVEL);
        SHOVELS.add(ItemTypes.GOLDEN_SHOVEL);
        SHOVELS.add(ItemTypes.DIAMOND_SHOVEL);
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        instance = this;

        try {
            loadStrings();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load strings!", ex);
        }

        if (FlintCore.getApiRevision() < MIN_FLINT_VERSION) {
            getLogger().error(getString("log.error.flint-version", MIN_FLINT_VERSION + ""));
            return;
        }

        // general plugin initialization
        Sponge.getEventManager().registerListeners(this, new BlockListener());
        Sponge.getEventManager().registerListeners(this, new PlayerListener());

        registerCommands();

        //MIN_PLAYERS = getConfig().getInt("min-prep-players"); //TODO
        MIN_PLAYERS = 2;

        //int shovelType = getConfig().getInt("shovel-type"); //TODO
        int shovelType = 3;
        ItemType shovelItem = shovelType >= 0 && shovelType < SHOVELS.size()
                ? SHOVELS.get(shovelType)
                : SHOVELS.get(3);
        SHOVEL = ItemStack.of(shovelItem, 1);

        // Flint initialization
        mg = FlintCore.registerPlugin("InfernoSpleef");
        mg.getEventBus().register(new MinigameListener());

        ImmutableSet<LifecycleStage> stages = ImmutableSet.copyOf(new LifecycleStage[]{
                new LifecycleStage(WAITING_STAGE_ID, -1),
                //new LifecycleStage(PREPARING_STAGE_ID, getConfig().getInt("prep-time")), //TODO
                new LifecycleStage(PREPARING_STAGE_ID, 30),
                //new LifecycleStage(PLAYING_STAGE_ID, getConfig().getInt("round-time")) //TODO
                new LifecycleStage(PLAYING_STAGE_ID, 90)
        });
        mg.setConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES, stages);
        //mg.setConfigValue(ConfigNode.MAX_PLAYERS, getConfig().getInt("arena-size")); //TODO
        mg.setConfigValue(ConfigNode.MAX_PLAYERS, 32);
    }

    /**
     * Gets the plugin's {@link Logger}.
     *
     * @return The logger
     */
    Logger getLogger() {
        return this.logger;
    }

    public static Main getInstance() {
        return instance;
    }

    public static Minigame getMinigame() {
        return mg;
    }

    private void registerCommands() {
        SimpleDispatcher rootCmd = new SimpleDispatcher();

        rootCmd.register(CommandSpec.builder()
                .permission("infernospleef.arena.create")
                .executor(new CreateArenaCommand())
                .build(), "createarena");

        rootCmd.register(CommandSpec.builder()
                .permission("infernospleef.play")
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("arena")))
                .executor(new JoinArenaCommand())
                .build(), "join");

        rootCmd.register(CommandSpec.builder()
                .permission("infernospleef.play")
                .executor(new LeaveArenaCommand())
                .build(), "leave");

        rootCmd.register(CommandSpec.builder()
                .permission("infernospleef.arena.remove")
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("arena")))
                .executor(new RemoveArenaCommand())
                .build(), "removearena");

        Sponge.getCommandManager().register(this, rootCmd);
    }

    private static void loadStrings() throws IOException {
        Properties props = new Properties();
        props.load(Main.class.getResourceAsStream("/strings.properties"));
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            STRINGS.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    public static String getString(String key, String... replacements) {
        String str = STRINGS.get(key);
        for (int i = 0; i < replacements.length; i++) {
            str.replaceAll("%" + (i + 1), replacements[i]);
        }
        return str;
    }

}
