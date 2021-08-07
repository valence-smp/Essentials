package net.essentialsx.discordlink.rolesync;

import net.essentialsx.api.v2.services.discord.InteractionRole;
import net.essentialsx.discordlink.EssentialsDiscordLink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.earth2me.essentials.I18n.tl;

public class RoleSyncManager {
    private final static Logger logger = Logger.getLogger("EssentialsDiscordLink");
    private final EssentialsDiscordLink ess;
    private final Map<String, InteractionRole> groupToRoleMap = new HashMap<>();
    private final Map<String, String> roleIdToGroupMap = new HashMap<>();

    public RoleSyncManager(final EssentialsDiscordLink ess) {
        this.ess = ess;
        onReload();
        //this.ess.getEss().runTaskTimerAsynchronously(() -> {

        //}, 1200, ess.getSettings().getRoleSyncResyncDelay() * 1200L);
    }

    public void onReload() {
        groupToRoleMap.clear();
        roleIdToGroupMap.clear();

        final List<String> groups = ess.getEss().getPermissionsHandler().getGroups();

        for (final Map.Entry<String, String> entry : ess.getSettings().getRoleSyncGroups().entrySet()) {
            if (isExampleRole(entry.getValue())) {
                continue;
            }

            final String group = entry.getKey();
            final InteractionRole role = ess.getApi().getRole(entry.getValue());
            if (!groups.contains(group)) {
                logger.warning(tl("discordLinkInvalidGroup", group, entry.getValue(), groups));
                continue;
            }
            if (role == null) {
                logger.warning(tl("discordLinkInvalidRole", entry.getValue(), group));
                continue;
            }

            if (role.isManaged() || role.isPublicRole()) {
                logger.warning(tl("discordLinkInvalidRoleManaged", role.getName(), role.getId()));
                continue;
            }

            if (!role.canInteract()) {
                logger.warning(tl("discordLinkInvalidRoleInteract", role.getName(), role.getId()));
                continue;
            }

            groupToRoleMap.put(group, role);
        }

        for (final Map.Entry<String, String> entry : ess.getSettings().getRoleSyncRoles().entrySet()) {
            if (isExampleRole(entry.getKey())) {
                continue;
            }

            final InteractionRole role = ess.getApi().getRole(entry.getKey());
            final String group = entry.getValue();
            if (role == null) {
                logger.warning(tl("discordLinkInvalidRole", entry.getKey(), group));
                continue;
            }
            if (!groups.contains(group)) {
                logger.warning(tl("discordLinkInvalidGroup", group, entry.getKey(), groups));
                continue;
            }

            roleIdToGroupMap.put(role.getId(), group);
        }
    }

    private boolean isExampleRole(final String role) {
        return role.equals("0") || role.equals("11111111111111111") || role.equals("22222222222222222") || role.equals("33333333333333333");
    }
}
