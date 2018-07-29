package tmm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class MovieParser {

    public static Movie parse(JSONObject json) {
        Movie movie = new Movie();
        parseIds(movie, json);
        movie.title = getStringSafely(json, "title");
        movie.description = getStringSafely(json, "plot");
        movie.year = getStringSafely(json, "year");
        movie.tags = parseTags(json);
        parseMediaFile(movie, json);
        return movie;
    }

    private static void parseMediaFile(Movie movie, JSONObject json) {
        JSONArray mediaFiles = json.getJSONArray("mediaFiles");
        for (int i = 0; i < mediaFiles.length(); i++) {
            JSONObject mediaFile = mediaFiles.getJSONObject(i);
            if ("VIDEO".equalsIgnoreCase(mediaFile.getString("type"))) {
                movie.widthInPixel = getIntSafely(mediaFile, "videoWidth");
                movie.heightInPixel = getIntSafely(mediaFile, "videoHeight");
                movie.lengthInSecs = getIntSafely(mediaFile, "durationInSecs");

                movie.audioStreams = new ArrayList<String>();
                JSONArray audioStreams = mediaFile.getJSONArray("audioStreams");
                for (int j = 0; j < audioStreams.length(); j++) {
                    JSONObject audioStream = audioStreams.getJSONObject(j);
                    String streamInfo = getStringSafely(audioStream, "codec") + " " + getStringSafely(audioStream, "channels");
                    movie.audioStreams.add(streamInfo);
                }
            } else if ("POSTER".equalsIgnoreCase(mediaFile.getString("type"))) {
                movie.pathToPoster = getStringSafely(mediaFile, "path") + File.separator + getStringSafely(mediaFile, "filename");
            }
        }
    }

    private static List<String> parseTags(JSONObject json) {
        List<String> tags = new ArrayList<String>();
        if (json.has("tags")) {
            JSONArray tagArray = json.getJSONArray("tags");
            for (int i = 0; i < tagArray.length(); i++) {
                tags.add(tagArray.getString(i));
            }
        }
        return tags;
    }

    private static String getStringSafely(JSONObject json, String key) {
        if (json.has(key)) {
            return json.getString(key);
        } else {
            return "";
        }
    }

    private static int getIntSafely(JSONObject json, String key) {
        if (json.has(key)) {
            return json.getInt(key);
        } else {
            return 0;
        }
    }

    private static void parseIds(Movie movie, JSONObject json) {
        if (json.has("ids")) {
            JSONObject ids = json.getJSONObject("ids");
            if (ids.has("imdb")) {
                movie.imdbId = ids.getString("imdb");
            }
            if (ids.has("tmdb")) {
                movie.tmdbId = ids.getInt("tmdb");
            }
        }
    }
}