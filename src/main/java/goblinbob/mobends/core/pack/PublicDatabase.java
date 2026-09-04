package goblinbob.mobends.core.pack;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PublicDatabase
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public PackEntry[] packs;

    public static PublicDatabase downloadPublicDatabase(String databaseUrl)
    {
        try
        {
            URL publicDirectoryUrl = new URL(databaseUrl);
            JsonReader fileReader = new JsonReader(new InputStreamReader(publicDirectoryUrl.openStream()));
            Gson gson = new Gson();
            return gson.fromJson(fileReader, PublicDatabase.class);
        }
        catch(JsonSyntaxException e)
        {
            LOGGER.warn("The downloaded database is not proper JSON.");
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to download the public bends pack database", e);
        }
        return null;
    }

    public class PackEntry
    {
        public String name;
        public String displayName;
        public String author;
        public String description;
        public String uploadedDate;
        public String updatedDate;
        public String downloadLink;
        public String thumbnail;
    }

}
