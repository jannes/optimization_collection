import algorithms.FPTAS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import util.InstanceReader;
import util.ProblemInstance;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Min Tardiness Algorithm")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApproxTardinessTest {


    @BeforeAll
    void initAll() { }


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
        FPTAS a = new FPTAS(instance);
        assertNotNull(solution);
        assertNotEquals(0, instance.getNumJobs());
        assertEquals(solution.intValue(), a.getApproxMinTard(1), solution.intValue());
    }

}

