package au.bzea.farecalculator.controller;

import au.bzea.farecalculator.converters.CsvConverter;
import au.bzea.farecalculator.model.TapInputLine;
import au.bzea.farecalculator.model.TapOutputLine;
import au.bzea.farecalculator.constants.Constants;
import au.bzea.farecalculator.util.GenerateOutputLines;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.io.IOException;

@RestController
public class FareCalculatorController {

    private static Logger logger = Logger.getLogger(FareCalculatorController.class.getName());

    // Calculate the fares from a csv list of tap on and tap off records.
	@GetMapping("/fares")
	public static String calculateFares(String inputTaps) throws IOException {

        // convert the csv string data to array of input lines
        ArrayList<TapInputLine> inputLines = CsvConverter.convertCsvStringToArray(inputTaps);
       
        // Order by PAN and DateTime
        Comparator<TapInputLine> multiplePropComparator = Comparator
                    .comparing(TapInputLine::getPAN)
                    .thenComparing(TapInputLine::getDateTimeUTC);
        Collections.sort(inputLines, multiplePropComparator);

        
        // Loop thru sorted input - Write to output class (json)
        ArrayList<TapOutputLine> outputLines = GenerateOutputLines.createOutputLines(inputLines);

		// Hashmap of fare types
        Map<String, Float> zoneCharges = new HashMap<>();
        zoneCharges.put(Constants.STOP1 + Constants.STOP2, 3.25f);
        zoneCharges.put(Constants.STOP2 + Constants.STOP1, 3.25f);
        zoneCharges.put(Constants.STOP2 + Constants.STOP3, 5.5f);
        zoneCharges.put(Constants.STOP3 + Constants.STOP2, 5.5f);
        zoneCharges.put(Constants.STOP1 + Constants.STOP3, 7.3f);
        zoneCharges.put(Constants.STOP3 + Constants.STOP1, 7.3f);

        outputLines.forEach((outputLine) -> {
            // Looping thru the output file determine the far depending on the tap and tap off values
            if (outputLine.Status == Constants.COMPLETED) {
                String stopPair = outputLine.FromStopId + outputLine.ToStopId;
                if (zoneCharges.containsKey(stopPair)) {
                    outputLine.ChargeAmount = zoneCharges.get(stopPair);
                }
            } else if (outputLine.Status == Constants.INCOMPLETE) {
                if (outputLine.FromStopId.equals(Constants.STOP1) || 
                outputLine.FromStopId.equals(Constants.STOP3)) {
                    outputLine.ChargeAmount = zoneCharges.get(Constants.STOP1 + Constants.STOP3);
                } else if (outputLine.FromStopId.equals(Constants.STOP2)) {
                    outputLine.ChargeAmount = zoneCharges.get(Constants.STOP2 + Constants.STOP3);
                }
            }

        });
		
        logger.info(outputLines.get(0).Started);
        logger.info(outputLines.get(0).Finished);
        logger.info(outputLines.get(0).FromStopId);
        logger.info(outputLines.get(0).ToStopId);
        logger.info(String.valueOf(outputLines.get(0).ChargeAmount));
        logger.info(String.valueOf(outputLines.get(0).PAN));
        logger.info(outputLines.get(0).Status);        
        // Convert ouput array to CSV
        return  CsvConverter.convertArrayToCsvString(outputLines);
	}
}