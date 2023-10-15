package com.github.kaerum.btwcarpetlite.utils;

import btw.world.util.BlockPos;
import com.github.kaerum.btwcarpetlite.BTWCarpetLite;
import com.github.kaerum.btwcommander.adaptations.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Review @ 1.8
 */

public class Messenger
{
    private static String _getChatComponentFromDesc(String message, String previous_message)
    {
        if (message.equalsIgnoreCase(""))
        {
            return "";
        }
        if (Character.isWhitespace(message.charAt(0)))
        {
            message = "w"+message;
        }
        int limit = message.indexOf(' ');
        String desc = message;
        String str = "";
        if (limit >= 0)
        {
            desc = message.substring(0, limit);
            str = message.substring(limit+1);
        }
        if (desc.charAt(0) == '/') // deprecated
        {
            return previous_message;
        }
        if (desc.charAt(0) == '?')
        {
            return previous_message;
        }
        if (desc.charAt(0) == '!')
        {
            return previous_message;
        }
        if (desc.charAt(0) == '^')
        {
            return previous_message;
        }
        return str;
    }
    public static String tp(String desc, Vec3 pos) { return tp(desc, pos.xCoord, pos.yCoord, pos.zCoord); }
    public static String tp(String desc, BlockPos pos) { return tp(desc, pos.x, pos.y, pos.z); }
    public static String tp(String desc, double x, double y, double z) { return tp(desc, (float)x, (float)y, (float)z);}
    public static String tp(String desc, float x, float y, float z)
    {
        return _getCoordsTextComponent(desc, x, y, z, false);
    }
    public static String tp(String desc, int x, int y, int z)
    {
        return _getCoordsTextComponent(desc, (float)x, (float)y, (float)z, true);
    }

    public static String dbl(String style, double double_value)
    {
        return c(String.format("%s %.1f",style,double_value),String.format("^w %f",double_value));
    }
    public static String dbls(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%.1f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }
    public static String dblf(String style, double ... doubles)
    {
        StringBuilder str = new StringBuilder(style + " [ ");
        String prefix = "";
        for (double dbl : doubles)
        {
            str.append(String.format("%s%f", prefix, dbl));
            prefix = ", ";
        }
        str.append(" ]");
        return c(str.toString());
    }
    public static String dblt(String style, double ... doubles)
    {
        List<Object> components = new ArrayList<>();
        components.add(style+" [ ");
        String prefix = "";
        for (double dbl:doubles)
        {

            components.add(String.format("%s %s%.1f",style, prefix, dbl));
            components.add("?"+dbl);
            components.add("^w "+dbl);
            prefix = ", ";
        }
        //components.remove(components.size()-1);
        components.add(style+"  ]");
        return c(components.toArray(new Object[0]));
    }

    private static String _getCoordsTextComponent(String style, float x, float y, float z, boolean isInt)
    {
        String text;
        String command;
        if (isInt)
        {
            text = String.format("%s [ %d, %d, %d ]",style, (int)x,(int)y, (int)z );
            command = String.format("!/tp %d %d %d",(int)x,(int)y, (int)z);
        }
        else
        {
            text = String.format("%s [ %.1f, %.1f, %.1f]",style, x, y, z);
            command = String.format("!/tp %.3f %.3f %.3f",x, y, z);
        }
        return c(text, command);
    }

    //message source
    public static void m(ServerCommandSource source, Object ... fields)
    {
        if (source != null) {
            source.sendFeedback(
                    Messenger.c(fields),
                    source.getServer() != null && source.getServer().worldServerForDimension(0) != null
            );
        }
    }
    public static void m(EntityPlayer player, Object ... fields)
    {
        player.sendChatToPlayer(Messenger.c(fields));
    }

    /*
    composes single line, multicomponent message, and returns as one chat messagge
     */
    public static String c(Object ... fields)
    {
        String message = "";
        String previous_component = null;
        for (Object o: fields)
        {
            if (o instanceof String)
            {
                message = message + ((String)o);
                previous_component = (String)o;
                continue;
            }
            String txt = o.toString();
            String comp = _getChatComponentFromDesc(txt,previous_component);
            if (comp != previous_component) {
                message = message + comp;
            }
            previous_component = comp;
        }
        return message;
    }

    //simple text

    public static String s(String text)
    {
        return s(text,"");
    }
    public static String s(String text, String style)
    {
        return text;
    }




    public static void send(EntityPlayer player, Collection<String> lines)
    {
        lines.forEach(message -> player.sendChatToPlayer(message));
    }
    public static void send(ServerCommandSource source, Collection<String> lines)
    {
        lines.stream().forEachOrdered((s) -> source.sendFeedback(s, false));
    }


    public static void print_server_message(MinecraftServer server, String message)
    {
        if (server == null) {
            BTWCarpetLite.LOGGER.log(Level.SEVERE, "Message not delivered: " + message);
            return;
        }
        server.sendChatToPlayer(message);
        String txt = c("gi "+message);
        for (WorldServer worldServer : server.worldServers) {
            send_message_to_all_world_players(worldServer, txt);
        }
    }

    private static void send_message_to_all_world_players(WorldServer worldServer, String message) {
        for (Object entityPlayer : worldServer.playerEntities) {
            ((EntityPlayer)entityPlayer).sendChatToPlayer(message);
        }
    }
}

