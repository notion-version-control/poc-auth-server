package ai.usnack.notionversioncontrol;

import java.nio.file.Path;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer postgresContainer() {
    String imageName = new ImageFromDockerfile("nvc-postgres-test", false)
        .withDockerfile(Path.of("docker/postgres/Dockerfile"))
        .get();
    return new PostgreSQLContainer(DockerImageName.parse(imageName).asCompatibleSubstituteFor("postgres"))
        .withCommand(
            "postgres",
            "-c", "shared_preload_libraries=pg_cron",
            "-c", "cron.database_name=test"
        );
  }

}
