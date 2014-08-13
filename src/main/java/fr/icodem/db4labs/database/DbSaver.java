package fr.icodem.db4labs.database;

import com.google.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DbSaver {

    @Inject
    private DbDescriptorProvider dbDescriptorProvider;

    @Inject private ConnectionProvider connectionProvider;

    @Inject
    private ValueConverter converter;

    public void save(Path path) throws Exception {

        DbDescriptor descriptor = dbDescriptorProvider.getDescriptor();
        Connection connection = connectionProvider.getConnection();
        SqlExecutor sqlExecutor = new SqlExecutor(descriptor, connection);

        // get tables to export
        List<TableDescriptor> tables = new ArrayList<>();
        for (TableDescriptor td : descriptor.getTables()) {
            if (sqlExecutor.count(td.getName()) > 0) {
                tables.add(td);
            }
        }

        // prepare archive file system
        try (FileSystem archive = createArchiveFileSystem(path);) {
            // files directories
            Path filesPath = archive.getPath("files");
            Files.createDirectory(filesPath);

            // json file
            Path pathInZipfile = archive.getPath("/data.json");

            try (SeekableByteChannel channel = Files.newByteChannel(pathInZipfile,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);) {
                writeJsonData(archive, channel, filesPath, tables, sqlExecutor);
            }

        }

    }

    private void writeJsonData(FileSystem archive, SeekableByteChannel jsonChannel,
                               Path filesPath, List<TableDescriptor> tables,
                               SqlExecutor sqlExecutor) throws Exception {

        int fileId = 0;

        ByteBuffer buffer = ByteBuffer.allocate(4096);
        buffer.put("{".getBytes());

        boolean firstTable = true;
        for (TableDescriptor table : tables) {
            if (!firstTable) {
                buffer.put(",".getBytes());
            } else firstTable = false;
            buffer.put("\r\n\t\"".getBytes())
                    .put(table.getName().getBytes())
                    .put("\":[\r\n".getBytes());

            flushBuffer(buffer, jsonChannel);
            List<PersistentObject> records = sqlExecutor.select(table.getName());// todo perf: replace with iterator ?
            boolean firstRecord = true;
            for (PersistentObject record : records) {
                if (!firstRecord) {
                    buffer.put(",\r\n".getBytes());
                }

                //json.clear();
                buffer.put("\t\t{".getBytes());
                boolean firstColumn = true;
                for (ColumnDescriptor column : table.getColumns()) {
                    boolean isBlob = DataType.BLOB.equals(column.getType());

                    if (!firstColumn) {
                        buffer.put(",".getBytes());
                    }

                    // property name
                    buffer.put("\"".getBytes())
                            .put(column.getName().getBytes())
                            .put("\":".getBytes());

                    // property value
                    Object value = record.getProperty(column.getName());
                    value = converter.javaToJson(value, column.getType());
                    if (value instanceof String) {
                        buffer.put("\"".getBytes());
                    }
                    if (value == null) buffer.put("null".getBytes());
                    else {
                        if (!isBlob && !column.isStoreAsFile()) {
                            if (value instanceof String) {
                                value = ((String) value).replaceAll("\"", "\\\\\"");// escape double quote for json format
                            }

                            byte[] bytes = value.toString().getBytes(Charset.forName("UTF-8"));
                            buffer.put(bytes);
                        }
                        else if (isBlob) {
                            byte[] blobData = (byte[])value;
                            String fileIdString = "" + ++fileId;
                            buffer.put(fileIdString.getBytes());
                            Path blobPath = filesPath.resolve(fileIdString);
                            addFileToArchive(archive, blobData, blobPath);
                        }
                        else if (column.isStoreAsFile()) {
                            byte[] bytes = value.toString().getBytes(Charset.forName("UTF-8"));
                            String fileIdString = "" + ++fileId;
                            buffer.put(fileIdString.getBytes());
                            Path blobPath = filesPath.resolve(fileIdString);
                            addFileToArchive(archive, bytes, blobPath);
                        }
                    }
                    if (value instanceof String) {
                        buffer.put("\"".getBytes());
                    }

                    if (DataType.BLOB.equals(column.getType())) {
                        flushBuffer(buffer, jsonChannel);
                    }

                    if (firstColumn) firstColumn = false;
                }
                buffer.put("}".getBytes());
                flushBuffer(buffer, jsonChannel);
                firstRecord = false;
            }
            buffer.put("\r\n\t]".getBytes());
            flushBuffer(buffer, jsonChannel);
        }

        buffer.put("\r\n}".getBytes());
        flushBuffer(buffer, jsonChannel);
    }

    private void flushBuffer(ByteBuffer buffer, SeekableByteChannel channel) throws IOException {
        buffer.flip();
        while(buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
    }

    private FileSystem createArchiveFileSystem(Path path) throws IOException {
        // delete old archive if exists
        if (Files.exists(path)) Files.delete(path);

        // convert the filename to a URI
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());
        return FileSystems.newFileSystem(uri, new HashMap<String, String>() {
            {
                put("create", "true");
            }
        });
    }

    private void addFileToArchive(FileSystem archive, byte[] data, Path path) throws Exception {
        try (SeekableByteChannel channel = Files.newByteChannel(path,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);) {
            ByteBuffer buffer = ByteBuffer.allocate(data.length);//TODO check memory
            buffer.put(data);
            flushBuffer(buffer, channel);
        }
    }

}
