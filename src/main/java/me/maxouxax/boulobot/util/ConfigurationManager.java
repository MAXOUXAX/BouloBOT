package me.maxouxax.boulobot.util;

import me.maxouxax.boulobot.BOT;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    private final File configFile;
    private final Map<String, String> configKeys = new HashMap<>();
    private final BOT bot;

    public ConfigurationManager(String fileName) {
        this.bot = BOT.getInstance();
        this.configFile = new File(fileName);
    }

    public void loadData() throws IOException {
        if(!configFile.exists()){
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
        }

        try{
            JSONReader reader = new JSONReader(configFile);
            JSONArray array = reader.toJSONArray();

            for(int i = 0; i < array.length(); i++)
            {
                JSONObject object = array.getJSONObject(i);
                configKeys.put(object.getString("key"), object.getString("value"));
            }

        }catch(IOException e){
            bot.getErrorHandler().handleException(e);
        }
    }

    public void saveData(){
        JSONArray array = new JSONArray();

        for(Map.Entry<String, String> configKeys : configKeys.entrySet())
        {
            JSONObject object = new JSONObject();
            object.accumulate("key", configKeys.getKey());
            object.accumulate("value", configKeys.getValue());
            array.put(object);
        }

        try(JSONWriter writter = new JSONWriter(configFile)){

            writter.write(array);
            writter.flush();

        }catch(IOException ioe){
            bot.getErrorHandler().handleException(ioe);
        }
    }

    public String getStringValue(String key){
        return configKeys.getOrDefault(key, "");
    }

    public Long getLongValue(String key){
        return Long.valueOf(configKeys.getOrDefault(key, "0"));
    }

    public void setValue(String key, String value, boolean save){
        configKeys.put(key, value);
        if(save){
            saveData();
        }
    }

}
