
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.core.io.Resource;
import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.stream.Stream;
@Component
@Log4j2
/**
 * eSargin
 */
public class InitializeData {
    @Autowired
    private DataSource dataSource;

    /**
     * FIXME https://stackoverflow.com/a/49257939
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        boolean boolx = true;
        if (boolx) {
            AtomicBoolean bool = new AtomicBoolean(false);
            ArrayList<String> fileName = new ArrayList<>();
            try {

                try (Stream<Path> paths = Files.walk(Paths.get(new FileSystemResource("").getFile().getAbsolutePath() + "\\src\\main\\resources\\data"))) {
                    paths
                            .filter(Files::isRegularFile)
                            .forEach(elem -> {
                                fileName.add(elem.getFileName().toString());
                                bool.set(true);
                            });
                }
            } catch (Exception e) {
                log.info("Yüklenecek Sql Scripti Bulunamadı.");
                bool.set(false);
            }

            if (bool.get()) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        ArrayList<Resource> listClassPath = new ArrayList<>();
                        Resource[] resources = new Resource[fileName.size()];
                        for (int i = 0; i < fileName.size(); i++) {
                            log.info("Script Hazırlanıyor : " + fileName.get(i));
                            resources[i] = new ClassPathResource("data/" + fileName.get(i));
                        }
                        ResourceDatabasePopulator resourceDatabasePopulator =
                                new ResourceDatabasePopulator(false, false, "UTF-8", resources);
                        log.info("Hazırlanan Scriptler Çalıştırılıyor.");
                        try {
                            resourceDatabasePopulator.execute(dataSource);
                            log.info("Scriptler Tamamlandı.");
                        } catch (Exception e) {
                            log.info("Tablolar boş olmadığı için scriptler çalıştırılamadı.");
                        }

                    }
                });
                t.start();

            }

        }

    }
}
