package cz.ivoa.vocloud.spark.worker.model;

import javax.json.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by radiokoza on 21.4.17.
 */
public class ParsedJsonConfig {
    private static final Logger LOG = Logger.getLogger(ParsedJsonConfig.class.getName());

    private final List<CopyOutputPath> copyOutputPaths;
    private final List<DownloadFileRule> downloadRules;
    private String jobConfig;

    public ParsedJsonConfig(String json) {
        this.copyOutputPaths = new ArrayList<>();
        this.downloadRules = new ArrayList<>();
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(json));
            JsonObject root = jsonReader.readObject();
            jsonReader.close();
            parseJsonConfig(root);
        } catch (JsonException ex) {
            jobConfig = json;
            LOG.log(Level.WARNING, "Error during JSON parsing. Whole config is considered as file input", ex);
        } catch (ClassCastException ex) {
            jobConfig = json;
            LOG.log(Level.WARNING, "Invalid form of the JSON input. Whole config is considered as file input", ex);
        }
    }

    private static String jsonToStr(JsonObject obj) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer);
        jsonWriter.writeObject(obj);
        jsonWriter.close();
        return writer.toString();
    }

    private void parseJsonConfig(JsonObject root) {
        if (!root.containsKey("job_config")) {
            //consider whole config as a job input
            jobConfig = jsonToStr(root);
            return;//nothing else to do here
        }
        jobConfig = jsonToStr(root.getJsonObject("job_config"));
        parseDownloadRules(root);
        parseCopyOutputPaths(root);
    }

    private void parseDownloadRules(JsonObject root) {
        if (!root.containsKey("download_files")) {
            return;//nothing to do
        }
        JsonArray arr = root.getJsonArray("download_files");
        for (JsonValue i : arr) {
            JsonObject rule = (JsonObject) i;
            List<String> urls = new ArrayList<>();
            for (JsonValue u : rule.getJsonArray("urls")) {
                urls.add(((JsonString) u).getString());
            }
            String folder = rule.getString("folder");
            downloadRules.add(new DownloadFileRule(urls, folder));
        }
    }

    private void parseCopyOutputPaths(JsonObject root) {
        if (!root.containsKey("copy_output")) {
            return;//nothing to do
        }
        JsonArray arr = root.getJsonArray("copy_output");
        for (JsonValue i : arr) {
            JsonObject rule = (JsonObject) i;
            String path = rule.getString("path");
            boolean mergeParts = rule.getBoolean("merge_parts", false);
            copyOutputPaths.add(new CopyOutputPath(path, mergeParts));
        }
    }

}
