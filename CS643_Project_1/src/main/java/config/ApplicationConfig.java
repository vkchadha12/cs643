package config;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.regions.Region;

public class ApplicationConfig {
    public CS643_Config get(String configFile) {

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(configFile));
            CS643_Config config = new CS643_Config();
            JSONObject jsonObject =  (JSONObject) obj;

            config.AWS_QUEUE_NAME = (String) jsonObject.get("AWS_QUEUE_NAME");
            System.out.println(config.AWS_QUEUE_NAME);

            config.AWS_S3_BUCKET = (String) jsonObject.get("AWS_S3_BUCKET");
            System.out.println(config.AWS_S3_BUCKET);

            //config.AWS_DEFAULT_REGION = (Region) jsonObject.get("AWS_DEFAULT_REGION");
            //System.out.println(config.AWS_DEFAULT_REGION);

            config.OUTPUT_FILE = (String) jsonObject.get("OUTPUT_FILE");
            System.out.println(config.OUTPUT_FILE);

            config.THRESHOLD = (Double) jsonObject.get("THRESHOLD");
            System.out.println(config.THRESHOLD);

            return config;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}