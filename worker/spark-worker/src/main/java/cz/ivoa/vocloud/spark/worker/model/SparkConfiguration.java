package cz.ivoa.vocloud.spark.worker.model;

import cz.ivoa.vocloud.spark.schema.Environment;
import cz.ivoa.vocloud.spark.schema.ParamsList;
import cz.ivoa.vocloud.spark.schema.Worker;
import cz.ivoa.vocloud.spark.worker.Config;
import org.w3c.dom.Element;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by radiokoza on 21.4.17.
 */
public class SparkConfiguration implements Serializable {
    private static final Logger LOG = Logger.getLogger(SparkConfiguration.class.getName());

    private final Map<String, String> environment;
    private final Map<String, String> genericParameters;
    private final Map<String, String> confParameters;

    public SparkConfiguration(Worker worker, String config) {
        //initialize arrays
        this.environment = new HashMap<>();
        this.genericParameters = new HashMap<>();
        this.confParameters = new HashMap<>();
        //parse environment from the configuration
        importEnvironment(Config.settings.getEnvironment());
        //import implicit parameters from the root element
        importParameters(Config.settings.getSubmitParams());
        importParameters(worker.getSubmitParams());
        //parse configuration from config if there is any
        importJsonParameters(config);
    }

    private String getStringValue(JsonValue value) {
        if (value.getValueType().equals(JsonValue.ValueType.STRING)) {
            return ((JsonString) value).getString();
        } else {
            return value.toString();
        }
    }

    private void importJsonParameters(String config) {
        try {
            JsonObject json;
            try (JsonReader jsonReader = Json.createReader(new StringReader(config))) {
                json = jsonReader.readObject();
            }
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
                        String value = getStringValue(c.getValue());
                        confParameters.put(name, value);
                    }
                } else {
                    //generic tag
                    String name = i.getKey();
                    String value = getStringValue(i.getValue());
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
        if (paramsList == null) {
            return;
        }
        for (Element elem : paramsList.getAny()) {
            //detect conf element
            if (elem.getTagName().equals("conf")) {
                String name = elem.getAttribute("name");
                String value = elem.getTextContent().trim();
                confParameters.put(name, value);
            } else {
                //any other element
                String name = elem.getTagName();
                String value = elem.getTextContent().trim();
                genericParameters.put(name, value);
            }
        }
    }

    private void importEnvironment(Environment environment) {
        for (Element elem : environment.getAny()) {
            String name = elem.getTagName();
            String value = elem.getTextContent().trim();
            this.environment.put(name, value);
        }

    }

    public List<String> commandsList() {
        List<String> commands = new ArrayList<>();
        //add generic parameters
        for (Map.Entry<String, String> i : genericParameters.entrySet()) {
            commands.add("--" + i.getKey());
            commands.add(i.getValue());
        }
        //add conf parameters
        for (Map.Entry<String, String> i : confParameters.entrySet()) {
            commands.add("--conf");
            commands.add(String.format("%s=%s", i.getKey(), i.getValue()));
        }
        return commands;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }
}
