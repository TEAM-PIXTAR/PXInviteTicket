package com.github.luriel0228.pxinviteticket.message;

import lombok.Getter;

@Getter
public enum MessageKey {
    /* --------------- NORMAL ---------------*/
    PREFIX("normal.prefix"),
    RELOAD_CONFIG("normal.reload_config"),

    /* --------------- ERROR ---------------*/
    PLAYER_ONLY("error.player_only"),
    SQL_ERROR("error.sql_error"),
    NO_PERMISSION("error.no_permission"),
    WRONG_COMMAND("error.wrong_command"),
    MISSING_PLAYER("error.missing_player"),
    SELF_INVITE("error.self_invite"),
    ALREADY_INVITED("error.already_invited"),
    MAX_INVITES_REACHED("error.max_invites_reached"),
    NO_INVITED_PLAYERS("error.no_invited_players"),

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
