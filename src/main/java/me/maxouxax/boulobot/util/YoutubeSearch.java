package me.maxouxax.boulobot.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import me.maxouxax.boulobot.BOT;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class YoutubeSearch {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final YouTube youTube;

    public YoutubeSearch() throws GeneralSecurityException, IOException {
        this.youTube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, httpRequest -> {})
                .setApplicationName(BOT.getInstance().getConfigurationManager().getStringValue("googleApplicationName"))
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(BOT.getInstance().getConfigurationManager().getStringValue("googleApiYoutubeToken")))
                .build();
    }

    public List<SearchResult> search(String query, long maxResults)
            throws IOException {
        YouTube.Search.List request = youTube.search()
                .list(Collections.singletonList("id,title"));
        SearchListResponse response = request
                .setMaxResults(maxResults)
                .setQ(query)
                .setType(Collections.singletonList("video"))
                .execute();
        return response.getItems();
    }
}