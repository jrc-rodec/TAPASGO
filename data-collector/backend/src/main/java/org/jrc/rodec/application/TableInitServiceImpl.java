package org.jrc.rodec.application;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVReader;
import io.quarkus.runtime.StartupEvent;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jrc.rodec.application.interfaces.TablesInitService;
import org.jrc.rodec.domain.CoordinateTuple;
import org.jrc.rodec.domain.TableRow;
import org.jrc.rodec.domain.repositories.TableRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import io.quarkus.logging.Log;
import org.jrc.rodec.microservices.*;

import static org.apache.spark.sql.functions.*;


@ApplicationScoped
public class TableInitServiceImpl implements TablesInitService {

    @Inject
    EntityManager eM;

    @ConfigProperty(name = "tables.dir")
    String tablesDir;

    @Inject
    TableRepository tableRepository;

    @RestClient
    GenesisApiClient genesisApiClient;

    void onStart(@Observes StartupEvent ev) {
        //List<TableRow> tableRows = eM.createQuery(
        //        "FROM TableRow WHERE tableId = '71141-0002'", TableRow.class).getResultList();
        //TableRow[] arr = (TableRow[]) tableRows.toArray();
        writeTables();
        //initTables();
    }

    public static SparkSession getSparkSession() {
        return SparkSession.builder()
                .appName("SQA Builder")
                .master("local")
                .getOrCreate();
    }

    Map<String, Integer> annotatorIndex = new HashMap<>();
    //currently not needed, as position is always zero -> no connected questions
    //Map<String, Integer> positionIndex;

    private void writeTables(){
        List<TableRow> tableRows = eM.createQuery(
                "FROM TableRow WHERE isAnswered = true", TableRow.class).getResultList();
        Dataset<Row> tableRowsdf = getSparkSession().createDataFrame(tableRows, TableRow.class);
        //register annotator udf
        getSparkSession().udf().register("getAnnotator", udf((String id) -> annotatorIndex.get(id) == null ? annotatorIndex.put(id, 0)
                : annotatorIndex.put(id, annotatorIndex.get(id) + 1), DataTypes.StringType));
        getSparkSession().udf().register("getAnswerCoordinates", udf((Integer row, Integer column) ->
                List.of(new CoordinateTuple(row, column).toString()), DataTypes.createArrayType(
                        DataTypes.StringType, false
                        )
                )
        );
        getSparkSession().udf().register("getAnswerText", udf((Map<String, String> rowEntries, Integer answerIndex) ->
                                List.of(rowEntries.get(rowEntries.keySet().toArray()[answerIndex])), DataTypes.createArrayType(
                                DataTypes.StringType, false
                        )
                )
        );


        tableRowsdf
                .withColumn("annotator", callUDF("getAnnotator", col("id")))
                .withColumn("position", lit(0))
                .withColumnRenamed("tableId", "id")
                .withColumn("answer_coordinates", callUDF("getAnswerColumns", sequence(col("row"), col("answerColumn"))))
                .withColumn("answer_text", callUDF("getAnswerText", sequence(col("rowEntries"), col("answerColumn"))))
                .select(col("id"), col("annotator"), col("position"), col("question"), col("id").as("table_file"),
                        col("answer_coordinates"))
                .write().csv("tabletest.csv");

    }

    @Override
    public void initTables() {
        Log.info("\n" + new Timestamp(System.currentTimeMillis()) + " INFO Started table init process!");
        List<List<TableRow>> tableList = readTablesFromSource();
        int i = 1;
        int size = tableList.size();
        for(List<TableRow> table: tableList) {
            Log.info("\n" + new Timestamp(System.currentTimeMillis()) + " INFO Inserting row " + i + "/" + size);
            tableRepository.insertTable(table);
            ++i;
        }
    }


    private List<List<TableRow>> readTablesFromSource() {
        List<List<TableRow>> CSVTableList = new ArrayList<>();
        MappingIterator<TableMetaData> tableMetaDataIter = null;
        try {
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.builder()
                    .setColumnSeparator(';')
                    .setUseHeader(true)
                    .setStrictHeaders(true)
                    .setSkipFirstDataRow(true)
                    .addColumn("", CsvSchema.ColumnType.NUMBER)
                    .addColumn("code", CsvSchema.ColumnType.STRING)
                    .addColumn("content", CsvSchema.ColumnType.STRING)
                    .addColumn("filename", CsvSchema.ColumnType.STRING)
                    .build();
            tableMetaDataIter = mapper.reader(TableMetaData.class).with(schema).readValues(new File("data.csv"));
            List<TableMetaData> tableMetaDataList = tableMetaDataIter.readAll();
            for(TableMetaData tableMetaData : tableMetaDataList){
                Log.info("\n" + new Timestamp(System.currentTimeMillis()) + " INFO Parsing table " + tableMetaData.getTableId() + "...");
                FileReader fileReader = new FileReader(tableMetaData.getFilePath());
                CSVReader csvReader = new CSVReader(fileReader, ';');
                CSVTableList.add(parseCSVToTable(tableMetaData.getTableId(), csvReader.readAll(), tableMetaData.getContent()));
            }
        } catch (IOException e) {
            return CSVTableList;
        }


        return CSVTableList;
    }

    private List<TableRow> parseCSVToTable(String tableName, List<String[]> csv, String content) {
        List<TableRow> table = new ArrayList<>();
        List<String> headerNames = Arrays.stream(csv.get(0)).collect(Collectors.toList());
        csv.remove(csv.get(0));

        int i = 0;
        for (String[] row : csv){
            table.add(TableRow.create(row, headerNames, tableName, i, content));
            ++i;
        }

        return table;
    }
}
