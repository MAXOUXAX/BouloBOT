package net.maxouxax.boulobot.util;

import net.maxouxax.boulobot.BOT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JSONReader{

    private final String json;
    private final BOT bot;

    public JSONReader(String path, BOT bot) throws IOException
    {
        this(new File(path), bot);
    }

    public JSONReader(File file, BOT bot) throws IOException
    {
        this(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), bot);
    }

    public JSONReader(Reader reader, BOT bot) throws IOException
    {
        this(new BufferedReader(reader), bot);
    }

    public JSONReader(BufferedReader reader, BOT bot) throws IOException
    {
        json = load(reader);
        this.bot = bot;
    }

    private String load(BufferedReader reader) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        while(reader.ready()) builder.append(reader.readLine());

        reader.close();

        return builder.length() == 0 ? "[]" : builder.toString();
    }

    public static <E> List<E> toList(String path, BOT bot)
    {
        return toList(new File(path), bot);
    }

    public static <E> List<E> toList(File file, BOT bot)
    {
        try
        {
            return toList(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), bot);
        }
        catch(IOException e)
        {
            bot.getErrorHandler().handleException(e);
        }
        return new ArrayList<>();
    }

    public static <E> List<E> toList(Reader reader, BOT bot)
    {
        return toList(new BufferedReader(reader), bot);
    }

    public static <E> List<E> toList(BufferedReader bufferedReader, BOT bot)
    {
        List<E> list= new ArrayList<>();

        try
        {
            JSONReader reader = new JSONReader(bufferedReader, bot);
            JSONArray array = reader.toJSONArray();
            for(int i = 0; i < array.length(); i++)
            {
                try
                {
                    list.add((E) array.get(i));
                }catch(ClassCastException e){
                    bot.getErrorHandler().handleException(e);
                }
            }
        }
        catch(IOException e)
        {
            bot.getErrorHandler().handleException(e);
        }

        return list;
    }

    public static <V> Map<String, V> toMap(String path, BOT bot)
    {
        return toMap(new File(path), bot);
    }

    public static <V> Map<String, V> toMap(File file, BOT bot)
    {
        try
        {
            return toMap(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), bot);
        }
        catch(IOException e)
        {
            bot.getErrorHandler().handleException(e);
        }
        return new HashMap<>();
    }

    public static <V> Map<String, V> toMap(Reader reader, BOT bot)
    {
        return toMap(new BufferedReader(reader), bot);
    }

    public static <V> Map<String, V> toMap(BufferedReader bufferedReader, BOT bot)
    {
        Map<String, V> map = new HashMap<>();

        try
        {
            JSONReader reader = new JSONReader(bufferedReader, bot);
            JSONObject object = reader.toJSONObject();
            for(String key : object.keySet())
            {
                Object obj = object.get(key);
                try
                {
                    map.put(key, (V) object.get(key));
                }
                catch(ClassCastException e) {
                    bot.getErrorHandler().handleException(e);
                }
            }
        }
        catch(IOException e)
        {
            bot.getErrorHandler().handleException(e);
        }

        return map;
    }

    public JSONArray toJSONArray()
    {
        return new JSONArray(json);
    }

    public JSONObject toJSONObject()
    {
        return new JSONObject(json);
    }
}
