package me.bruno.removerodblocking;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;

public class RemoveRodBlocking implements ClientModInitializer {

    public static final Logger LOGGER = LogUtils.getLogger();

    private static RemoveRodBlocking instance;

    public static RemoveRodBlocking getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
    }
}
