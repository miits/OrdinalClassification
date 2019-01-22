package put.main;

import org.junit.jupiter.api.Test;
import org.rulelearn.data.csv.ObjectBuilder;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {
    @Test
    void loadDataTest()
    {
        ObjectBuilder ob = new ObjectBuilder(false);
        List<String[]> objects = null;
        try {
            objects = ob.getObjects("data/csv/balance_scale.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assertTrue(objects != null);
        assertEquals(objects.size(), 625);
    }
}