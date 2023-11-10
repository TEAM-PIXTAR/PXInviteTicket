package com.github.luriel0228.pxinviteticket.message;

import lombok.Getter;

@Getter
public enum MessageKey {
    /* --------------- NORMAL ---------------*/
    PREFIX("normal.prefix"),
    RELOAD_CONFIG("normal.reload_config"),

    /* --------------- ERROR ---------------*/
    PLAYER_ONLY("error.player_only"),
    NO_PERMISSION("error.no_permission"),
    WRONG_COMMAND("error.wrong_command"),
    MISSING_PLAYER("error.missing_player"),
    UNKNOWN_PLAYER("error.unknown_player"),
    ALREADY_INVITED("error.already_invited"),
    MAX_INVITES_REACHED("max_invites_reached"),
    NO_INVITED_PLAYERS("no_invited_players"),

    /* --------------- MAIN ---------------*/
    SET_INVITE_SUCCESS("main.invite_success"),
    INVITED_PLAYER("main.invited_player");

    private final String key;

    MessageKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
