package cz.ivoa.vocloud.spark.worker.model;

import cz.ivoa.vocloud.spark.schema.ParamsList;
import cz.ivoa.vocloud.spark.schema.Worker;
import cz.ivoa.vocloud.spark.worker.Config;
import org.w3c.dom.Element;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by radiokoza on 21.4.17.
 */
public class SparkConfiguration {
    private static final Logger LOG = Logger.getLogger(SparkConfiguration.class.getName());

    private final Map<String, String> genericParameters;
    private final Map<String, String> confParameters;

    public SparkConfiguration(Worker worker, String config) {
        //initialize arrays
        this.genericParameters = new HashMap<>();
        this.confParameters = new HashMap<>();
        //import implicit parameters from the root element
        importParameters(Config.settings.getSubmitParams());
        importParameters(worker.getSubmitParams());
        //parse configuration from config if there is any
        importJsonParameters(config);
    }

    private void importJsonParameters(String config) {
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(config));
            JsonObject json = jsonReader.readObject();
            jsonReader.close();
            if (!json.containsKey("spark_params")) {
                //no configuration parameters in json
                return;
            }
            JsonObject params = json.getJsonObject("spark_params");
            for (Map.Entry<String, JsonValue> i : params.entrySet()) {
                //detect configuration tag
                if (i.getKey().equals("conf")) {
                    JsonObject confObject = (JsonObject) i.getValue();
                    for (Map.Entry<String, JsonValue> c : confObject.entrySet()) {
                        String name = c.getKey();
                        String value = ((JsonString) c.getValue()).getString();
                        confParameters.put(name, value);
                    }
                } else {
                    //generic tag
                    String name = i.getKey();
                    String value = ((JsonString) i.getValue()).getString();
                    genericParameters.put(name, value);
                }
            }
        } catch (JsonParsingException ex) {
            LOG.log(Level.INFO, "Configuration file is not JSON parsable. Skipping parameters loading", ex);
        } catch (JsonException ex) {
            LOG.log(Level.WARNING, "Exception thrown during JSON processing", ex);
        } catch (ClassCastException ex) {
            LOG.log(Level.WARNING, "Unexpected JSON format. Skipping parameters loading", ex);
        }
    }

    private void importParameters(ParamsList paramsList) {
        for (Element elem : paramsList.getAny()) {
            //detect conf element
            if (elem.getTagName().equals("conf")) {
                String name = elem.getAttribute("name");
                String value = elem.getTextContent();
                confParameters.put(name, value);
            } else {
                //any other element
                String name = elem.getTagName();
                String value = elem.getTextContent();
                genericParameters.put(name, value);
            }
        }
    }
}
