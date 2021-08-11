package net.essentialsx.discordlink;

import com.earth2me.essentials.IEssentialsModule;
import net.ess3.api.IUser;
import net.essentialsx.api.v2.events.discordlink.DiscordLinkStatusChangeEvent;
import net.essentialsx.api.v2.services.discord.InteractionMember;
import net.essentialsx.discordlink.rolesync.RoleSyncManager;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class AccountLinkManager implements IEssentialsModule {
    private static final char[] CODE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final EssentialsDiscordLink ess;
    private final AccountStorage storage;
    private final RoleSyncManager roleSyncManager;

    private final Map<String, UUID> codeToUuidMap = new ConcurrentHashMap<>();

    public AccountLinkManager(EssentialsDiscordLink ess, AccountStorage storage, RoleSyncManager roleSyncManager) {
        this.ess = ess;
        this.storage = storage;
        this.roleSyncManager = roleSyncManager;
    }

    public boolean isLinked(final UUID uuid) {
        return getDiscordId(uuid) != null;
    }

    public boolean isLinked(final String discordId) {
        return getUUID(discordId) != null;
    }

    public String createCode(final UUID uuid) throws IllegalArgumentException {
        synchronized (codeToUuidMap) {
            final Optional<Map.Entry<String, UUID>> prevCode = codeToUuidMap.entrySet().stream().filter(stringUUIDEntry -> stringUUIDEntry.getValue().equals(uuid)).findFirst();
            if (prevCode.isPresent()) {
                throw new IllegalArgumentException(prevCode.get().getKey());
            }

            final String code = generateCode();

            codeToUuidMap.put(code, uuid);
            return code;
        }
    }

    public UUID getPendingUUID(final String code) {
        synchronized (codeToUuidMap) {
            return codeToUuidMap.remove(code);
        }
    }

    public String getDiscordId(final UUID uuid) {
        return storage.getDiscordId(uuid);
    }

    public IUser getUser(final String discordId) {
        final UUID uuid = getUUID(discordId);
        if (uuid == null) {
            return null;
        }
        return ess.getEss().getUser(uuid);
    }

    public UUID getUUID(final String discordId) {
        return storage.getUUID(discordId);
    }

    public boolean removeAccount(final InteractionMember member, final DiscordLinkStatusChangeEvent.Cause cause) {
        final UUID uuid = getUUID(member.getId());
        if (storage.remove(member.getId())) {
            ensureAsync(() -> {
                final IUser user = ess.getEss().getUser(uuid);
                ensureSync(() -> ess.getServer().getPluginManager().callEvent(new DiscordLinkStatusChangeEvent(user, member, member.getId(), false, cause)));
            });
            return true;
        }
        ensureAsync(() -> roleSyncManager.unSync(uuid, member.getId()));
        return false;
    }

    public boolean removeAccount(final IUser user, final DiscordLinkStatusChangeEvent.Cause cause) {
        final String id = getDiscordId(user.getBase().getUniqueId());
        if (storage.remove(user.getBase().getUniqueId())) {
            ess.getApi().getMemberById(id).thenAccept(member -> ensureSync(() ->
                    ess.getServer().getPluginManager().callEvent(new DiscordLinkStatusChangeEvent(user, member, id, false, cause))));
            return true;
        }
        ensureAsync(() -> roleSyncManager.unSync(user.getBase().getUniqueId(), id));
        return false;
    }

    public void registerAccount(final UUID uuid, final InteractionMember member, final DiscordLinkStatusChangeEvent.Cause cause) {
        storage.add(uuid, member.getId());
        ensureAsync(() -> roleSyncManager.sync(uuid, member.getId()));
        ensureAsync(() -> {
            final IUser user = ess.getEss().getUser(uuid);
            ensureSync(() -> ess.getServer().getPluginManager().callEvent(new DiscordLinkStatusChangeEvent(user, member, member.getId(), true, cause)));
        });
    }

    private void ensureSync(final Runnable runnable) {
        if (ess.getServer().isPrimaryThread()) {
            runnable.run();
            return;
        }
        ess.getEss().scheduleSyncDelayedTask(runnable);
    }

    private void ensureAsync(final Runnable runnable) {
        if (!ess.getServer().isPrimaryThread()) {
            runnable.run();
            return;
        }
        ess.getEss().runTaskAsynchronously(runnable);
    }

    private String generateCode() {
        final char[] code = new char[8];
        final Random random = ThreadLocalRandom.current();

        for (int i = 0; i < 8; i++) {
            code[i] = CODE_CHARACTERS[random.nextInt(CODE_CHARACTERS.length)];
        }
        final String result = new String(code);

        if (codeToUuidMap.containsKey(result)) {
            // If this happens, buy a lottery ticket.
            return generateCode();
        }
        return result;
    }
}
