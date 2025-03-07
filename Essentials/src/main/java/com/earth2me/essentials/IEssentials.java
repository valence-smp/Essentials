package com.earth2me.essentials;

import com.earth2me.essentials.api.IItemDb;
import com.earth2me.essentials.api.IJails;
import com.earth2me.essentials.api.IWarps;
import com.earth2me.essentials.commands.IEssentialsCommand;
import com.earth2me.essentials.commands.PlayerNotFoundException;
import com.earth2me.essentials.perm.PermissionsHandler;
import com.earth2me.essentials.updatecheck.UpdateChecker;
import com.earth2me.essentials.userstorage.IUserMap;
import net.ess3.provider.Provider;
import net.essentialsx.api.v2.services.BalanceTop;
import net.essentialsx.api.v2.services.mail.MailService;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public interface IEssentials extends Plugin {
    void addReloadListener(IConf listener);

    void reload();

    Map<String, IEssentialsCommand> getCommandMap();

    List<String> onTabCompleteEssentials(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IEssentialsModule module);

    boolean onCommandEssentials(CommandSender sender, Command command, String commandLabel, String[] args, ClassLoader classLoader, String commandPath, String permissionPrefix, IEssentialsModule module);

    @Deprecated
    User getUser(Object base);

    User getUser(UUID base);

    User getUser(String base);

    User getUser(Player base);

    User matchUser(Server server, User sourceUser, String searchTerm, Boolean getHidden, boolean getOffline) throws PlayerNotFoundException;

    boolean canInteractWith(CommandSource interactor, User interactee);

    boolean canInteractWith(User interactor, User interactee);

    I18n getI18n();

    User getOfflineUser(String name);

    World getWorld(String name);

    int broadcastMessage(String message);

    int broadcastMessage(IUser sender, String message);

    int broadcastMessage(IUser sender, String message, Predicate<IUser> shouldExclude);

    int broadcastMessage(String permission, String message);

    void broadcastTl(String tlKey, Object... args);

    void broadcastTl(IUser sender, String tlKey, Object... args);

    void broadcastTl(IUser sender, String permission, String tlKey, Object... args);

    void broadcastTl(IUser sender, Predicate<IUser> shouldExclude, String tlKey, Object... args);

    void broadcastTl(IUser sender, Predicate<IUser> shouldExclude, boolean parseKeywords, String tlKey, Object... args);

    ISettings getSettings();

    BukkitScheduler getScheduler();

    IJails getJails();

    IWarps getWarps();

    Worth getWorth();

    Backup getBackup();

    Kits getKits();

    RandomTeleport getRandomTeleport();

    UpdateChecker getUpdateChecker();

    BukkitTask runTaskAsynchronously(Runnable run);

    BukkitTask runTaskLaterAsynchronously(Runnable run, long delay);

    BukkitTask runTaskTimerAsynchronously(Runnable run, long delay, long period);

    int scheduleSyncDelayedTask(Runnable run);

    int scheduleSyncDelayedTask(Runnable run, long delay);

    int scheduleSyncRepeatingTask(Runnable run, long delay, long period);

    PermissionsHandler getPermissionsHandler();

    AlternativeCommandsHandler getAlternativeCommandsHandler();

    void showError(CommandSource sender, Throwable exception, String commandLabel);

    IItemDb getItemDb();

    IUserMap getUsers();

    @Deprecated
    UserMap getUserMap();

    BalanceTop getBalanceTop();

    EssentialsTimer getTimer();

    MailService getMail();

    /**
     * Get a list of players who are vanished.
     *
     * @return A list of players who are vanished
     * @deprecated Use {@link net.ess3.api.IEssentials#getVanishedPlayersNew()} where possible.
     */
    @Deprecated
    List<String> getVanishedPlayers();

    Collection<Player> getOnlinePlayers();

    Iterable<User> getOnlineUsers();

    PluginCommand getPluginCommand(String cmd);

    ProviderFactory getProviders();

    default <P extends Provider> P provider(final Class<P> providerClass) {
        return getProviders().get(providerClass);
    }
}
