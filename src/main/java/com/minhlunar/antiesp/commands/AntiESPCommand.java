package com.minhlunar.antiesp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.minhlunar.antiesp.manager.ESPDetectionManager;
import com.minhlunar.antiesp.manager.DeepslateObfuscationManager;
import java.util.Map;

public class AntiESPCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ESPDetectionManager espDetectionManager;
    private final DeepslateObfuscationManager deepslateObfuscationManager;

    public AntiESPCommand(JavaPlugin plugin, ESPDetectionManager espDetectionManager, DeepslateObfuscationManager deepslateObfuscationManager) {
        this.plugin = plugin;
        this.espDetectionManager = espDetectionManager;
        this.deepslateObfuscationManager = deepslateObfuscationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("antiesp.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "status":
                showStatus(sender);
                break;
            case "reset":
                resetAll(sender);
                break;
            case "check":
                if (args.length > 1) {
                    checkPlayer(sender, args[1]);
                } else {
                    sender.sendMessage("§cUsage: /antiesp check <player>");
                }
                break;
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage("§a✓ AntiESP config reloaded!");
                break;
            case "deepslate":
                if (sender instanceof Player) {
                    showDeepslateStatus((Player) sender);
                } else {
                    sender.sendMessage("§cThis command can only be used by players!");
                }
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand. Use /antiesp help");
        }

        return true;
    }

    private void showStatus(CommandSender sender) {
        Map<String, Integer> suspiciousPlayers = espDetectionManager.getSuspiciousPlayers();

        sender.sendMessage("§6=== AntiESP Status ===");
        sender.sendMessage("§aPlugin: §2Enabled");
        sender.sendMessage("§aDeepslate Obfuscation: §2Enabled");
        sender.sendMessage("§7  • Hidden at Y >= " + deepslateObfuscationManager.getObfuscationStartHeight());
        sender.sendMessage("§7  • Shown at Y <= " + deepslateObfuscationManager.getShowDeepslateHeight());
        sender.sendMessage("§aSuspicious Players: §2" + suspiciousPlayers.size());

        if (suspiciousPlayers.isEmpty()) {
            sender.sendMessage("§aNo suspicious players detected!");
        } else {
            suspiciousPlayers.forEach((player, level) ->
                sender.sendMessage("  §c" + player + " §6(Suspicion: " + level + ")")
            );
        }
    }

    private void checkPlayer(CommandSender sender, String playerName) {
        int suspicionLevel = espDetectionManager.getSuspicionLevel(playerName);
        sender.sendMessage("§6Player: §2" + playerName);
        sender.sendMessage("§6Suspicion Level: §2" + suspicionLevel);

        if (suspicionLevel >= 5) {
            sender.sendMessage("§c⚠ This player is highly suspicious!");
        }
    }

    private void showDeepslateStatus(Player player) {
        boolean shouldShow = deepslateObfuscationManager.shouldShowDeepslate(player);
        int playerY = player.getLocation().getBlockY();
        int startHeight = deepslateObfuscationManager.getObfuscationStartHeight();
        int showHeight = deepslateObfuscationManager.getShowDeepslateHeight();

        player.sendMessage("§6=== Deepslate Obfuscation Status ===");
        player.sendMessage("§aYour Y Position: §2" + playerY);
        player.sendMessage("§aObfuscation Range:");
        player.sendMessage("§7  • Hidden: Y ≥ " + startHeight);
        player.sendMessage("§7  • Shown: Y ≤ " + showHeight);
        
        if (shouldShow) {
            player.sendMessage("§a✓ Deepslate is §aVISIBLE");
        } else {
            player.sendMessage("§c✗ Deepslate is §cOBFUSCATED (hidden)");
            player.sendMessage("§eMove down to Y ≤ " + showHeight + " to see deepslate");
        }
    }

    private void resetAll(CommandSender sender) {
        // Get all suspicious players and reset them
        espDetectionManager.getSuspiciousPlayers().keySet().forEach(
            playerName -> espDetectionManager.resetSuspicion(playerName)
        );
        sender.sendMessage("§a✓ All suspicion levels have been reset!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== AntiESP Commands ===");
        sender.sendMessage("§a/antiesp status §7- Show all suspicious players");
        sender.sendMessage("§a/antiesp check <player> §7- Check player suspicion level");
        sender.sendMessage("§a/antiesp deepslate §7- Check deepslate obfuscation status");
        sender.sendMessage("§a/antiesp reset §7- Reset all suspicion levels");
        sender.sendMessage("§a/antiesp reload §7- Reload plugin config");
        sender.sendMessage("§a/antiesp help §7- Show this help message");
    }
}