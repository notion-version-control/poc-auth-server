package ai.usnack.notionversioncontrol;

import org.springframework.boot.SpringApplication;

public class TestNotionVersionControlApplication {

  public static void main(String[] args) {
    SpringApplication.from(NotionVersionControlApplication::main)
        .with(TestcontainersConfiguration.class).run(args);
  }

}
