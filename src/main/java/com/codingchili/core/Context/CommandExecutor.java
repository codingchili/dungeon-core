package com.codingchili.core.Context;

import java.util.*;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Configuration.System.LauncherSettings;
import com.codingchili.core.Exception.NoSuchCommandException;
import com.codingchili.core.Logging.*;
import com.codingchili.core.Security.TokenRefresher;

/**
 * @author Robin Duda
 */
public class CommandExecutor {
    private static final String GENERATE_SECRETS = "--generate-secrets";
    private static final String GENERATE_TOKENS = "--generate-tokens";
    private static final String GENERATE_PRESHARED = "--generate-preshared";
    private static final String GENERATE = "--generate";
    private static final String HELP = "--help";
    private final Logger logger = new ConsoleLogger();
    private final LaunchContext context;
    private final LauncherSettings settings;
    private boolean success = true;

    public CommandExecutor(LaunchContext context) {
        this.context = context;
        this.settings = context.settings();

        if (context.args().length != 0) {
            execute();
        } else {
            success = false;
        }
    }

    private void execute() {
        switch (context.args()[0]) {
            case GENERATE_PRESHARED:
                new TokenRefresher(logger).preshare();
                break;
            case GENERATE_SECRETS:
                new TokenRefresher(logger).secrets();
                break;
            case GENERATE_TOKENS:
                new TokenRefresher(logger).tokens();
                break;
            case GENERATE:
                new TokenRefresher(logger).all();
                break;
            case HELP:
                help();
                break;
            default:
                success = false;
        }
    }

    private void help() {
        logger.level(Level.STARTUP)
                .log("=================================================== HELP ====================================================")
                .log("\t\t<block-name>\t\tdeploys the services configured in the given block.")
                .log("\t\t<remote-name>\t\tdeploys configured blocks on a remote host.")
                .log("\t\t" + GENERATE_PRESHARED + "\tgenerates pre-shared keys for authentication.")
                .log("\t\t" + GENERATE_SECRETS + "\tgenerates authentication secrets")
                .log("\t\t" + GENERATE_TOKENS + "\tgenerates tokens from existing secrets.")
                .log("\t\t" + GENERATE + "\t\tgenerate secrets, tokens and preshared keys.")
                .log("\t\t" + HELP + "\t\t\tprints this help text.")
                .log("");

        List<BlockRow> blocks = new ArrayList<>();

        logger.log("\t\t" + Strings.CONFIGURED_BLOCKS + "\t\tremotes available", Level.WARNING);
        settings.getBlocks().keySet()
                .forEach(block -> {
                    BlockRow row = new BlockRow(block);
                    settings.getHosts().entrySet().stream()
                            .filter(entry -> entry.getValue().equals(block))
                            .map(Map.Entry::getKey)
                            .forEach(row.remotes::add);
                    blocks.add(row);
                });
        blocks.forEach(block -> {
            logger.log(block.toString(), Level.PURPLE);
        });
    }

    private class BlockRow {
        final List<String> remotes = new ArrayList<>();
        public final String block;

        BlockRow(String block) {
            this.block = block;
        }

        @Override
        public String toString() {
            String string = "\t\t" + block + "\t\t\t\t\t";

            for (int i = 0; i < remotes.size(); i++) {
                if (i == 0) string += "[";

                string += remotes.get(i);

                if (i < remotes.size() - 1) string += ", ";
                if (i == remotes.size() - 1) string += "]";
            }

            return string;
        }
    }

    public boolean success() {
        return success;
    }

    public String getMessage() {
        return new NoSuchCommandException((context.args().length == 0) ? "" : context.args()[0]).getMessage();
    }
}
