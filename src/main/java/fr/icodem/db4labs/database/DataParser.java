package fr.icodem.db4labs.database;

import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class DataParser {

    private SqlExecutor sqlExecutor;

    public DataParser(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    public void parse(Path dataPath, DbDescriptor descriptor) throws Exception {
        try (FileSystem archive = getArchiveFileSystem(dataPath);) {
            dataPath = archive.getPath("/data.json");
            try (InputStream is = Files.newInputStream(dataPath);) {
                InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
                new JSONParser().parse(reader, new DataContentHandler(archive.getPath("/files"), descriptor, sqlExecutor));
            }
        }

    }

    private FileSystem getArchiveFileSystem(Path path) throws IOException {
        // convert the filename to a URI
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());
        return FileSystems.newFileSystem(uri, new HashMap<String, String>() {
            {
                put("create", "false");
            }
        });
    }


}
