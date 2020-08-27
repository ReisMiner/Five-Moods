package xyz.ReisMiner;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Bot extends ListenerAdapter {
    static int counter=0;
    static JDABuilder builder;
    //five moods menu website
    static String url = "https://siemens.sv-restaurant.ch/de/menuplan/five-moods/";
    static Document document;

    //i have no idea what this is for. the ide made that xD
    static {
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //bot stuff copied from https://github.com/DV8FromTheWorld/JDA
    public static void main(String[] args) throws LoginException {
        builder = new JDABuilder();
        builder.createLight(token.token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
                .build();

    }

    //bot stuff copied also from https://github.com/DV8FromTheWorld/JDA
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        //f!menu command
        if (msg.getContentRaw().equals("f!menu")) {
            MessageChannel channel = event.getChannel();
            //sending the message
            channel.sendMessage("```" + getMenu(0) + "\n============================\n"
                    + getMenu(1) + "\n============================\n"
                    + getMenu(2) + "\n============================\n"
                    + getMenu(3) +
                    "```").queue();
        }
        //f!help command
        if (msg.getContentRaw().equals("f!help")) {
            MessageChannel channel = event.getChannel();
            long time = System.currentTimeMillis();
            //sending the message
            builder.setActivity(Activity.playing(String.valueOf(time)));
            channel.sendMessage("```To see the menus from today enter f!menu." +
                    "\nThat's the only command.\nWell. If you don't count this one.```").queue();
        }
    }
    //changing activity every 10 seconds to cycle through the menus from today
    public void onReady(ReadyEvent event) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                event.getJDA().getPresence().setActivity(Activity.playing("f!menu | "+document.select("body")
                        .get(0).select(".item-content .menu-title").get(counter).text()+ " -> "
                        +document.select("body").get(0).select(".item-content .menu-prices").get(counter).text()));
                counter++;
                if(counter==4){
                    counter=0;
                }
            }
        }, 0, 10000 );
    }
    //getting the text of the html
    public static String getMenu(int number) {
        Element body = document.select("body").get(0);
        Element menuline = body.select(".item-content .menuline").get(number);
        Element menu_title = body.select(".item-content .menu-title").get(number);
        Element menu_description = body.select(".item-content .menu-description").get(number);
        Element menu_prices = body.select(".item-content .menu-prices").get(number);
        return menuline.text() + menu_title.text() + menu_description.text() + menu_prices.text();
    }
}