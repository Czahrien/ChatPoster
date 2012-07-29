/**
 * File: ChatPoster.java
 * Author: Czahrien <czahrien@gmail.com>
 * Description: Posts chat messages from the server to a php page that adds them
 * to a database.
 * 
 */
package net.mctitan.ChatPoster;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.ChatMode;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
/**
 *
 * @author Czahrien
 */
public class ChatPoster extends JavaPlugin implements Listener {
    boolean usingFactions;
    String host;
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        host = getConfig().getString("posthost");
        
        if(getServer().getPluginManager().getPlugin("Factions") != null) {
            usingFactions = true;
        } else {
            usingFactions = false;
        }
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void chat(PlayerChatEvent e) {
        String tag = "";
        final int type;
        if(usingFactions) {
            FPlayer p = FPlayers.i.get(e.getPlayer());
            tag = p.getTag();
            if(!tag.equals("")) {
                tag += " ";
            }
            type = p.getChatMode().ordinal();
        } else {
            type = 0;
        }
        final String player = tag + e.getPlayer().getName();
        final String msg = e.getMessage();
        final long time = System.currentTimeMillis();
        
        // We want this to be in a new thread to not lag up the server.
        // Since I am lazy and do not want to deal with synchronization each
        // message gets it's own threads.
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(player,"UTF-8")
                    + "&" + URLEncoder.encode("body", "UTF-8") + "=" + URLEncoder.encode(msg,"UTF-8")
                    + "&" + URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode("" + time,"UTF-8")
                    + "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("" + type,"UTF-8");

                    URL url = new URL(host);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoOutput(true);
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(data);
                    wr.flush();
                    wr.close();
                    conn.getResponseMessage();
                    conn.disconnect();
                } catch(Exception ex) {ex.printStackTrace();}
            }
        });
            
        t.start();
        
    }
    
}
