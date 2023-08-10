package au.bzea.farecalculator.controller;

import au.bzea.farecalculator.converters.CsvConverter;
import au.bzea.farecalculator.model.TapInputLine;
import au.bzea.farecalculator.model.TapOutputLine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
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

    private static final String COMPLETED = "COMPLETED";
    private static final String INCOMPLETE = "INCOMPLETE";
    private static final String CANCELLED = "CANCELLED";
    private static final String PROCESSED = "PROCESSED";

    private static final String STOP1 = "Stop1";
    private static final String STOP2 = "Stop2";
    private static final String STOP3 = "Stop3";

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
        ArrayList<TapOutputLine> outputLines = new ArrayList<TapOutputLine>();



        // function to find output line value
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        inputLines.forEach((inputLine) -> {
            logger.info(inputLine.ID + ";");
            logger.info(inputLine.DateTimeUTC);
            logger.info(inputLine.TapType);
            logger.info(inputLine.StopId);
            logger.info(inputLine.CompanyId);        
            logger.info(inputLine.BusID);        
            logger.info(String.valueOf(inputLine.PAN));
    
            if (inputLine.TapType.equals("ON")) {
                logger.info("is ON");

                // For tap on write new output entry 
                TapOutputLine outputLine = new TapOutputLine();
                outputLine.Started = inputLine.DateTimeUTC;
                outputLine.FromStopId = inputLine.StopId;
                outputLine.CompanyId = inputLine.CompanyId;
                outputLine.BusID = inputLine.BusID;
                outputLine.PAN = inputLine.PAN;
                outputLine.Status = INCOMPLETE;

                outputLines.add(outputLine);
                
                inputLine.Status = PROCESSED;
            } else if (inputLine.TapType.equals("OFF")) {
                logger.info("is OFF");
                // For tap off update previous tap on entry
                for (TapOutputLine outputLine : outputLines) {
                    if (outputLine.PAN.equals(inputLine.PAN) && 
                        outputLine.BusID.equals(inputLine.BusID) && 
                        outputLine.Status.equals(INCOMPLETE)) {
                        outputLine.Finished = inputLine.DateTimeUTC;
                        outputLine.ToStopId = inputLine.StopId;
                        outputLine.Status = COMPLETED;

                        // set duration seconds
                        try {
                            Date startTime = dateFormat.parse(outputLine.Started);
                            Date finishTime = dateFormat.parse(outputLine.Finished);

                            long diffMillis = finishTime.getTime() - startTime.getTime();
                            long secs = diffMillis / 1000;               
                            outputLine.DurationSecs = (int)secs;               
                        } catch (ParseException e){
                            e.printStackTrace();
                        }

                        inputLine.Status = PROCESSED;
                        break;
                    }
                }

                if (!inputLine.Status.equals(PROCESSED)){
                    logger.info("not PROC");

                    // If tap on for tap off NOT FOUND, write new tap off CANCELLED
                    TapOutputLine outputLine = new TapOutputLine();
                    outputLine.Started = outputLine.Finished = inputLine.DateTimeUTC;
                    outputLine.FromStopId = outputLine.ToStopId = inputLine.StopId;
                    outputLine.DurationSecs = 0;
                    outputLine.CompanyId = inputLine.CompanyId;
                    outputLine.BusID = inputLine.BusID;
                    outputLine.PAN = inputLine.PAN;
                    outputLine.Status = CANCELLED;

                    inputLine.Status = PROCESSED;                    
                }
            }

        });


		// Hashmap of fare types
        Map<String, Float> zoneCharges = new HashMap<>();
        zoneCharges.put(STOP1 + STOP2, 3.25f);
        zoneCharges.put(STOP2 + STOP1, 3.25f);
        zoneCharges.put(STOP2 + STOP3, 5.5f);
        zoneCharges.put(STOP3 + STOP2, 5.5f);
        zoneCharges.put(STOP1 + STOP3, 7.3f);
        zoneCharges.put(STOP3 + STOP1, 7.3f);

        outputLines.forEach((outputLine) -> {
            // Looping thru the output file determine the far depending on the tap and tap off values
            if (outputLine.Status == COMPLETED) {
                String stopPair = outputLine.FromStopId + outputLine.ToStopId;
                if (zoneCharges.containsKey(stopPair)) {
                    outputLine.ChargeAmount = zoneCharges.get(stopPair);
                }
            } else if (outputLine.Status == INCOMPLETE) {
                if (outputLine.FromStopId.equals(STOP1) || outputLine.FromStopId.equals(STOP3)) {
                    outputLine.ChargeAmount = zoneCharges.get(STOP1 + STOP3);
                } else if (outputLine.FromStopId.equals(STOP2)) {
                    outputLine.ChargeAmount = zoneCharges.get(STOP2 + STOP3);
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