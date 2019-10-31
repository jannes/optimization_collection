import data.Arc;
import data.FlowRequirement;
import data.Graph;
import data.Instance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class InstanceReader {

    public static Instance parseInstance(Path instancePath) throws IOException {
        var lines = Files.lines(instancePath).iterator();
        var numNodes = Integer.parseInt(lines.next().split("\t")[1]);
        var numArcs = Integer.parseInt(lines.next().split("\t")[1]);
        var numCom = Integer.parseInt(lines.next().split("\t")[1]);
        lines.next();
        lines.next();
        var arcs = new ArrayList<Arc>();
        for (int i = 0; i < numArcs; i++) {
            arcs.add(lineToArc(lines.next()));
        }
        lines.next();
        lines.next();
        var flowReqs = new ArrayList<FlowRequirement>();
        for (int i = 0; i < numCom; i++) {
            flowReqs.add(lineToFlowReq(lines.next()));
        }
        return new Instance(new Graph(arcs, numNodes), flowReqs);
    }

    private static FlowRequirement lineToFlowReq(String line) {
        var split = line.split("\t");
        return new FlowRequirement(Integer.parseInt(split[0]),
                Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    private static Arc lineToArc(String line) {
        var split = line.split("\t");
        return new Arc(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                Integer.parseInt(split[2]), Integer.parseInt(split[3]));
    }
}
