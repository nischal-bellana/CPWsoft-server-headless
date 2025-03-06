package com.CPWsoft.lwjgl3;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.game_states.GameState;
import com.game_states.State;

import java.io.*;
import java.net.*;

import com.CPWsoft.CPWsoft;
import com.CPWsoft.Threads.ServerThread;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.CPWsoft.Threads.nonGameThread;
import com.Connection.RoomConnection;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	
    public static void main(String[] args) throws IOException {
//        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
//        createApplication();
    	ServerThread th = new ServerThread();
    	th.run();
    }
    
    public static HeadlessApplication createHeadlessApplication(RoomConnection roomconnection) {
    	return new HeadlessApplication(new CPWsoft(roomconnection), getHeadlessApplicationConfiguration());
    }
    
    private static HeadlessApplicationConfiguration getHeadlessApplicationConfiguration() {
    	HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
    	
    	configuration.updatesPerSecond = 144;
    	
    	System.out.println("no get Display Mode");
    	
    	return configuration;
    }
    
}