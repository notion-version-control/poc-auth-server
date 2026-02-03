package ai.usnack.notionversioncontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotionVersionControlApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotionVersionControlApplication.class, args);
  }

}
