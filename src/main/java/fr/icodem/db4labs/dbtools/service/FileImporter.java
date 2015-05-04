package fr.icodem.db4labs.dbtools.service;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import fr.icodem.db4labs.event.DataImportDoneEvent;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class FileImporter {
    @Inject private EventBus eventBus;

    public void importData(File file, DataConsumer<InputStream> consumer) throws Exception {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                importDataInternal(file, consumer);
                return null;
            }
        };

        // notify end of processing
        task.setOnSucceeded(event -> {
            eventBus.post(new DataImportDoneEvent(WorkerStateEvent.WORKER_STATE_SUCCEEDED));

        });
        task.setOnFailed(event -> {
            eventBus.post(new DataImportDoneEvent(WorkerStateEvent.WORKER_STATE_FAILED));

        });
        task.setOnCancelled(event -> {
            eventBus.post(new DataImportDoneEvent(WorkerStateEvent.WORKER_STATE_CANCELLED));

        });

        // start task
        new Thread(task).start();

    }

    private void importDataInternal(File file, DataConsumer<InputStream> consumer) throws Exception {
        if (file.isDirectory()) {
            Stream.of(file.listFiles())
                    .filter(f -> f.getName().endsWith(".xml"))
                    .forEach(f -> {
                        try (InputStream is = new FileInputStream(f)) {
                            consumer.accept(is);
                        } catch (Exception e) {
                            String source = f.getAbsolutePath();
                            String dest = source + ".rejected";
                            try {
                                Files.copy(Paths.get(source), Paths.get(dest));
                            } catch (IOException ioe) {}
                        }

                        backupImportedFile(f);
                    });
        }
        else if (file.isFile()) {
            try (InputStream is = new FileInputStream(file)) {
                consumer.accept(is);
            } catch (Exception e) {
                e.printStackTrace();
                String source = file.getAbsolutePath();
                String dest = source + ".rejected";
                Files.copy(Paths.get(source), Paths.get(dest));
            }

            backupImportedFile(file);
        }
    }

    private void backupImportedFile(File file) {
        if (file == null || !file.exists()) return;

        // move processed file
        try {
            Path backupDir = Paths.get(file.getAbsolutePath())
                    .getParent().resolve("backup");
            if (!Files.exists(backupDir)) {
                Files.createDirectory(backupDir);
            }
            Files.move(Paths.get(file.getAbsolutePath()), backupDir.resolve(file.getName()),
                    StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
