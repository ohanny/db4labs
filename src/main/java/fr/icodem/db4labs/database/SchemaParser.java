package fr.icodem.db4labs.database;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import fr.icodem.db4labs.app.AppName;

import java.io.InputStream;
import java.io.InputStreamReader;

public class SchemaParser {
    private JSONParser parser = new JSONParser();

    public DbDescriptor parse(String schema) throws Exception {
        InputStream is = getClass().getResourceAsStream("/schemas/" + schema);
        InputStreamReader reader = new InputStreamReader(is);
        JSONObject jsonDatabase = (JSONObject) parser.parse(reader);

        DbDescriptor descriptor = null;

        // first process imports
        JSONArray imports = (JSONArray) jsonDatabase.get("imports");
        if (imports != null) {
            for (int i = 0; i < imports.size(); i++) {
                String importedSchema = (String) imports.get(i);
                descriptor = parse(importedSchema);
            }
        }

        // when no imports
        if (descriptor == null) {
            descriptor = new DbDescriptor();
        }

        // main properties
        String appName = (String)jsonDatabase.get("appName");
        descriptor.setAppName(AppName.valueOf(appName));
        descriptor.setDbName((String)jsonDatabase.get("dbName"));
        descriptor.setDescription((String)jsonDatabase.get("description"));

        // tables
        JSONArray tableDescriptorList = (JSONArray) jsonDatabase.get("tables");
        for (int i = 0; i < tableDescriptorList.size(); i++) {
            JSONObject jsonTable = (JSONObject) tableDescriptorList.get(i);
            TableDescriptor tableDescriptor = new TableDescriptor();
            tableDescriptor.setName((String)jsonTable.get("name"));
            descriptor.addTable(tableDescriptor);

            // columns
            JSONArray columnDescriptorList = (JSONArray)jsonTable.get("columns");
            for (int j = 0; j < columnDescriptorList.size(); j++) {
                JSONObject jsonColumn = (JSONObject) columnDescriptorList.get(j);

                ColumnDescriptor columnDescriptor = new ColumnDescriptor();
                columnDescriptor.setName((String)jsonColumn.get("name"));
                columnDescriptor.setType(DataType.getValue((String)jsonColumn.get("type")));
                if (jsonColumn.get("pk") != null) {
                    columnDescriptor.setPrimaryKey((Boolean)jsonColumn.get("pk"));
                }
                if (jsonColumn.get("nullable") != null) {
                    columnDescriptor.setNullable((Boolean)jsonColumn.get("nullable"));
                } else columnDescriptor.setNullable(true);
                if (jsonColumn.get("identity") != null) {
                    columnDescriptor.setIdentity((Boolean)jsonColumn.get("identity"));
                }
                if (jsonColumn.get("length") != null) {
                    columnDescriptor.setLength(((Long)jsonColumn.get("length")).shortValue());
                }
                if (jsonColumn.get("precision") != null) {
                    columnDescriptor.setPrecision(((Long)jsonColumn.get("precision")).shortValue());
                }
                if (jsonColumn.get("scale") != null) {
                    columnDescriptor.setScale(((Long)jsonColumn.get("scale")).shortValue());
                }
                if (jsonColumn.get("storeAsFile") != null) {
                    columnDescriptor.setStoreAsFile((Boolean)jsonColumn.get("storeAsFile"));
                } else columnDescriptor.setStoreAsFile(false);

                tableDescriptor.addColumn(columnDescriptor);
            }

            // constraints
            JSONArray constraintDescriptorList = (JSONArray) jsonTable.get("constraints");
            if (constraintDescriptorList != null) {
                for (int j = 0; j < constraintDescriptorList.size(); j++) {
                    JSONObject jsonConstraint = (JSONObject) constraintDescriptorList.get(j);

                    ConstraintDescriptor constraintDescriptor = new ConstraintDescriptor();
                    constraintDescriptor.setName((String)jsonConstraint.get("name"));
                    constraintDescriptor.setType((String) jsonConstraint.get("type"));
                    constraintDescriptor.setTableRef((String) jsonConstraint.get("tableRef"));
                    if (jsonConstraint.get("column") != null) {
                        constraintDescriptor.addColumn((String)jsonConstraint.get("column"));
                    }
                    if (jsonConstraint.get("columns") != null) {
                        JSONArray columns = (JSONArray)jsonConstraint.get("columns");
                        for (int k = 0; k < columns.size(); k++) {
                            constraintDescriptor.addColumn((String)columns.get(k));
                        }
                    }
                    if (jsonConstraint.get("columnRef") != null) {
                        constraintDescriptor.addColumnRef((String) jsonConstraint.get("columnRef"));
                    }
                    if (jsonConstraint.get("columnsRef") != null) {
                        JSONArray columns = (JSONArray)jsonConstraint.get("columnsRef");
                        for (int k = 0; k < columns.size(); k++) {
                            constraintDescriptor.addColumnRef((String)columns.get(k));
                        }
                    }

                    tableDescriptor.addConstraint(constraintDescriptor);
                }
            }
        }

        return descriptor;
    }

}
