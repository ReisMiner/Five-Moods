package xyz.ReisMiner;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.collections4.Get;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;

public class Bot extends ListenerAdapter {
    static int counter = 0;
    static boolean gibz_we = false;
    static String date = "";
    static JDABuilder builder;
    //five moods menu website
    static String url = "https://siemens.sv-restaurant.ch/de/menuplan/five-moods/";
    static String gibz = "https://zfv.ch/de/microsites/restaurant-treff/menuplan";
    static Document document, documentGibz;

    static void load() {
        try {
            document = Jsoup.connect(url).get();
            documentGibz = Jsoup.connect(gibz).get();
            System.out.println("loaded Sites");
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
        load();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        //f!moods command
        if (msg.getContentRaw().equalsIgnoreCase("f!moods")) {
            if (!LocalDate.now().toString().equals(date)) {
                date = LocalDate.now().toString();
                System.out.println(date + " localdate -> " + LocalDate.now().toString());
                load();
            }
            MessageChannel channel = event.getChannel();
            //sending the message
            channel.sendMessage("```" + getMenu(0) + "\n============================\n"
                    + getMenu(1) + "\n============================\n"
                    + getMenu(2) + "\n============================\n"
                    + getMenu(3) +
                    "```").queue();
        }
        //f!help command
        if (msg.getContentRaw().equalsIgnoreCase("f!help")) {
            MessageChannel channel = event.getChannel();
            //sending the message
            channel.sendMessage("```Commands für d Menüs sind f!gibz oder f!moods```").queue();
        }
        if (msg.getContentRaw().equalsIgnoreCase("f!gibz")) {
            int gibz_count=0;
            gibz_we = false;
            System.out.println(LocalDate.now());
            MessageChannel channel = event.getChannel();
            String mesg = "```";

            if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                channel.sendMessage("```lol. du gasch am wucheend id schuel? xD```").queue();
                gibz_we = true;
            }
            try {
                for (gibz_count = 0; gibz_count < gibz_count+1; gibz_count++) {
                        GetGIBZ(gibz_count).id();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(gibz_count);
            }
            if (gibz_we == false) {
                for (int j = 0; j < gibz_count; j++) {
                    if (GetGIBZ(j).parent().parent().attr("data-date").contains(LocalDate.now().toString())) {
                        mesg += GetGIBZ(j).text() + "\n=============================================\n";
                    }
                }
                channel.sendMessage(mesg + "```").queue();
            }

        }
    }

    //changing activity every 10 seconds to cycle through the menus from today
    public void onReady(ReadyEvent event) {
        load();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                event.getJDA().getPresence().setActivity(Activity.playing("f!moods | " + document.select("body")
                        .get(0).select(".item-content .menu-title").get(counter).text() + " -> "
                        + document.select("body").get(0).select(".item-content .menu-prices").get(counter).text()));
                counter++;
                if (counter == 4) {
                    counter = 0;
                }
            }
        }, 0, 10000);

    }

    //getting the text of the html
    public static String getMenu(int number) {
        Element body = document.select("body").get(0);
        Element menuline = body.select(".item-content .menuline").get(number);
        Element menu_title = body.select(".item-content .menu-title").get(number);
        Element menu_description = body.select(".item-content .menu-description").get(number);
        Element menu_prices = body.select(".item-content .menu-prices").get(number);
        return menuline.text() + "\n" + menu_title.text() + "\n" + menu_description.text() + "\n" + menu_prices.text();
    }

    public static Element GetGIBZ(int number) {
        Element body = documentGibz.select("body").get(0);
        documentGibz.select(".txt-slide").remove();
        Element menuline = body.select(".menu .txt-hold").get(number);
        return menuline;
    }
}

