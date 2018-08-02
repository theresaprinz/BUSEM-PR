package at.jku.ce.CoMPArE;

import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.HashSet;
import java.util.Set;


public class BUSEMProcess {

    private static void addSubjectToProcess(Subject s, Process p) {
        p.addSubject(s);
    }

    private static void addMessageToProcess(Message m, Process p) {
        p.addMessage(m);
    }

    public static Process gettraditionalProcess() {
        Process tradProz = new Process("Traditioneller Prozess");

        Subject immoanb = new Subject("Immobilienanbieter");
        Subject wohngen = new Subject("Wohnungsgenossenschaft");
        Subject mieter = new Subject("Mieter");

        addSubjectToProcess(immoanb, tradProz);
        addSubjectToProcess(wohngen, tradProz);
        addSubjectToProcess(mieter, tradProz);

        Message kaufangebot = new Message("Kauf-Angebot");
        Message kaufannahme = new Message("Kauf-Annahme");
        Message kaufablehnung = new Message("Kauf-Ablehnung");
        Message angebot = new Message("Angebot");
        Message annahme = new Message("Annahme");
        Message ablehnung = new Message("Ablehnung");
        Message vertrag = new Message("Vertrag");

        addMessageToProcess(kaufangebot, tradProz);
        addMessageToProcess(kaufannahme, tradProz);
        addMessageToProcess(kaufablehnung, tradProz);
        addMessageToProcess(angebot, tradProz);
        addMessageToProcess(annahme, tradProz);
        addMessageToProcess(ablehnung, tradProz);
        addMessageToProcess(vertrag, tradProz);

        // Wohnungsgenossenschaft
        State state_wohngen = wohngen.setFirstState(new ActionState("Informationen zu Immobilie sammeln"));
        state_wohngen = state_wohngen.addNextState(new ActionState("Entscheidung treffen"));
        State temp = state_wohngen.addNextState(new ActionState("Angebot entwerfen"), new Condition("Immobilie kaufen"));
        state_wohngen.addNextState(new ActionState("Idee verwerfen", true), new Condition("Immobilie nicht kaufen"));
        state_wohngen = temp;
        state_wohngen = state_wohngen.addNextState(new SendState("Angebot senden", kaufangebot));

        state_wohngen = state_wohngen.addNextState(new RecvState("Auf Entscheidung warten", kaufannahme));
        ((RecvState) state_wohngen).addRecvdMessage(kaufablehnung);
        temp = state_wohngen.addNextState(new ActionState("Annahme erhalten"), new MessageCondition(kaufannahme.getUUID()));
        state_wohngen.addNextState(new ActionState("Ablehnung erhalten", true), new MessageCondition(kaufablehnung.getUUID()));
        state_wohngen = temp;
        state_wohngen = state_wohngen.addNextState(new ActionState("Immobilienkauf abwickeln"));
        state_wohngen = state_wohngen.addNextState(new ActionState("Mieter suchen"));
        state_wohngen = state_wohngen.addNextState(new SendState("Angebot senden", angebot));

        state_wohngen = state_wohngen.addNextState(new RecvState("Auf Entscheidung warten", annahme));
        ((RecvState) state_wohngen).addRecvdMessage(ablehnung);
        temp = state_wohngen.addNextState(new ActionState("Annahme erhalten"), new MessageCondition(annahme.getUUID()));
        state_wohngen.addNextState(new ActionState("Ablehnung erhalten", true), new MessageCondition(ablehnung.getUUID()));
        state_wohngen = temp;
        state_wohngen = state_wohngen.addNextState(new ActionState("Vertrag verhandeln"));
        state_wohngen = state_wohngen.addNextState(new SendState("Vertrag senden", vertrag));
        state_wohngen.addNextState(new ActionState("Wohnung übergeben", true));

        // Immobilienanbieter
        State state_immoanb = immoanb.setFirstState(new RecvState("Angebot erhalten", kaufangebot));
        state_immoanb = state_immoanb.addNextState(new ActionState("Entscheidung fällen"));
        temp = state_immoanb.addNextState(new SendState("Annahme senden", kaufannahme), new Condition("Angebot annehmen"));
        state_immoanb.addNextState(new SendState("Ablehnung senden", kaufablehnung, true), new Condition("Angebot ablehnen"));
        state_immoanb = temp;
        state_immoanb.addNextState(new ActionState("Immobilienverkauf abwickeln", true));

        //Mieter
        State state_mieter = mieter.setFirstState(new RecvState("Angebot erhalten", angebot));
        state_mieter = state_mieter.addNextState(new ActionState("Wohnung besichtigen"));
        state_mieter = state_mieter.addNextState(new ActionState("Entscheidung fällen"));
        temp = state_mieter.addNextState(new SendState("Annahme senden", annahme), new Condition("Angebot annehmen"));
        state_mieter.addNextState(new SendState("Ablehnung senden", ablehnung, true), new Condition("Angebot ablehnen"));
        state_mieter = temp;
        state_mieter = state_mieter.addNextState(new RecvState("Auf Vertrag warten", vertrag));
        state_mieter = state_mieter.addNextState(new ActionState("Vertrag abschließen"));
        state_mieter.addNextState(new ActionState("Wohnung übernehmen", true));

        return tradProz;
    }

    public static Set<Process> getDemoProcesses() {
        Set<Process> demoProcesses = new HashSet<>();
        demoProcesses.add(BUSEMProcess.gettraditionalProcess());

        return demoProcesses;
    }
}
