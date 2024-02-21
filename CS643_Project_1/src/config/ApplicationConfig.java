package config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ApplicationConfig {
    public CS643_Config get(String configFile) {

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(configFile));
            CS643_Config config = new CS643_Config();
            JSONObject jsonObject =  (JSONObject) obj;

            CS643_Config.setAwsQueueName((String) jsonObject.get("AWS_QUEUE_NAME"));
            System.out.println("App Config: " + CS643_Config.getAwsQueueName());

            CS643_Config.setAwsS3Bucket((String) jsonObject.get("AWS_S3_BUCKET"));
            System.out.println("App Config: " + CS643_Config.getAwsS3Bucket());

            //config.AWS_DEFAULT_REGION = (Region) jsonObject.get("AWS_DEFAULT_REGION");
            //System.out.println(config.AWS_DEFAULT_REGION);

            CS643_Config.setOutputFile((String) jsonObject.get("OUTPUT_FILE"));
            System.out.println("App Config: " + CS643_Config.getOutputFile());

            config.setTHRESHOLD((Double) jsonObject.get("THRESHOLD"));
            System.out.println("App Config: " + config.getTHRESHOLD());

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