package me.bruno.removerodblocking;

import net.fabricmc.api.ClientModInitializer;

public class RemoveRodBlocking implements ClientModInitializer {

    private static RemoveRodBlocking instance;

    public static RemoveRodBlocking getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
    }
}
