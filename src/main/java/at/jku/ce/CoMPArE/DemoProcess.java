package at.jku.ce.CoMPArE;

import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by oppl on 22/11/2016.
 */
public class DemoProcess {

    private static void addSubjectToProcess(Subject s, Process p) {
        p.addSubject(s);
    }

    private static void addMessageToProcess(Message m, Process p) {
        p.addMessage(m);
    }

    public static Process getDemoProcess() {
        Process p = new Process("Vacation Application (incomplete version)");

        Subject s1 = new Subject("Employee");
        Subject s2 = new Subject("Secretary");
        Subject s3 = new Subject("Boss");

        addSubjectToProcess(s1, p);
        addSubjectToProcess(s2, p);
        addSubjectToProcess(s3, p);

        Message applicationForm = new Message("Filled Application Form");
        Message checkedApplicationForm = new Message("Checked Application Form");
        Message confirmedApplication = new Message("Confirmed Application Form");
        Message confirmation = new Message("Confirmation");

        addMessageToProcess(applicationForm, p);
        addMessageToProcess(checkedApplicationForm, p);
        addMessageToProcess(confirmedApplication, p);
        addMessageToProcess(confirmation, p);

        State s = s1.setFirstState(new ActionState("Fill Application Form"));
        s = s.addNextState(new SendState("Send Application Form", applicationForm));
        s = s.addNextState(new RecvState("Wait for Confirmation", confirmation));
        s = s.addNextState(new ActionState("Book Holiday", true));

        s = s2.setFirstState(new RecvState("Wait for Application Form", applicationForm));
        s = s.addNextState(new ActionState("Check for Conflicts"));
        s = s.addNextState(new SendState("Forward checked Application", checkedApplicationForm));
        s = s.addNextState(new RecvState("Wait for confirmed Application", confirmedApplication));
        s = s.addNextState(new ActionState("File confirmed Application"));
        s = s.addNextState(new SendState("Send Confirmation", confirmation, true));

        s = s3.setFirstState(new RecvState("Wait for checked Application From", checkedApplicationForm));
        s = s.addNextState(new ActionState("Confirm Application"));
        s = s.addNextState(new SendState("Return confirmed Application Form", confirmedApplication, true));

        return p;
    }

    public static Process getComplexDemoProcess() {
        Process p = new Process("Vacation Application");

        Subject s1 = new Subject("Employee");
        Subject s2 = new Subject("Secretary");
        Subject s3 = new Subject("Boss");

        addSubjectToProcess(s1, p);
        addSubjectToProcess(s2, p);
        addSubjectToProcess(s3, p);

        Message applicationForm = new Message("Filled Application Form");
        Message checkedApplicationForm = new Message("Checked Application Form");
        Message confirmedApplication = new Message("Confirmed Application Form");
        Message declinedApplication = new Message("Declined Application Form");
        Message confirmation = new Message("Confirmation");
        Message rejection = new Message("Rejection");

        addMessageToProcess(applicationForm, p);
        addMessageToProcess(checkedApplicationForm, p);
        addMessageToProcess(confirmedApplication, p);
        addMessageToProcess(declinedApplication, p);
        addMessageToProcess(confirmation, p);
        addMessageToProcess(rejection, p);

        State s = s1.setFirstState(new ActionState("Fill Application Form"));
        s = s.addNextState(new SendState("Send Application Form", applicationForm));
        s = s.addNextState(new RecvState("Wait for Decision", confirmation));
        ((RecvState) s).addRecvdMessage(rejection);
        s.addNextState(new ActionState("Book Holiday", true), new MessageCondition(confirmation.getUUID()));
        s.addNextState(new ActionState("Be angry", true), new MessageCondition(rejection.getUUID()));

        s = s2.setFirstState(new RecvState("Wait for Application Form", applicationForm));
        s = s.addNextState(new ActionState("Check for Conflicts"));
        State temp = s.addNextState(new SendState("Forward checked Application", checkedApplicationForm), new Condition("no Conflicts"));
        s.addNextState(new SendState("Decline Application because of Conflicts", rejection), new Condition("Conflicts Identified"));
        s = temp;
        s = s.addNextState(new RecvState("Wait for Decision on Application", confirmedApplication));
        ((RecvState) s).addRecvdMessage(declinedApplication);
        s = s.addNextState(new ActionState("File decided Application"));
        s.addNextState(new SendState("Send confirmed Application", confirmation, true), new MessageCondition(confirmedApplication.getUUID()));
        s.addNextState(new SendState("Send declined Application", rejection, true), new MessageCondition(declinedApplication.getUUID()));

        s = s3.setFirstState(new RecvState("Wait for checked Application From", checkedApplicationForm));
        s = s.addNextState(new ActionState("Decide on Application"));
        s.addNextState(new SendState("Return confirmed Application Form", confirmedApplication, true), new Condition("confirm"));
        s.addNextState(new SendState("Return rejected Application Form", declinedApplication, true), new Condition("reject"));

        return p;
    }

