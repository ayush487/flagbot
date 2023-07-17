package com.ayush;

import javax.security.auth.login.LoginException;

import com.ayush.listeners.MessageListener;
import com.ayush.listeners.SlashCommandListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Main 
{
    private final static String BOT_TOKEN = "MTEyOTc4OTMyMDE2NTg2NzY2Mg.GBQb5I.FNo146eci467VR74LlynjNrJ66S9c08QuDPDN4";
    
    public static void main( String[] args ) throws LoginException, InterruptedException
    {
        JDA jda = JDABuilder.createDefault(BOT_TOKEN).addEventListeners(new MessageListener(), new SlashCommandListener()).build().awaitReady();
        
        jda.upsertCommand("guess", "Guess the country name by its flag").queue();;
    }
}