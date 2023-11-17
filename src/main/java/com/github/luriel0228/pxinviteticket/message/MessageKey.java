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
    EMPTY_HAND("error.empty_hand"),
    NO_INVITE_ITEM_SET("error.no_invite_itemset"),
    INVALID_PLAYER("error.invalid_player"),

    /* --------------- MAIN ---------------*/
    GET_INVITE("main.get_invite"),
    CANCEL_INVITE("main.cancel_invite"),
    SET_INVITE_SUCCESS("main.invite_success"),
    INVITED_PLAYER("main.invited_player"),
    INVITE_ITEM_REGISTERED("main.invite_item_registered"),
    NO_INVITE_ITEM("main.no_invite_item"),
    GIVE_INVITE_SUCCESS("main.give_invite_success");

    private final String key;

    MessageKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