    public static Process getSplitJoinDemoProcess() {
        Process p = new Process("Split/Join Demo");

        Subject s1 = new Subject("Actor");
        p.addSubject(s1);

        State s = s1.setFirstState(new ActionState("Decide whether to go left or right"));
        State left = new ActionState("Going left");
        State right = new ActionState("Going right");
        s.addNextState(left, new Condition("left"));
        s.addNextState(right, new Condition("right"));
        State straightAgain = new ActionState("Go straight ahead again", true);
        left.addNextState(straightAgain);
        right.addNextState(straightAgain);

        return p;
    }

    public static Process getLoopDemoProcess() {

        Process p = new Process("Loop Demo");

        Subject s1 = new Subject("Actor");
        p.addSubject(s1);

        State s = s1.setFirstState(new ActionState("Prepare for Jumping"));
        State loopState = new ActionState("Jump");
        State finalState = new ActionState("Relax", true);
        State restState = new ActionState("Rest");
        s.addNextState(loopState);
        loopState.addNextState(finalState, new Condition("done jumping"));
        loopState.addNextState(restState, new Condition("continue jumping"));
        restState.addNextState(loopState);

        return p;
    }

    public static Process getTask1Process() {
        Process p = new Process("Task 1");

        Subject s1 = new Subject("Employee");
        Subject s2 = new Subject("Secretary");
        Subject s3 = new Subject("Boss");

        addSubjectToProcess(s1, p);
        addSubjectToProcess(s2, p);
        addSubjectToProcess(s3, p);

        Message applicationForm = new Message("Filled Application Form");
        Message checkedApplicationForm = new Message("Checked Application Form");
        Message confirmedApplication = new Message("Confirmed Application Form");
        Message declinedApplication = new Message("Declined Application Form");
        Message confirmation = new Message("Confirmation");
        Message rejection = new Message("Rejection");

        addMessageToProcess(applicationForm, p);
        addMessageToProcess(checkedApplicationForm, p);
        addMessageToProcess(confirmedApplication, p);
        addMessageToProcess(declinedApplication, p);
        addMessageToProcess(confirmation, p);
        addMessageToProcess(rejection, p);

        State s = s1.setFirstState(new ActionState("Fill Application Form"));
        State jumpBack = s;
        s = s.addNextState(new SendState("Send Application Form", applicationForm));
        s = s.addNextState(new RecvState("Wait for Decision", confirmation));
        ((RecvState) s).addRecvdMessage(rejection);
        s.addNextState(new ActionState("Book Holiday", true), new MessageCondition(confirmation.getUUID()));
        s = s.addNextState(new ActionState("Try again with different date?"), new MessageCondition(rejection.getUUID()));
        s.addNextState(jumpBack, new Condition("yes"));
        s.addNextState(new ActionState("Be angry", true), new Condition("no"));

        s = s2.setFirstState(new RecvState("Wait for Application Form", applicationForm));
        s = s.addNextState(new ActionState("Check for Conflicts"));
        State temp = s.addNextState(new SendState("Forward checked Application", checkedApplicationForm), new Condition("no Conflicts"));
        s = s.addNextState(new SendState("Decline Application because of Conflicts", rejection, true), new Condition("Conflicts Identified"));
        s.addNextState(s2.getFirstState());
        s = temp;
        s = s.addNextState(new RecvState("Wait for Decision on Application", confirmedApplication));
        ((RecvState) s).addRecvdMessage(declinedApplication);
        s = s.addNextState(new ActionState("File decided Application"));
        temp = s.addNextState(new SendState("Send confirmed Application", confirmation, true), new MessageCondition(confirmedApplication.getUUID()));
        s = s.addNextState(new SendState("Send declined Application", rejection, true), new MessageCondition(declinedApplication.getUUID()));
//        temp.addNextState(s2.getFirstState());
//        s.addNextState(s2.getFirstState());

        s = s3.setFirstState(new RecvState("Wait for checked Application From", checkedApplicationForm));
        s = s.addNextState(new ActionState("Decide on Application"));
        temp = s.addNextState(new SendState("Return confirmed Application Form", confirmedApplication, true), new Condition("confirm"));
        s = s.addNextState(new SendState("Return rejected Application Form", declinedApplication, true), new Condition("reject"));
//        temp.addNextState(s3.getFirstState());
//        s.addNextState(s3.getFirstState());

        return p;

    }

