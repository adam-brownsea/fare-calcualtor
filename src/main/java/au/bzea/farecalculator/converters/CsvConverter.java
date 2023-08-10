package au.bzea.farecalculator.converters;

import au.bzea.farecalculator.model.TapInputLine;
import au.bzea.farecalculator.model.TapOutputLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVPrinter;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import java.io.IOException;

public class CsvConverter {
    private static Logger logger = Logger.getLogger(CsvConverter.class.getName());
    
    private static List<CSVRecord> readCsvFromString(String csvData) throws IOException {
        CSVParser parser = new CSVParser(new StringReader(csvData), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        parser.close();;
        return records;
    }
    
    private static ArrayList<TapInputLine> convertToArray(List<CSVRecord> records) {
        ArrayList<TapInputLine> tapInputLines = new ArrayList<TapInputLine>();

        
        // Loop thru records adding to json list
        for (CSVRecord record : records) {
            TapInputLine inputLine = new TapInputLine();

            inputLine.ID = Integer.parseInt(record.get(0));
            inputLine.DateTimeUTC = record.get(1).trim();
            inputLine.TapType = record.get(2).trim();
            inputLine.StopId = record.get(3).trim();
            inputLine.CompanyId = record.get(4).trim();
            inputLine.BusID = record.get(5).trim();
            inputLine.PAN = record.get(6).trim();
            inputLine.Status = "";

            tapInputLines.add(inputLine);
        }

        return tapInputLines;
    }

    private static String writeStringFromCsv(List<CSVRecord> csvRecords) throws IOException {
        StringWriter stringWriter = new StringWriter();
        CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT);

        for (CSVRecord record : csvRecords) { csvPrinter.printRecord(record); }

        csvPrinter.flush();
        csvPrinter.close();

        return stringWriter.toString();
    }

    private static List<CSVRecord> convertToCSVRecords(List<TapOutputLine> outputLines) throws IOException {
        List<CSVRecord> csvRecords = new ArrayList<>();

        // Define format based on output headers
        String header = String.join(",", 
                "Started", "Finished", "DurationSecs", "FromStopId", "ToStopId", 
                "ChargeAmount", "CompanyId", "BusID", "PAN", "Status");
        CSVRecord csvHeader = CSVFormat.DEFAULT.parse(new StringReader(header)).getRecords().get(0);
        csvRecords.add(csvHeader);
                
        for (TapOutputLine outputLine : outputLines) {
            String csvString = String.join(",", 
                outputLine.Started,
                outputLine.Finished,
                String.valueOf(outputLine.DurationSecs),
                outputLine.FromStopId,
                outputLine.ToStopId,
                String.valueOf(outputLine.ChargeAmount),
                outputLine.CompanyId,
                outputLine.BusID,
                String.valueOf(outputLine.PAN),
                outputLine.Status
            );
            CSVRecord csvRecord = CSVFormat.DEFAULT.parse(new StringReader(csvString)).getRecords().get(0);
            csvRecords.add(csvRecord);
        }

        return csvRecords;
    }

    public static ArrayList<TapInputLine> convertCsvStringToArray(String csvData) throws IOException {
        try {
            List<CSVRecord> records = readCsvFromString(csvData);

            return convertToArray(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;                   
    }

    public static String convertArrayToCsvString(ArrayList<TapOutputLine>  outputLines) throws IOException {
        try {
            List<CSVRecord> records = convertToCSVRecords(outputLines);

            logger.info("Records: " + records.size());
            return writeStringFromCsv(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;                   
    }

}
