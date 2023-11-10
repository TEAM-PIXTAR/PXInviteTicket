package com.github.luriel0228.pxinviteticket.message;

import com.github.luriel0228.pxinviteticket.util.ColorCode;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class Message {
    public static Message instance;

    public HashMap<MessageKey, String> messageMap;

    public Message(FileConfiguration fileConfiguration) {
        FileConfiguration configuration = MessageConfig.messageConfig;
        messageMap = new HashMap<>();

        for (MessageKey key : MessageKey.values()) {
            String msg = configuration.getString(key.getKey());
            messageMap.put(key, (msg == null) ? "Invalid Message." : msg);
        }
    }

    private String getPrefix() {
        return messageMap.get(MessageKey.PREFIX);
    }

    public String getMessage(MessageKey key) {
        return ColorCode.colorCodes(getPrefix() + messageMap.get(key));
    }

    public static Message getInstance() {
        return instance;
    }
}