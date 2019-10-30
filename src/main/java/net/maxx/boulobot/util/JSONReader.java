package net.maxx.boulobot.util;

import net.maxx.boulobot.BOT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JSONReader{

    private final String json;
    private final BOT botDiscord;

    public JSONReader(String path, BOT botDiscord) throws IOException
    {
        this(new File(path), botDiscord);
    }

    public JSONReader(File file, BOT botDiscord) throws IOException
    {
        this(new InputStreamReader(new FileInputStream(file)), botDiscord);
    }

    public JSONReader(Reader reader, BOT botDiscord) throws IOException
    {
        this(new BufferedReader(reader), botDiscord);
    }

    public JSONReader(BufferedReader reader, BOT botDiscord) throws IOException
    {
        json = load(reader);
        this.botDiscord = botDiscord;
    }

    private String load(BufferedReader reader) throws IOException
    {
        StringBuilder builder = new StringBuilder();

        while(reader.ready()) builder.append(reader.readLine());

        reader.close();

        return builder.length() == 0 ? "[]" : builder.toString();
    }

    public static <E> List<E> toList(String path, BOT botDiscord)
    {
        return toList(new File(path), botDiscord);
    }

    public static <E> List<E> toList(File file, BOT botDiscord)
    {
        try
        {
            return toList(new InputStreamReader(new FileInputStream(file)), botDiscord);
        }
        catch(IOException e)
        {

        }
        return new ArrayList<>();
    }

    public static <E> List<E> toList(Reader reader, BOT botDiscord)
    {
        return toList(new BufferedReader(reader), botDiscord);
    }

    public static <E> List<E> toList(BufferedReader bufferedReader, BOT botDiscord)
    {
        List<E> list= new ArrayList<>();

        try
        {
            JSONReader reader = new JSONReader(bufferedReader, botDiscord);
            JSONArray array = reader.toJSONArray();
            for(int i = 0; i < array.length(); i++)
            {
                try
                {
                    list.add((E) array.get(i));
                }catch(ClassCastException e){}
            }
        }
        catch(IOException e)
        {
            botDiscord.getErrorHandler().handleException(e);
        }

        return list;
    }

    public static <V> Map<String, V> toMap(String path, BOT botDiscord)
    {
        return toMap(new File(path), botDiscord);
    }

    public static <V> Map<String, V> toMap(File file, BOT botDiscord)
    {
        try
        {
            return toMap(new InputStreamReader(new FileInputStream(file)), botDiscord);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static <V> Map<String, V> toMap(Reader reader, BOT botDiscord)
    {
        return toMap(new BufferedReader(reader), botDiscord);
    }

    public static <V> Map<String, V> toMap(BufferedReader bufferedReader, BOT botDiscord)
    {
        Map<String, V> map = new HashMap<>();

        try
        {
            JSONReader reader = new JSONReader(bufferedReader, botDiscord);
            JSONObject object = reader.toJSONObject();
            for(String key : object.keySet())
            {
                Object obj = object.get(key);
                try
                {
                    map.put(key, (V) object.get(key));
                }
                catch(ClassCastException e) {}
            }
        }
        catch(IOException e)
        {
            botDiscord.getErrorHandler().handleException(e);
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
