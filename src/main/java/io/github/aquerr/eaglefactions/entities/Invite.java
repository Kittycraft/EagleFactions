package io.github.aquerr.eaglefactions.entities;

import java.util.UUID;

public class Invite {
    private String factionName;
    private UUID playerUUID;

    public Invite(String factionName, UUID playerUUID) {
        this.factionName = factionName;
        this.playerUUID = playerUUID;
    }

    public String getFactionName() {
        return factionName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public boolean equals(Object allyInvite) {
        if (!(allyInvite instanceof Invite)) {
            return false;
        }
        if (allyInvite == this) {
            return true;
        }
        return this.factionName.equals(((Invite) allyInvite).factionName) && this.playerUUID.equals(((Invite) allyInvite).playerUUID);
    }

    @Override
    public int hashCode() {
        return factionName.length();
    }
}
