package put.test;

import org.junit.jupiter.api.Test;
import org.rulelearn.data.csv.ObjectBuilder;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoadDataTest {

    @Test
    void loadDataTest(){
        ObjectBuilder ob = new ObjectBuilder(true);
        List<String []> objects = null;
        try {
            objects = ob.getObjects("src/put/test/resources/data/csv/prioritisation1.csv");
        }
        catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
        catch (UnsupportedEncodingException ex) {
            System.out.println(ex);
        }
        assertTrue(objects != null);
        assertEquals(objects.size(), 579);
    }
}
