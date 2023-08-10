package au.bzea.farecalculator.util;

import au.bzea.farecalculator.model.TapInputLine;
import au.bzea.farecalculator.model.TapOutputLine;
import au.bzea.farecalculator.constants.Constants;

import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;


public class GenerateOutputLines {
    private static Logger logger = Logger.getLogger(GenerateOutputLines.class.getName());
    
    public static ArrayList<TapOutputLine> createOutputLines(ArrayList<TapInputLine> inputLines) {
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
                outputLine.Status = Constants.INCOMPLETE;

                outputLines.add(outputLine);
                
                inputLine.Status = Constants.PROCESSED;
            } else if (inputLine.TapType.equals("OFF")) {
                logger.info("is OFF");
                // For tap off update previous tap on entry
                for (TapOutputLine outputLine : outputLines) {
                    if (outputLine.PAN.equals(inputLine.PAN) && 
                        outputLine.BusID.equals(inputLine.BusID) && 
                        outputLine.Status.equals(Constants.INCOMPLETE)) {
                        outputLine.Finished = inputLine.DateTimeUTC;
                        outputLine.ToStopId = inputLine.StopId;

                        if (outputLine.ToStopId.equals(outputLine.FromStopId)) {
                            outputLine.Status = Constants.CANCELLED;
                        } else {
                            outputLine.Status = Constants.COMPLETED;
                        }

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

                        inputLine.Status = Constants.PROCESSED;
                        break;
                    }
                }

                if (!inputLine.Status.equals(Constants.PROCESSED)){
                    logger.info("not PROC");

                    // If tap on for tap off NOT FOUND, write new tap off CANCELLED
                    TapOutputLine outputLine = new TapOutputLine();
                    outputLine.Started = outputLine.Finished = inputLine.DateTimeUTC;
                    outputLine.FromStopId = outputLine.ToStopId = inputLine.StopId;
                    outputLine.DurationSecs = 0;
                    outputLine.CompanyId = inputLine.CompanyId;
                    outputLine.BusID = inputLine.BusID;
                    outputLine.PAN = inputLine.PAN;
                    outputLine.Status = Constants.CANCELLED;

                    inputLine.Status = Constants.PROCESSED;                    
                }
            }

        });
        return outputLines;
        
    }
}
