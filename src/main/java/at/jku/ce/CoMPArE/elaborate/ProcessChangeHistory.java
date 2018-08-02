package at.jku.ce.CoMPArE.elaborate;

import java.util.LinkedList;

/**
 * Created by oppl on 15/12/2016.
 */
public class ProcessChangeHistory {

    private LinkedList<ProcessChangeTransaction> changes;

    public ProcessChangeHistory() {
        changes = new LinkedList<>();
    }

    public void add(ProcessChangeTransaction processChange) {
        changes.addFirst(processChange);
    }

    public void removeUntil(ProcessChangeTransaction processChange) {
        if (!changes.contains(processChange)) return;
        while (changes.getFirst() != processChange) changes.removeFirst();
        changes.removeFirst();
    }

    public LinkedList<ProcessChangeTransaction> getHistory() {
        return changes;
    }
}
