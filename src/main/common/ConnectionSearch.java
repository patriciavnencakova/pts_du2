package main.common;

import main.common.dataTypes.*;
import main.common.line.LineFactory;
import main.common.line.Lines;
import main.common.stop.StopFactory;
import main.common.stop.Stops;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class ConnectionSearch {

    private final Lines lines;
    private final Stops stops;

    public ConnectionSearch(Stops stops, Lines lines) {
        this.stops = stops;
        this.lines = lines;
    }

    public ConnectionData search(StopName from, StopName to, Time when) {

        if (!stops.setStartingStop(from, when))
            return new ConnectionData("Invalid starting stop!");

        List<LineName> lineNames = stops.getLines(from);
        Time time = lines.updateReachable(lineNames, from, when);
        Optional<Pair<Time, StopName>> pair = stops.earliestReachableStopAfter(time);

        while (true) {
            if (pair.isEmpty()) {
                lines.clean();
                stops.clean();
                return new ConnectionData("There is no possible connection!");
            }
            if (pair.get().y.equals(to)) break;

            lineNames = stops.getLines(pair.get().y);
            time = lines.updateReachable(lineNames, pair.get().y, pair.get().x);
            pair = stops.earliestReachableStopAfter(time);
        }
        Stack<Tuple<StopName, LineName, Time>> stack = new Stack<>();
        Pair<Time, LineName> toPair = stops.getReachableAt(to);
        stack.push(new Tuple<>(new StopName(to), toPair.y, toPair.x));
        StopName stopName = lines.updateCapacityAndGetPreviousStop(toPair.y, to, toPair.x);

        while (!stopName.equals(from)) {
            toPair = stops.getReachableAt(stopName);
            stack.push(new Tuple<>(new StopName(stopName), toPair.y, toPair.x));
            stopName = lines.updateCapacityAndGetPreviousStop(toPair.y, stopName, toPair.x);
        }

        stack.push(new Tuple<>(new StopName(stopName), new LineName(" "), when));
        lines.clean();
        stops.clean();
        return new ConnectionData("Fastest route found successfully!", stack);
    }
}
