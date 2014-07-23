package fr.icodem.db4labs.database;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DataContentHandler implements ContentHandler {

    private SqlExecutor sqlExecutor;
    private ValueConverter converter;

    private Path filesPath;

    private DbDescriptor descriptor;

    private TableDescriptor tableInProcess;
    private ColumnDescriptor columnInProcess;
    private PersistentObject dataInProcess;

    public DataContentHandler(Path filesPath, DbDescriptor descriptor, SqlExecutor sqlExecutor) {
        this.filesPath = filesPath;
        this.descriptor = descriptor;
        this.sqlExecutor = sqlExecutor;
        this.converter = new ValueConverter();
    }

    @Override
    public void startJSON() throws ParseException, IOException {
        dataInProcess = new PersistentObject();
    }

    @Override
    public void endJSON() throws ParseException, IOException {}

    @Override
    public boolean startObject() throws ParseException, IOException {
        if (tableInProcess != null) {
            dataInProcess.clearContent();
        }
        return true;
    }

    @Override
    public boolean endObject() throws ParseException, IOException {
        if (tableInProcess != null) {
            try {
                sqlExecutor.insert(dataInProcess);// TODO USE BATCH
                sqlExecutor.commit();
            } catch (Exception e) {
                try {
                    sqlExecutor.rollback();
                } catch (Exception ex) {}
                e.printStackTrace();
                throw new ParseException(ParseException.ERROR_UNEXPECTED_TOKEN,
                                        ": Could not insert data during parsing => table " +
                                        tableInProcess.getName() + " " + dataInProcess.getProperties() + dataInProcess.getTable());
            }
        }
        return true;
    }

    @Override
    public boolean startObjectEntry(String str) throws ParseException, IOException {
        if (tableInProcess == null) {// new table, str is  table name
            tableInProcess = descriptor.getTable(str);
            dataInProcess.setTable(str);
        }
        else {// parameters for insert processing
            columnInProcess = tableInProcess.getColumn(str);// s is column name
        }
        return true;
    }

    @Override
    public boolean endObjectEntry() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean startArray() throws ParseException, IOException {
        return true;
    }

    @Override
    public boolean endArray() throws ParseException, IOException {
        tableInProcess = null;// end of table lines, new table expected afterward
        return true;
    }

    @Override
    public boolean primitive(Object obj) throws ParseException, IOException {
        if (tableInProcess != null && columnInProcess != null) {
            Object value = converter.jsonToJava(obj, columnInProcess.getType());

            if (DataType.BLOB.equals(columnInProcess.getType()) && value != null) {
                value = getFileContent((long)value);
            }
            else if (DataType.VARCHAR.equals(columnInProcess.getType())
                    && value != null && columnInProcess.isStoreAsFile()) {
                long fileId = Long.parseLong((String)value);
                value = getTextFileContent(fileId);
            }

            dataInProcess.setProperty(columnInProcess.getName(), value);
        }
        return true;
    }

    private byte[] getFileContent(long fileId) throws IOException {
        byte[] content = null;
        Path filePath = filesPath.resolve("" + fileId);
        try (SeekableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.READ);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            ByteBuffer buffer = ByteBuffer.allocate(1096);
            while (channel.read(buffer) > 0) {
                buffer.flip();
                baos.write(buffer.array());
                buffer.clear();
            }
            content = baos.toByteArray();
        }
        return content;
    }

    private String getTextFileContent(long fileId) throws IOException {
        byte[] content = getFileContent(fileId);

        String str = new String(content, 0, content.length, Charset.forName("UTF-8"));
        return str;
    }

}
