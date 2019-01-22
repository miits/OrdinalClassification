package put.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.Test;
import org.rulelearn.data.Attribute;
import org.rulelearn.data.json.AttributeDeserializer;
import org.rulelearn.data.json.ObjectBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JsonTest {
    @Test
    void loadDataTest()
    {
        Attribute[] attributes = null;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Attribute.class, new AttributeDeserializer());
        Gson gson = gsonBuilder.setPrettyPrinting().create();

        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new FileReader("data/json/balance_scale.json"));
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
        if (jsonReader != null) {
            attributes = gson.fromJson(jsonReader, Attribute[].class);
        }
        else {
            fail("Unable to load JSON test file with definition of attributes");
        }

        JsonElement json = null;
        try {
            jsonReader = new JsonReader(new FileReader("data/json/balance_scale.json"));
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex.toString());
        }
        if (jsonReader != null) {
            JsonParser jsonParser = new JsonParser();
            json = jsonParser.parse(jsonReader);
        }
        else {
            fail("Unable to load JSON test file with definition of objects");
        }

        ObjectBuilder ob = new ObjectBuilder(attributes);
        List<String []> objects = null;
        objects = ob.getObjects(json);

        assertTrue(objects != null);
        assertEquals(objects.size(), 5);
    }
}
