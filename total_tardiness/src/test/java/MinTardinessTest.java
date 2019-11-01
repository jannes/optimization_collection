import util.InstanceReader;
import util.ProblemInstance;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import algorithms.ExactSchedule;
import algorithms.ExactWSched;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Min Tardiness Algorithm")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MinTardinessTest {


    @BeforeAll
    void initAll() { }

    static Arguments lineToArguments(String line) {
        String[] filenameAndSolution = line.split("\\s+");
        String filename = filenameAndSolution[0];
        Integer solution = Integer.valueOf(filenameAndSolution[1]);
        return Arguments.of(filename, solution);
    }

    static Stream<Arguments> provideInstancesDescriptors() {
        Stream<Arguments> argumentsStream = null;
        File f = new File(MinTardinessTest.class.getResource("test-set-answers.txt").getFile());
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            argumentsStream = reader.lines().map(MinTardinessTest::lineToArguments).limit(100);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return argumentsStream;
    }

    @ParameterizedTest
    @MethodSource("provideInstancesDescriptors")
    void minTardTest(String filename, Integer solution) {
        String fullPathOfInstance = String.format("%s/%s",
                MinTardinessTest.class.getResource("instances").getPath(),
                filename);
        File instanceFile = new File(fullPathOfInstance);
        assertTrue(instanceFile.exists());
        ProblemInstance instance = InstanceReader.readInstance(fullPathOfInstance);
        ExactSchedule a = new ExactSchedule(instance);
        assertNotNull(solution);
        assertNotEquals(0, instance.getNumJobs());
        assertEquals(solution.intValue(), a.getMinimumTardiness(0));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("provideInstancesDescriptors")
    void optSchedTest(String filename, Integer solution) {
        String fullPathOfInstance = String.format("%s/%s",
                MinTardinessTest.class.getResource("instances").getPath(),
                filename);
        File instanceFile = new File(fullPathOfInstance);
        assertTrue(instanceFile.exists());
        ProblemInstance instance = InstanceReader.readInstance(fullPathOfInstance);
        ExactWSched a = new ExactWSched(instance);
        assertNotNull(solution);
        assertNotEquals(0, instance.getNumJobs());
        StringBuilder stringBuilder = new StringBuilder();
        int[][] schedule = a.getOptimalSchedule(0);
        for (int i = 0; i < schedule.length; i++) {
            if (schedule[i] == null)
                stringBuilder.append("null, ");
            else
                stringBuilder.append(String.format("%d, ",schedule[i][2]));
        }
        assertEquals("", stringBuilder.toString());
    }

}
