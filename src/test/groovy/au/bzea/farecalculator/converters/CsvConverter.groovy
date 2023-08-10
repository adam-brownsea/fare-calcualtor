package au.bzea.farecalculator.converters

import au.bzea.farecalculator.model.TapOutputLine
import spock.lang.Specification
import spock.lang.Subject
import java.util.logging.Logger

class CsvConverterTest extends Specification {

    @Subject
    def converter = new CsvConverter()
    Logger logger = Logger.getLogger("")

    def data = "ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\r\n" +
"1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559\r\n" +
"2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559\r\n" +
"3, 22-01-2023 09:20:00, ON, Stop3, Company1, Bus36, 4111111111111111\r\n" +
"4, 23-01-2023 08:00:00, ON, Stop1, Company1, Bus37, 4111111111111111\r\n" +
"5, 23-01-2023 08:02:00, OFF, Stop1, Company1, Bus37, 4111111111111111\r\n" +
"6, 24-01-2023 16:30:00, OFF, Stop2, Company1, Bus37, 5500005555555559"

    def data2 = "Started, Finished, DurationSecs, FromStopId, ToStopId, ChargeAmount, CompanyId, BusID, PAN, Status\r\n" +
    "22-01-2018 13:00:00, 22-01-2018 13:05:00, 900, Stop1, Stop2, \$3.25, Company1,Bus37, 5500005555555559, COMPLETED"


    def "CsvConverterTest read from string"() { 
        given: "a csv file"
        def csvData = data

        when: "I read it"
        logger.info (csvData)
        def records = converter.readCsvFromString(csvData)

        then: "I am returned the expect number of records"
        logger.info("record size: " + records.size())
        assert records.size() == 6
    }

    def "CsvConverterTest convert to array"() { 
        given: "a csv file"
        def csvData = data

        when: "I read it"
        logger.info (csvData)
        def records = converter.readCsvFromString(csvData)

        and: "convert it to array"
        def arrayData = converter.convertToArray(records)

        then: "I am returned a arracy file with expected result"
        logger.info("arrayData size: " + arrayData.size())
        assert arrayData.size() == 6
    }

    def "CsvConverterTest convert in one method"() { 
        given: "a csv file"
        def csvData = data

        when: "I read it and convert it "
        logger.info (csvData)
        def arrayData = converter.convertCsvStringToArray(csvData)

        then: "I am returned a array file with expected result"
        logger.info("array size: " + arrayData.size())
        assert arrayData.size() == 6
        assert arrayData.get(0).ID == 1
        assert arrayData.get(1).DateTimeUTC == "22-01-2023 13:05:00"
        assert arrayData.get(2).TapType == "ON"
        assert arrayData.get(3).StopId == "Stop1"
        assert arrayData.get(4).CompanyId == "Company1"
        assert arrayData.get(5).BusID == "Bus37"
        assert arrayData.get(5).PAN == "5500005555555559"
    }

}