    public static Process getTask2Process() {
        Process p = new Process("Task 2");

        Subject s1 = new Subject("Student");
        Subject s2 = new Subject("Professor");

        addSubjectToProcess(s1, p);
        addSubjectToProcess(s2, p);

        Message pruefungsAnmeldung = new Message("Examination registration");
        Message pruefungsAntritt = new Message("Examination attempt");
        Message pruefungsTermin = new Message("Examination date");
        Message pruefung = new Message("Examination");
        Message positiverSchein = new Message("Positive certificate");

        addMessageToProcess(pruefungsAnmeldung, p);
        addMessageToProcess(pruefungsTermin, p);
        addMessageToProcess(pruefungsAntritt, p);
        addMessageToProcess(positiverSchein, p);
        addMessageToProcess(pruefung, p);

        State s = s1.setFirstState(new SendState("Register for subject examination", pruefungsAnmeldung));
        s = s.addNextState(new RecvState("Wait for examination date", pruefungsTermin));
        s = s.addNextState(new ActionState("Study for examination"));
        s = s.addNextState(new RecvState("Wait for examination", pruefung));
        s = s.addNextState(new SendState("Take examiniation", pruefungsAntritt));
        s = s.addNextState(new RecvState("Wait for results", positiverSchein));
        s.addNextState(new ActionState("Celebrate", true));

        s = s2.setFirstState(new RecvState("Wait for examination registration", pruefungsAnmeldung));
        s = s.addNextState(new SendState("Set examination date", pruefungsTermin));
        s = s.addNextState(new ActionState("Prepare examination"));
        s = s.addNextState(new SendState("Provide examination to student", pruefung));
        s = s.addNextState(new RecvState("Test student", pruefungsAntritt));
        s = s.addNextState(new ActionState("Issue positive certificate"));
        s.addNextState(new SendState("Send positive certificate", positiverSchein, true));

        return p;
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

    public static Process getCommoningProcess() {

        Process commProz = new Process("Commoning Prozess");

        //Subjects
        Subject immoanb = new Subject("Immobilienanbieter");
        Subject mieter = new Subject("Mieter");
        Subject verein = new Subject("Hausverein");
        Subject gmbh = new Subject("Hausbesitz GmbH");
        Subject syndikat = new Subject("Mietshäuser Syndikat");

        addSubjectToProcess(immoanb, commProz);
        addSubjectToProcess(mieter, commProz);
        addSubjectToProcess(verein, commProz);
        addSubjectToProcess(gmbh, commProz);
        addSubjectToProcess(syndikat, commProz);

        //Messages
        Message syndikat_anmeldung = new Message("Syndikat-Anmeldung");
        Message bestätigung_anmeldung = new Message("Bestätigung-Anmeldung");

        Message vereinsgründung = new Message("Vereinsgrünung");
        Message bestätigung_vereinsgründung = new Message("Bestätigung-Vereinsgründung");

        Message gmbhgründung = new Message("GmbHgrünung");
        Message gmbhabwicklung = new Message("Gmbhabwicklung");
        Message bestätigung_gmbhgründung = new Message("Bestätigung-GmbH");

        Message kaufangebot = new Message("Kauf-Angebot");
        Message kaufannahme = new Message("Kauf-Annahme");
        Message kaufablehnung = new Message("Kauf-Ablehnung");
        Message kaufabwicklung = new Message("Kauf-Abwicklung");

        addMessageToProcess(syndikat_anmeldung, commProz);
        addMessageToProcess(bestätigung_anmeldung, commProz);

        addMessageToProcess(vereinsgründung, commProz);
        addMessageToProcess(bestätigung_vereinsgründung, commProz);

        addMessageToProcess(gmbhgründung, commProz);
        addMessageToProcess(gmbhabwicklung, commProz);
        addMessageToProcess(bestätigung_gmbhgründung, commProz);

        addMessageToProcess(kaufangebot, commProz);
        addMessageToProcess(kaufannahme, commProz);
        addMessageToProcess(kaufablehnung, commProz);
        addMessageToProcess(kaufabwicklung, commProz);

        //Immobilienanbieter
        State state_immoanbieter = immoanb.setFirstState(new ActionState("Immobilie zum Kauf anbieten"));
        state_immoanbieter = state_immoanbieter.addNextState(new SendState("Angebot senden", kaufangebot));

        state_immoanbieter = state_immoanbieter.addNextState(new RecvState("Auf Entscheidung warten", kaufannahme));
        ((RecvState) state_immoanbieter).addRecvdMessage(kaufablehnung);
        State temp = state_immoanbieter.addNextState(new ActionState("Annahme erhalten"), new MessageCondition(kaufannahme.getUUID()));
        state_immoanbieter.addNextState(new ActionState("Ablehnung erhalten", true), new MessageCondition(kaufablehnung.getUUID()));
        state_immoanbieter = temp;

        state_immoanbieter = state_immoanbieter.addNextState(new RecvState("Kauf abwickeln", kaufabwicklung));
        state_immoanbieter.addNextState(new ActionState("Immobilienkauf abwickeln", true));


        //Mieter
        State state_mieter = mieter.setFirstState(new RecvState("Angebot erhalten", kaufangebot));
        state_mieter = state_mieter.addNextState(new ActionState("Entscheidung treffen"));
        temp = state_mieter.addNextState(new SendState("Annahme senden", kaufannahme), new Condition("Angebot annehmen"));
        state_mieter.addNextState(new SendState("Ablehnung senden", kaufablehnung, true), new Condition("Angebot ablehnen"));
        state_mieter = temp;

        state_mieter = state_mieter.addNextState(new ActionState("Informationen sammeln"));
        state_mieter = state_mieter.addNextState(new SendState("Anmeldung beim Syndikat", syndikat_anmeldung));

        state_mieter = state_mieter.addNextState(new RecvState("Bestätigung für Anmeldung", bestätigung_anmeldung));
        state_mieter = state_mieter.addNextState(new SendState("Vereinsgründung veranlassen", vereinsgründung));

        state_mieter = state_mieter.addNextState(new RecvState("Bestätigung für Vereinsgründung", bestätigung_vereinsgründung));
        state_mieter = state_mieter.addNextState(new RecvState("Bestätigung für GmbH-Gründung", bestätigung_gmbhgründung));

        state_mieter.addNextState(new ActionState("Kapitalkosten, Solidaritätsbeiträge und Bewirtschaftungskosten bezahlen", true));

        //Hausverein
        State state_verein = verein.setFirstState(new RecvState("Verein soll gegründet werden", vereinsgründung));
        state_verein = state_verein.addNextState(new ActionState("Hausverein gründen"));
        state_verein = state_verein.addNextState(new SendState("Bestätigung senden", bestätigung_vereinsgründung));

        state_verein = state_verein.addNextState(new ActionState("Eigenmittel aufbringen, Direktkredit organisieren und Bankkredit aufnehmen"));
        state_verein = state_verein.addNextState(new ActionState("Vertragsklauseln festlegen"));
        state_verein = state_verein.addNextState(new ActionState("GmbH Vertrag erstellen	"));
        state_verein.addNextState(new SendState("GmbH Gründung veranlassen", gmbhgründung, true));

        //GmBH
        State state_gmbh = gmbh.setFirstState(new RecvState("GmbH soll gegründet werden", gmbhgründung));
        state_gmbh = state_gmbh.addNextState(new SendState("Gründungsabwicklung", gmbhabwicklung));
        state_gmbh = state_gmbh.addNextState(new ActionState("Hausbesitz GmbH Gründung abwickeln"));
        state_gmbh = state_gmbh.addNextState(new ActionState("Immobilienkauf vorbereiten"));
        state_gmbh = state_gmbh.addNextState(new SendState("Kauf Abwicklung", kaufabwicklung));

        state_gmbh = state_gmbh.addNextState(new ActionState("Immobilienkauf abwicklen"));
        state_gmbh = state_gmbh.addNextState(new SendState("Bestätigung senden", bestätigung_gmbhgründung));

        state_gmbh.addNextState(new ActionState("GmbH verwalten", true));

        //Syndikat
        State state_syndikat = syndikat.setFirstState(new RecvState("Anmeldung erhalten", syndikat_anmeldung));
        state_syndikat = state_syndikat.addNextState(new ActionState("Anmeldung durchführen"));
        state_syndikat = state_syndikat.addNextState(new SendState("Bestätigung senden", bestätigung_anmeldung));

        state_syndikat = state_syndikat.addNextState(new RecvState("Gründungsabwicklung", gmbhabwicklung));
        state_syndikat = state_syndikat.addNextState(new ActionState("Hausbesitz GmbH Gründung abwickeln"));
        state_syndikat.addNextState(new ActionState("Hausprojekt unterstützen", true));

        return commProz;
    }

    public static Process getTransformationsProcess() {

        Process transProz = new Process("Transformationsprozess");

        //Subjects
        Subject immoanb = new Subject("Immobilienanbieter");
        Subject wohngen = new Subject("Wohnungsgenossenschaft");
        Subject mieter = new Subject("Mieter");
        Subject verein = new Subject("Hausverein");
        Subject gmbh = new Subject("Hausbesitz GmbH");

        addSubjectToProcess(immoanb, transProz);
        addSubjectToProcess(wohngen, transProz);
        addSubjectToProcess(mieter, transProz);
        addSubjectToProcess(verein, transProz);
        addSubjectToProcess(gmbh, transProz);

        //Messages
        Message kaufangebot = new Message("Angebot");
        Message kaufannahme = new Message("Annahme");
        Message kaufablehnung = new Message("Ablehnung");

        Message vereinsgründung = new Message("Vereinsgrünung");
        Message bestätigung_vereinsgründung = new Message("Bestätigung-Vereinsgründung");

        Message gmbhgründung = new Message("GmbHgrünung");
        Message gmbhabwicklung = new Message("Gmbhabwicklung");
        Message bestätigung_gmbhgründung = new Message("Bestätigung-GmbH");

        Message kaufangebot_gen_mie = new Message("Kauf-Angebot");
        Message kaufannahme_gen_mie = new Message("Kauf-Annahme");
        Message kaufablehnung_gen_mie = new Message("Kauf-Ablehnung");
        Message kaufabwicklung_gen_mie = new Message("Kauf-Abwicklung");

        addMessageToProcess(kaufangebot, transProz);
        addMessageToProcess(kaufannahme, transProz);
        addMessageToProcess(kaufablehnung, transProz);

        addMessageToProcess(vereinsgründung, transProz);
        addMessageToProcess(bestätigung_vereinsgründung, transProz);

        addMessageToProcess(gmbhgründung, transProz);
        addMessageToProcess(gmbhabwicklung, transProz);
        addMessageToProcess(bestätigung_gmbhgründung, transProz);

        addMessageToProcess(kaufangebot_gen_mie, transProz);
        addMessageToProcess(kaufannahme_gen_mie, transProz);
        addMessageToProcess(kaufablehnung_gen_mie, transProz);
        addMessageToProcess(kaufabwicklung_gen_mie, transProz);

        // Immobilienanbieter
        State state_immoanb = immoanb.setFirstState(new RecvState("Angebot erhalten", kaufangebot));
        state_immoanb = state_immoanb.addNextState(new ActionState("Entscheidung fällen"));
        State temp = state_immoanb.addNextState(new SendState("Annahme senden", kaufannahme), new Condition("Angebot annehmen"));
        state_immoanb.addNextState(new SendState("Ablehnung senden", kaufablehnung, true), new Condition("Angebot ablehnen"));
        state_immoanb = temp;
        state_immoanb.addNextState(new ActionState("Immobilienverkauf abwickeln", true));

        // Wohnungsgenossenschaft
        State state_wohngen = wohngen.setFirstState(new ActionState("Informationen zu Immobilie sammeln"));
        state_wohngen = state_wohngen.addNextState(new ActionState("Entscheidung treffen"));
        temp = state_wohngen.addNextState(new ActionState("Angebot entwerfen"), new Condition("Immobilie kaufen"));
        state_wohngen.addNextState(new ActionState("Idee verwerfen", true), new Condition("Immobilie nicht kaufen"));
        state_wohngen = temp;
        state_wohngen = state_wohngen.addNextState(new SendState("Angebot senden", kaufangebot));

        state_wohngen = state_wohngen.addNextState(new RecvState("Auf Entscheidung warten", kaufannahme));
        ((RecvState) state_wohngen).addRecvdMessage(kaufablehnung);
        temp = state_wohngen.addNextState(new ActionState("Annahme erhalten"), new MessageCondition(kaufannahme.getUUID()));
        state_wohngen.addNextState(new ActionState("Ablehnung erhalten", true), new MessageCondition(kaufablehnung.getUUID()));
        state_wohngen = temp;
        state_wohngen = state_wohngen.addNextState(new ActionState("Immobilienkauf abwickeln"));
        state_wohngen = state_wohngen.addNextState(new ActionState("Entscheidung fällen"));

        temp = state_wohngen.addNextState(new ActionState("Immobilie zum Kauf anbieten"), new Condition("Immobilie verkaufen"));
        state_wohngen.addNextState(new ActionState("Vorgehen wie im traditionellen Prozess", true), new Condition("Immobilie nicht verkaufen"));
        state_wohngen = temp;
        state_wohngen = state_wohngen.addNextState(new SendState("Angebot senden", kaufangebot_gen_mie));

        state_wohngen = state_wohngen.addNextState(new RecvState("Auf Entscheidung warten", kaufannahme_gen_mie));
        ((RecvState) state_wohngen).addRecvdMessage(kaufablehnung_gen_mie);
        temp = state_wohngen.addNextState(new ActionState("Annahme erhalten"), new MessageCondition(kaufannahme_gen_mie.getUUID()));
        state_wohngen.addNextState(new ActionState("Ablehnung erhalten", true), new MessageCondition(kaufablehnung_gen_mie.getUUID()));
        state_wohngen = temp;
        state_wohngen = state_wohngen.addNextState(new RecvState("Gründungsabwicklung", gmbhabwicklung));
        state_wohngen = state_wohngen.addNextState(new ActionState("Hausbesitz GmbH Gründung abwickeln"));
        state_wohngen = state_wohngen.addNextState(new RecvState("Kauf abwickeln", kaufabwicklung_gen_mie));
        state_wohngen = state_wohngen.addNextState(new ActionState("Immobilienkauf abwickeln"));
        state_wohngen.addNextState(new ActionState("Hausprojekt unterstützen", true));

        //Mieter
        State state_mieter = mieter.setFirstState(new RecvState("Angebot erhalten", kaufangebot_gen_mie));
        state_mieter = state_mieter.addNextState(new ActionState("Entscheidung treffen"));
        temp = state_mieter.addNextState(new SendState("Annahme senden", kaufannahme_gen_mie), new Condition("Angebot annehmen"));
        state_mieter.addNextState(new SendState("Ablehnung senden", kaufablehnung_gen_mie, true), new Condition("Angebot ablehnen"));
        state_mieter = temp;

        state_mieter = state_mieter.addNextState(new ActionState("Informationen sammeln"));
        state_mieter = state_mieter.addNextState(new SendState("Vereinsgründung veranlassen", vereinsgründung));

        state_mieter = state_mieter.addNextState(new RecvState("Bestätigung für Vereinsgründung", bestätigung_vereinsgründung));
        state_mieter = state_mieter.addNextState(new RecvState("Bestätigung für GmbH-Gründung", bestätigung_gmbhgründung));

        state_mieter.addNextState(new ActionState("Kapitalkosten, Solidaritätsbeiträge und Bewirtschaftungskosten bezahlen", true));

        //Hausverein
        State state_verein = verein.setFirstState(new RecvState("Verein soll gegründet werden", vereinsgründung));
        state_verein = state_verein.addNextState(new ActionState("Hausverein gründen"));
        state_verein = state_verein.addNextState(new SendState("Bestätigung senden", bestätigung_vereinsgründung));

        state_verein = state_verein.addNextState(new ActionState("Eigenmittel aufbringen, Direktkredit organisieren und Bankkredit aufnehmen"));
        state_verein = state_verein.addNextState(new ActionState("Vertragsklauseln festlegen"));
        state_verein = state_verein.addNextState(new ActionState("GmbH Vertrag erstellen	"));
        state_verein.addNextState(new SendState("GmbH Gründung veranlassen", gmbhgründung, true));

        //GmBH
        State state_gmbh = gmbh.setFirstState(new RecvState("GmbH soll gegründet werden", gmbhgründung));
        state_gmbh = state_gmbh.addNextState(new SendState("Gründungsabwicklung", gmbhabwicklung));
        state_gmbh = state_gmbh.addNextState(new ActionState("Hausbesitz GmbH Gründung abwickeln"));
        state_gmbh = state_gmbh.addNextState(new ActionState("Immobilienkauf vorbereiten"));
        state_gmbh = state_gmbh.addNextState(new SendState("Kauf Abwicklung", kaufabwicklung_gen_mie));

        state_gmbh = state_gmbh.addNextState(new ActionState("Immobilienkauf abwicklen"));
        state_gmbh = state_gmbh.addNextState(new SendState("Bestätigung senden", bestätigung_gmbhgründung));

        state_gmbh.addNextState(new ActionState("GmbH verwalten", true));

        return transProz;
    }

    public static Set<Process> getDemoProcesses() {
        Set<Process> demoProcesses = new HashSet<>();
/*        demoProcesses.add(DemoProcess.getDemoProcess());
        demoProcesses.add(DemoProcess.getComplexDemoProcess());
        demoProcesses.add(DemoProcess.getSplitJoinDemoProcess());
        demoProcesses.add(DemoProcess.getLoopDemoProcess());
*/
        demoProcesses.add(DemoProcess.gettraditionalProcess());
        demoProcesses.add(DemoProcess.getCommoningProcess());
        demoProcesses.add(DemoProcess.getTransformationsProcess());

        return demoProcesses;
    }
}
