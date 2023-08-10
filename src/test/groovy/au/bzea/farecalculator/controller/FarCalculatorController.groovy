package au.bzea.farecalculator.controller

import spock.lang.Specification
import spock.lang.Subject
import java.util.logging.Logger

class FareCalculatorControllerTest extends Specification {

    @Subject
    def controller = new FareCalculatorController()
    Logger logger = Logger.getLogger("")

    def data = "ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN\r\n" +
"1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559\r\n" +
"2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559\r\n" +
"3, 22-01-2023 09:20:00, ON, Stop3, Company1, Bus36, 4111111111111111\r\n" +
"4, 23-01-2023 08:00:00, ON, Stop1, Company1, Bus37, 4111111111111111\r\n" +
"5, 23-01-2023 08:02:00, OFF, Stop1, Company1, Bus37, 4111111111111111\r\n" +
"6, 24-01-2023 16:30:00, OFF, Stop2, Company1, Bus37, 5500005555555559"

    def "FareCalculatorController fares calaluate"() { 
        given: "a csv file"
        def csvData = data

        when: "I calculate the fares"
        logger.info (csvData)
        def outputData = controller.calculateFares(csvData)

        then: "I am returned the expected data"
        logger.info(outputData)
        assert outputData.contains("22-01-2023 13:00:00,22-01-2023 13:05:00")
    }

}