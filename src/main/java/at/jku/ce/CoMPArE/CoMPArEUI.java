package at.jku.ce.CoMPArE;

import at.jku.ce.CoMPArE.elaborate.*;
import at.jku.ce.CoMPArE.execute.Instance;
import at.jku.ce.CoMPArE.execute.InstanceHistoryStep;
import at.jku.ce.CoMPArE.process.*;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.scaffolding.ScaffoldingManager;
import at.jku.ce.CoMPArE.simulate.Simulator;
import at.jku.ce.CoMPArE.storage.FileStorageHandler;
import at.jku.ce.CoMPArE.visualize.VisualizeModel;
import at.jku.ce.CoMPArE.visualize.VisualizeModelEvolution;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.*;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.googleanalytics.tracking.GoogleAnalyticsTracker;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.SliderPanelStyles;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderPanelListener;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.util.*;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */

@Theme("demo")
@Push(transport = Transport.WEBSOCKET_XHR)
public class CoMPArEUI extends UI implements SliderPanelListener {
    Navigator navigator;
    private VerticalLayout pagebody;
    private MenuBar menuebar;
    private Map<Subject, Panel> subjectPanels;
    private Panel scaffoldingPanel;
    private HorizontalLayout mainLayoutFrame;
    private HorizontalLayout toolBar;
    private VerticalLayout mainInteractionArea;
    private HorizontalSplitPanel splitPanel;
    private GridLayout subjectLayout;
    private TabSheet visualizationTabs;
    private VerticalLayout tabLayout;
    private SliderPanel visualizationSlider;
    private SliderPanel historySlider;
    private VisualizeModel visualizeModel;

    private Process currentProcess;
    private Instance currentInstance;

    private GoogleAnalyticsTracker tracker;
    private ScaffoldingManager scaffoldingManager;
    private Simulator simulator;
    private StateClickListener stateClickListener;
    private boolean initialStartup;
    private boolean selectionMode;
    private boolean onboardingActive;
    private boolean elaborationAvailable;
    private boolean elaborationActive;
    private boolean doNotNotifyScaffoldingManager;

    private ProcessChangeHistory processChangeHistory;

    private Subject lastActiveSubject;

    private Button differentProcess;
    private Button simulate;
    private Button restart;
    private Button elaborationHistory;

    private FileStorageHandler fileStorageHandler;

    private long id;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        getPage().setTitle("Housing - selbstorganisiert wohnen, solidarisch wirtschaften");

        id = -1;
        initialStartup = true;
        selectionMode = false;
        onboardingActive = false;
        elaborationAvailable = true;
        elaborationActive = false;
        doNotNotifyScaffoldingManager = false;
        stateClickListener = null;
        currentProcess = DemoProcess.gettraditionalProcess();
        processChangeHistory = new ProcessChangeHistory();
        tracker = new GoogleAnalyticsTracker("UA-37510687-4", "auto");
        tracker.extend(this);
        currentInstance = new Instance(currentProcess);
        currentInstance.setProcessHasBeenChanged(true);

        scaffoldingPanel = new Panel("What to consider:");
        scaffoldingPanel.setWidth("950px");
        scaffoldingPanel.setHeight("200px");
        scaffoldingPanel.setContent(new GridLayout(3, 1));

        differentProcess = new Button("Anderen Prozess auswählen");
        differentProcess.addClickListener(e -> {
            selectDifferentProcess();
        });

        restart = new Button("Restart Process");
        toolBar = new HorizontalLayout();
        pagebody = new VerticalLayout();
        splitPanel = new HorizontalSplitPanel();

//changed        createBasicLayout();
        createGlobalLayout();
        pagebody.addComponent(createPageBodyForThemenfeld());

        simulator = new Simulator(currentInstance, subjectPanels, this);
//        scaffoldingManager = new ScaffoldingManager(currentProcess,scaffoldingPanel);

//changed        updateUI();

        Page.getCurrent().addBrowserWindowResizeListener(e -> {
            if (subjectLayout != null) {
                recalculateSubjectLayout((int) (e.getWidth() * (splitPanel.getSplitPosition() / 100)));
                splitPanel.setMaxSplitPosition((float) ((e.getWidth() * 0.7)), Unit.PIXELS);
                visualizeModel.changeSize(((int) ((e.getWidth() * (((100 - splitPanel.getSplitPosition()) / 100))) - e.getWidth() * 0.10)), (int) (e.getHeight() * 0.75));
            }
        });

        fileStorageHandler = new FileStorageHandler();
        if (!fileStorageHandler.isIDCookieAvailable()) {
//            GroupIDEntryWindow groupIDEntryWindow = new GroupIDEntryWindow(fileStorageHandler);
//            this.getUI().addWindow(groupIDEntryWindow);
        }
    }

    private void createGlobalLayout() {

        //ROOTLayout
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setWidth("100%");
        rootLayout.setSpacing(true);
        rootLayout.setMargin(true);


        //Pageheader
        HorizontalLayout pageheader = new HorizontalLayout();
        pageheader.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        pageheader.setWidth("100%");
        rootLayout.addComponent(pageheader);

        Label title = new Label("Housing");
        title.addStyleName(ValoTheme.LABEL_H1);
        pageheader.addComponent(title);

        VerticalLayout titleCommentLayout = new VerticalLayout();
        titleCommentLayout.setWidth("100%");
        titleCommentLayout.setDefaultComponentAlignment(Alignment.BOTTOM_RIGHT);
        Label titleComment = new Label("Selbstorganisiert wohnen, solidarisch wirtschaften");
        titleComment.addStyleName(ValoTheme.LABEL_H2);
        titleCommentLayout.addComponent(titleComment);
        pageheader.addComponent(titleCommentLayout);

        HorizontalLayout menue = new HorizontalLayout();
        menue.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        menue.setWidth("100%");
        menue.setHeight("45px");
        rootLayout.addComponent(menue);

        //Pagebody

        pagebody.setWidth("100%");
        pagebody.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        rootLayout.addComponent(pagebody);

        //Menü
        menuebar = new MenuBar();
        menuebar.setWidth("100%");
        menuebar.setHeight("40px");

        menuebar.addItem("Themenfeld", e -> {
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForThemenfeld());
        });
        menuebar.addItem("Analytische Betrachtung", e -> {
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForAnalytischeBetrachtung());
        });
        menuebar.addItem("Prozesse", null);
        menuebar.addItem("Schlussfolgerung", e -> {
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForSchlussfolgerung());
        });
        menuebar.getItems().get(2).addItem("Traditioneller Prozess", e -> {
            changeToNewProcess(DemoProcess.gettraditionalProcess());
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForTraditionellerProzess());
            pagebody.addComponent(createPageBodyForProzesse_Execution());
        });
        menuebar.getItems().get(2).addItem("Commoning Prozess", e -> {
            changeToNewProcess(DemoProcess.getCommoningProcess());
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForCommoningProzess());
            pagebody.addComponent(createPageBodyForProzesse_Execution());
        });
        menuebar.getItems().get(2).addItem("Transformations Prozess", e -> {
            changeToNewProcess(DemoProcess.getTransformationsProcess());
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForTransformationsProzess());
            pagebody.addComponent(createPageBodyForProzesse_Execution());
        });
        menue.addComponent(menuebar);

        this.setContent(rootLayout);
    }

    private Component createPageBodyForThemenfeld() {
        VerticalLayout content = new VerticalLayout();
        content.setWidth("100%");

        Label text1 = new Label(
                " <p><b>“Selbstorganisiert wohnen, solidarisch wirtschaften”</b></p>" +
                        "<p>So lautet das Motto vom Mietshäuser Syndikat. Schon seit über 20 Jahren sorgt das Unternehmen " +
                        "für Gemeineigentum an Haus und Grund, bezahlbaren Wohnraum für Menschen mit wenig Geld, " +
                        "Raum für Gruppen, politische Initiativen und das alles in Selbstorganisation [1]. </p>" +
                        "<p>Beim Commoning geht es in erster Linie um das Gemeinwohl d.h. dem Menschen im sozialen Gefüge. " +
                        "Geht es nach Herrn Manfred Blachfellner (Economy Coordinator for the Common Good Tyrol, " +
                        "Member of the Common Good Matrix Development-Team), lässt sich das Common Good in vier " +
                        "Bereiche aufteilen: </p>" +
                        "<ul>" +
                        "  <li>Menschenwürdigkeit</li>" +
                        "  <li>Solidarität und soziale Gerechtigkeit</li>" +
                        "  <li>Umweltverträglichkeit sowie </li>" +
                        "  <li>Transparenz und Mitbestimmung</li>" +
                        "</ul> " +
                        "<p>Diese vier Bereiche beschreiben den Gedanken des Commoning und das Fallbeispiel, dass wir " +
                        "gewählt haben – das Mietshäuser Syndikat – sehr gut.</p>", ContentMode.HTML);
        content.addComponent(text1);

        HorizontalLayout text2Layout = new HorizontalLayout();
        text2Layout.setWidth("100%");
        text2Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        content.addComponent(text2Layout);

        String basepath = VaadinService.getCurrent()
                .getBaseDirectory().getAbsolutePath();
        FileResource resource1 = new FileResource(new File(basepath + "/images/Uebersicht.png"));
        Image image1 = new Image("Abbildung 1 HAUSBESITZ GMBH ALS BINDEGLIED", resource1);
        image1.setWidth("100%");

        Label text2 = new Label("<p>Das Mietshäuser Syndikat:</p>" +
                "<ul>" +
                "  <li>berät selbstorganisierte Hausprojekte, die sich für das Syndikatsmodell interessieren,</li>" +
                "  <li>beteiligt sich an Projekten, damit diese dem Immobilienmarkt entzogen werden,</li>" +
                "  <li>hilft mit Know-How bei der Projektfinanzierung und</li>" +
                "  <li>initiiert neue Projekte.</li>" +
                "</ul> " +
                "<p>Was so einfach klingt bedingt einer besonderen Beteiligungsstruktur die zwischen " +
                "Hausprojekten und Syndikatsverbund notwendig ist, um die Immobilie in Richtung " +
                "„Entprivatisierung“ zu lenken [1].</p>" +
                "<p>Abbildung 1 zeigt einen Überblick über diese Struktur. Die Hausbesitz GmbH agiert als " +
                "Bindeglied zwischen Hausverein und Mietshäuser Syndikat. Sie besteht aus zwei Gesellschaftern," +
                " die das gleiche Stimmrecht haben, wodurch keiner der beiden überstimmt werden kann. D.h. die " +
                "Immobilie kann nur verkauft werden, sofern sich beide Parteien explizit darauf einigen, " +
                "wodurch ein Verkauf durch eine der Parteien unmöglich wird [1].</p>", ContentMode.HTML);
        text2Layout.addComponent(text2);
        text2Layout.addComponent(image1);

        Label text3 = new Label(
                "<p>Das Mietshäuser Syndikat fungiert als eine Art Kontrollorgan und dies nicht nur bei einem " +
                        "Hausprojekt, sondern bei allen Hausprojekten. Dadurch bildet das Syndikat die zentrale " +
                        "Schnittstelle im Netzwerk der Projekte. Durch die Erfahrungen aus bestehenden Hausprojekten " +
                        "und das große Netzwerk können neue Projekte durch Beratungsgespräche unterstützt werden [1].</p>" +
                        "<p>Um den Kauf von Immobilien als Gemeineigentum zu ermöglichen und den Wohnraum bezahlbar zu " +
                        "halten, besteht eine besondere Finanzierungsform. Die Immobilien werden in der Regel durch " +
                        "Direkt- und Bankkredite, zu fast gleichen Teilen, und einem geringeren Anteil an Eigenkapital " +
                        "finanziert. Die Rückzahlung der Kredite erfolgt durch die Einnahmen aus der Miete. D.h. hier " +
                        "können Personen, die diese Systeme unterstützen möchten, mit Direktkrediten helfen, solche " +
                        "Projekte umzusetzen [1].</p>", ContentMode.HTML);
        content.addComponent(text3);

        HorizontalLayout text4Layout = new HorizontalLayout();
        text4Layout.setWidth("100%");
        text4Layout.setSpacing(true);
        text4Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        content.addComponent(text4Layout);

        FileResource resource2 = new FileResource(new File(basepath + "/images/Finanzierung.png"));
        Image image2 = new Image("ABBILDUNG 2 – KOSTENVERTEILUNG [1]", resource2);
        image2.setWidth("80%");

        Label text4 = new Label(
                "<p>Wie in Abbildung 2 ersichtlich, wird zu Beginn der Mietlaufzeit ein größerer Anteil an " +
                        "Kapitalkosten und ein kleinerer Anteil an Solidarbeitrag bezahlt. Der Solidarbeitrag, ist " +
                        "ein Betrag, der an das Mietshäuser Syndikat bezahlt wird, die diese Beträge wiederum wieder " +
                        "für Finanzierungen von Hausprojekten verwenden. Im Laufe der Jahre werden die Kapitalkosten " +
                        "geringer und der Solidaranteil steigt. Neben den Kapitalkosten und Solidarbeitrag werden durch" +
                        " die Miete auch Bewirtschaftungskosten eingehoben. Diese werden für Sanierungen und " +
                        "Instandsetzungen am Objekt herangezogen [1].</p>"
                , ContentMode.HTML);
        text4Layout.addComponent(image2);
        text4Layout.addComponent(text4);


        Label text5 = new Label(
                "<p>Fasst man das Mietshäuser Syndikat nochmal zusammen, lässt sich das Ziel wie folgt beschreiben: </p>" +
                        "<ul>" +
                        "<li>Eine Gruppe tatendurstiger Menschen nimmt Häuser ins Visier in denn sie zusammenwohnen " +
                        "möchten. Sie suchen ausreichenden und vor allem selbstbestimmten Wohnraum, auch in Kombination" +
                        " mit öffentlichen Räumen für Veranstaltungen, für Gruppen und Projekte. </li>" +
                        "  <li>Langjährige Bewohner/innen eines Hauses resignieren nicht bei Verkaufsplänen des " +
                        "Hausbesitzers, sondern entwickeln eine Vision: Die Übernahme „ihres Hauses“ in Selbstorganisation. </li>" +
                        "</ul> ",
                ContentMode.HTML);
        text5.setWidth("100%");
        content.addComponent(text5);

        content.addComponent(new Label("</br><p>[1] Marilyn Lürtzing; Lothar Mühlbacher; Anita Selz, Das Mietshäuser Syndikat und die Hausprojekte. 2016.</p>", ContentMode.HTML));
        return content;
    }

    private Component createPageBodyForAnalytischeBetrachtung() {
        VerticalLayout content = new VerticalLayout();
        Label text1 = new Label(
                "<p>Wir haben versucht mit unserem Fallbeispiel des Mietshaus Syndikates ein Bild zu zeichnen, " +
                        "um ein besseres Verständnis vermitteln zu können. Wie bereits eingangs erörtert, geht es " +
                        "beim Commoning um das Allgemeinwohl. Dieses setzt sich aus Humanität, Solidarität, " +
                        "Nachhaltigkeit und Transparenz zusammen. </p>", ContentMode.HTML);
        content.addComponent(text1);

        HorizontalLayout text2Layout = new HorizontalLayout();
        text2Layout.setWidth("100%");
        text2Layout.setSpacing(true);
        text2Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        content.addComponent(text2Layout);

        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        FileResource resource1 = new FileResource(new File(basepath + "/images/commoning.png"));
        Image image1 = new Image("ABBILDUNG 1 COMMON", resource1);
        image1.setWidth("80%");

        Label text2 = new Label(
                "<p>Abbildung 1 zeigt, dass das „Common“ unseres Fallbeispiels aus drei Bereichen besteht und " +
                        "zwar den Commoners, dem Commoning und der Common-Pool-Ressource.</p>" +
                        "<p>" +
                        "<b>Commoners:</b> <BLOCKQUOTE>Hausverein, Hausbesitz GmbH, Mietshäuser Syndikat GmbH </BLOCKQUOTE>" +
                        "<b>Commoning:</b> <BLOCKQUOTE>Erwerb einer Immobilie, Verwaltung der Immobilie </BLOCKQUOTE> " +
                        "<b>Common-Pool-Ressource:</b> " +
                        "<ul>" +
                        "<li>Physisch-materielle Basis: <BLOCKQUOTE>Immobilie </BLOCKQUOTE></li>" +
                        "<li>Nicht-physische Basis: <BLOCKQUOTE>Wissen zur Abwicklung (Erwerb, Gründungen und Verwaltung der Immobilie)</BLOCKQUOTE></li>" +
                        "</ul></p>", ContentMode.HTML);
        text2Layout.addComponent(image1);
        text2Layout.addComponent(text2);

        Label text3 = new Label(
                "<p>Es wird verdeutlicht, dass eine einzelne Person nicht alleine den Erwerb der Immobilie " +
                        "bewerkstelligen muss, sondern dies in der Gemeinschaft geschieht. Die beteiligten Personen " +
                        "schließen sich zu einem Verein zusammen und werden vom Syndikat mit allen notwendigen " +
                        "Informationen versorgt, die diese benötigen, um die Immobilie zu erwerben und zu verwalten. " +
                        "Dabei stellt das Commoning den sozialen Prozess dar. Hier kommen auch die Prozessmodelle zur " +
                        "Anwendung. Die Common-Pool Ressource besteht aus zwei Teilen. Dem physisch materiellen – in " +
                        "unserem Fallbeispiel – der Immobilie, die erworben und bewohnt wird und einem nicht-physischen " +
                        "Teil, der das Wissen zur Abwicklung beinhaltet. All diese Schritte werden in der Gemeinschaft " +
                        "entschieden und durchgeführt. </p>",
                ContentMode.HTML);
        content.addComponent(text3);
        return content;
    }

    private Component createPageBodyForTraditionellerProzess() {
        VerticalLayout content = new VerticalLayout();
        content.setWidth("100%");

        Label processlabel = new Label("Referenzmodell");
        processlabel.addStyleName(ValoTheme.LABEL_H3);
        content.addComponent(processlabel);

        Label text1 = new Label("<p>Zur Veranschaulichung wie der traditionelle Prozess aussieht, wurde der " +
                "subjektorientierter Prozess in Abbildung 1 dargestellt. </p>", ContentMode.HTML);
        content.addComponent(text1);

        VerticalLayout text2Layout = new VerticalLayout();
        text2Layout.setWidth("100%");
        text2Layout.setSpacing(true);
        text2Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        FileResource resource1 = new FileResource(new File(basepath + "/images/Domain SID.png"));
        Image image1 = new Image("ABBILDUNG 1 TRADITIONELLES PROZESSMODELL – SUBJEKT-INTERAKTIONS-DIAGRAMM (SID) IN S-BPM", resource1);
        image1.setWidth("60%");

        Label text2 = new Label("<p>Der Prozess startet bei der Wohnungsgenossenschaft, die ein Angebot für einen " +
                "Immobilienkauf an den Immobilienanbieter sendet, welches der Immobilienanbieter annehmen oder ablehnen " +
                "kann. Wird das Angebot angenommen, sendet die Wohnungsgenossenschaft einer wohnungssuchenden Person " +
                "ein Wohnungsangebot, welches angenommen oder abgelehnt werden kann.</p>" +
                "<p>Was dies im Detail bedeutet wird in Abbildung 2 überblicksmäßig gezeigt und nachfolgend erläutert.</p>", ContentMode.HTML);
        content.addComponent(text2);
        content.addComponent(text2Layout);
        text2Layout.addComponent(image1);

        VerticalLayout text3Layout = new VerticalLayout();
        text3Layout.setWidth("100%");
        text3Layout.setSpacing(true);
        text3Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        FileResource resource2 = new FileResource(new File(basepath + "/images/DomainProzess.png"));
        Image image2 = new Image("ABBILDUNG 2 - TRADITIONELLES PROZESSMODELL IN BPMN", resource2);
        image2.setWidth("100%");
        Label text3 = new Label("<p>Wie die einzelnen Rollen untereinander agieren, wird anhand von Abbildung 2 " +
                "überblickmäßig dargestellt und nachfolgend im Detail erklärt:</p>", ContentMode.HTML);

        text3Layout.addComponent(text3);
        text3Layout.addComponent(image2);
        content.addComponent(text3Layout);

        Label text4 = new Label("<p>Der Prozess beginnt mit der Informationsbeschaffung zu einer bestimmten " +
                "Immobilie, die erworben werden soll (siehe 2). Es folgt eine Entscheidung, ob die Immobilie " +
                "gekauft werden soll oder nicht. Falls die Immobilie gekauft werden soll, wird ein Angebot entworfen, " +
                "welches dem Immobilienanbieter gesendet wird.</p>" +
                "<p>Dem Immobilienanbieter steht es frei das Angebot anzunehmen oder abzulehnen (siehe 1). </p>" +
                "<p>Kann die Wohnungsgenossenschaft die Immobilie erwerben, wird dem potentiellen Mieter die freie " +
                "Wohnung angeboten (siehe 3). </p>" +
                "<p>Dieser kann die Wohnung besichtigen und entscheiden, ob er das Wohnungsangebot annimmt oder ablehnt (siehe 5).</p>" +
                "<p>Es wird ein Mietvertrag zwischen Mieter und Wohnungsgenossenschaft geschlossen und die Wohnung " +
                "wird im letzten Schritt an den Mieter übergeben (siehe 4 und 6).</p>", ContentMode.HTML);

        content.addComponent(text4);

        return content;
    }

    private Component createPageBodyForCommoningProzess() {
        VerticalLayout content = new VerticalLayout();
        content.setWidth("100%");

        Label processlabel = new Label("Commoning Prozess");
        processlabel.addStyleName(ValoTheme.LABEL_H3);
        content.addComponent(processlabel);

        Label text1 = new Label("<p>Da die meisten Menschen sich mit dem Thema Haus und/oder Wohnung in Bezug auf " +
                "Miete oder Eigentum bereits irgendwann auseinandergesetzt haben und wir die Welt der Common‘s mit der " +
                "Welt der Prozesse verknüpfen wollten, wurden Ausschnitte aus der Realität, analog zum Fallbeispiel des " +
                "Mietshäuser Syndikat, gewählt. D.h. es wurden nicht alle Arbeitsschritte modelliert, sondern nur jene, " +
                "um den Commoning Prozess zu veranschaulichen. Damit diese vergleichbar gemacht werden konnten, wurden " +
                "nur die wichtigsten Bestandteile des Prozesses als subjektorientiertes S-BPM und konventionelle " +
                "Prozessmodell in BPMN modelliert. Um die Komplexität der Modelle zu verringern wurden bewusst einige " +
                "Entscheidungsschritte nicht ausmodelliert, da diese zur Darstellung des Fallbeispiels nicht relevant sind.</p>" +
                "<p>Wir weisen darauf hin, dass wir bezüglich der Finanzierung nur den groben Überblick modelliert und " +
                "den Immobilienankauf verkürzt dargestellt haben. Die Prozessmodelle sind schemenhaft und entsprechen " +
                "nur teilweise der Realität. </p>", ContentMode.HTML);
        content.addComponent(text1);

        VerticalLayout text2Layout = new VerticalLayout();
        text2Layout.setWidth("100%");
        text2Layout.setSpacing(true);
        text2Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        FileResource resource1 = new FileResource(new File(basepath + "/images/Commoning SID.png"));
        Image image1 = new Image("ABBILDUNG 1 COMMONING PROZESSMODELL – SUBJEKT-INTERAKTIONS-DIAGRAMM (SID) IN S-BPM", resource1);
        image1.setWidth("80%");

        Label text2 = new Label("<p>Zum besseren Verständnis haben wir in einem Subjekt-Interaktionsmodell alle " +
                "beteiligten Rollen des Commoning Prozesses modelliert. Der Prozess beginnt, wie in Abbildung 1 " +
                "dargestellt, beim Immobilienanbieter. Dieser stellt den Mietern das Angebot, die Immobilie in der diese " +
                "derzeit wohnen, zu erwerben. Die Mieter entscheiden in der Gemeinschaft, also nicht einer für sich, das " +
                "Angebot anzunehmen oder abzulehnen. Entscheiden sich die Mieter, die Immobilie zu erwerben, müssen " +
                "diese einen Hausverein gründen und in weiterer Folge gemeinsam mit der „Mietshäuser Syndikat GmbH“ " +
                "eine Hausbesitz GmbH gründen. </p>", ContentMode.HTML);
        content.addComponent(text2);
        content.addComponent(text2Layout);
        text2Layout.addComponent(image1);

        VerticalLayout text3Layout = new VerticalLayout();
        text3Layout.setWidth("100%");
        text3Layout.setSpacing(true);
        text3Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        FileResource resource2 = new FileResource(new File(basepath + "/images/CommoningProzess.png"));
        Image image2 = new Image("ABBILDUNG 2 - COMMONING PROZESSMODELL IN BPMN", resource2);
        image2.setWidth("100%");
        Label text3 = new Label("<p>Wie die einzelnen Rollen untereinander agieren, wird anhand von Abbildung 2 " +
                "überblickmäßig dargestellt und nachfolgend im Detail erklärt:</p>", ContentMode.HTML);

        text3Layout.addComponent(text3);
        text3Layout.addComponent(image2);
        content.addComponent(text3Layout);

        Label text4 = new Label("<p>Wie beim Subjekt-Interaktion Diagramm bereits beschrieben, beginnt dieser " +
                "Prozess mit einem Angebot vom Immobilienanbieter, der die bewohnte Immobilie den Bewohnern zum Kauf " +
                "anbietet bzw. den Mietern mitteilt, dass das Objekt verkauft wird (siehe 1). </p>" +
                "<p>Beim Commoning folgt nun eine gemeinschaftliche Entscheidung, ob die Mieter das Angebot annehmen " +
                "oder ablehnen möchten. Entscheidet sich die Mietergemeinschaft das Angebot anzunehmen, wird dies dem " +
                "Immobilienanbieter mitgeteilt und es werden alle relevanten Informationen gesammelt (siehe 3).</p>" +
                "<p>Dabei kann das „Mietshäuser Syndikat“ behilflich sein, d.h. sie geben Auskunft wie die weitere " +
                "Abwicklung des Kaufs zu erfolgen hat und beraten diese dabei. Hierzu ist jedoch eine Anmeldung beim " +
                "Mietshaus Syndikat notwendig (siehe 7). </p>" +
                "<p>Danach müssen die Mieter einen Hausverein gründen. Dies ist notwendig, um gemeinsam mit dem Mietshaus " +
                "Syndikat eine Hausbesitz GmbH zu gründen. Dieser Schritt ist, wie bereits erwähnt, wichtig, damit die " +
                "Immobilie nicht mehr veräußert werden kann, solange nicht beide Parteien darüber übereinstimmen und " +
                "sämtliche Entscheidungen, die Immobilie betreffend, gemeinschaftlich getroffen werden. In der GmbH " +
                "Satzung werden auch alle finanziellen Themen abgedeckt, wie beispielsweise die im Prozess dargestellten " +
                "Rückzahlungen. Zu Beginn werden die Kredite an die Geldgeber beglichen, dem Syndikat wird ein " +
                "Solidaritätsbeitrag geleistet und für die Immobilie Bewirtschaftungskosten angespart. </p>" +
                "<p>Beim Hausverein beginnt der Prozess mit der Gründung (siehe 5). D.h. ab dem Zeitpunkt, " +
                "wo dieser offiziell eingetragen ist, können die Mitglieder, also die Mieter, beginnen das Geld zu " +
                "organisieren. </p>" +
                "<p>Wie erwähnt hilft und berät dabei das Syndikat. Es können Eigenmittel eingebracht werden, welche " +
                "aber in einem Commoning Prozess nur einen sehr geringen Anteil haben. Weiters können Direktkredite bei " +
                "Personen gestellt werden, die das Mietshäuser Syndikat und seine Projekte unterstützen möchten oder " +
                "es können ganz gewöhnliche Bankkredite aufgenommen werden). Wurde die Kaufsumme aufgebracht, werden " +
                "die letzten Vertragsklauseln geklärt und festgelegt. Danach kommt es zur Gründung der Hausbesitz GmbH, " +
                "gemeinsam mit dem Mietshäuser Syndikat (siehe 8 und 6). </p>" +
                "<p>Wie in 6. ersichtlich, ist die Hausbesitz GmbH besonders für den Kaufabschluss notwendig. </p>" +
                "<p>Ist der Kauf zwischen Hausbesitz GmbH und Immobilienanbieter (siehe 2) abgeschlossen, " +
                "bedienen die Mieter die Kapitalkosten und einen Bewirtschaftungsbeitrag in Form einer Miete (siehe 4). " +
                "</p><br>", ContentMode.HTML);

        content.addComponent(text4);

        return content;
    }

    private Component createPageBodyForTransformationsProzess() {
        VerticalLayout content = new VerticalLayout();
        content.setWidth("100%");

        Label processlabel = new Label("Transformation");
        processlabel.addStyleName(ValoTheme.LABEL_H3);

        Label text1 = new Label("<p>In diesem Abschnitt widmen wir uns neben der Gegenüberstellung der " +
                "Transformation, d.h. wie der traditionelle Prozess mit dem Commoning Prozess verbunden werden kann.</p>" +
                "<p>Was anfangs sehr einfach und überschaubar wirkt stellt sich, wenn man den traditionellen Prozess " +
                "dem Commoning Prozess gegenüberstellt, als wesentlich umfangreicher heraus. Dies fällt schon " +
                "hinsichtlich der beteiligten Personen auf, da ein Immobilienanbieter, die Mieter, der Hausverein, " +
                "eine Hausbesitz GmbH und das Mietshäuser Syndikat involviert sind, während beim traditionellen Prozess " +
                "lediglich der Mieter, die Wohnungsgenossenschaft und der Immobilienanbieter benötigt wird. Aber auch " +
                "einige andere Punkte unterscheiden sich, wie in Tabelle 1 gezeigt wird.</p>", ContentMode.HTML);

        Table table = new Table("TABELLE 1 - GEGENÜBERSTELLUNG");
        table.setWidth("100%");

        table.addContainerProperty("Traditionelles Modell", String.class, null);
        table.addContainerProperty("Commoningmodell", String.class, null);

        table.addItem(new Object[]{"Fremdbestimmt durch Wohnungsgenossenschaft", "Selbstbestimmung in der Gemeinschaft zum Wohle der Gemeinschaft"}, 1);
        table.addItem(new Object[]{"Auslöser durch Wohnungsgenossenschaft", "Auslöser durch Immobilienanbieter"}, 2);
        table.addItem(new Object[]{"Verkauf der Immobilie möglich", "Kein herkömmlicher Verkauf mehr möglich – „Entprivatisierung“"}, 3);
        table.addItem(new Object[]{"Finanzierung obliegt der Wohnungsgenossenschaft", "Keine Eigenmittel notwendig"}, 4);
        table.addItem(new Object[]{"Mietersuche", "Keine Mietersuche notwendig"}, 5);
        table.setPageLength(table.size());

        VerticalLayout image1layout = new VerticalLayout();
        image1layout.setWidth("100%");
        image1layout.setSpacing(true);
        image1layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        FileResource resource1 = new FileResource(new File(basepath + "/images/Transformation SID.png"));
        Image image1 = new Image("ABBILDUNG 1 TRANSFORMATIONS PROZESSMODELL – SUBJEKT-INTERAKTIONS-DIAGRAMM (SID) IN S-BPMN", resource1);
        image1.setWidth("80%");

        Label text2 = new Label("<p>Folgend widmen wir uns der Transformation, d.h. wie der klassische Prozess" +
                " mit dem Commoning Prozess verbunden werden kann. Abbildung 1 zeigt überblicksmäßig die Subjekte und " +
                "deren Interaktionen im Transformationsprozess. Diese Lösung ist nur eine von zahlreichen Möglichkeiten" +
                ", wie das traditionelle Modell mit dem Commoningmodell verknüpft werden kann. </p>", ContentMode.HTML);

        VerticalLayout image2Layout = new VerticalLayout();
        image2Layout.setWidth("100%");
        image2Layout.setSpacing(true);
        image2Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        FileResource resource2 = new FileResource(new File(basepath + "/images/Transformationsprozess.png"));
        Image image2 = new Image("ABBILDUNG 2 - TRANSFORMATIONS PROZESSMODELL IN BPMN", resource2);
        image2.setWidth("100%");
        Label text3 = new Label("<p>Wie sich die Subjekte im Detail verhalten wird in Abbildung 2 dargestellt" +
                " und nachfolgend erklärt. </p>", ContentMode.HTML);

        Label text4 = new Label("<p>Bei unserer Transformation startet der Prozess, wie beim traditionellen " +
                "Prozess, bei der Wohnungsgenossenschaft, die eine Immobilie erwerben möchte. Dabei werden zuerst " +
                "Informationen über die Immobilie gesammelt und es wird dann die Entscheidung getroffen, dem " +
                "Immobilienanbieter ein Angebot zu senden oder nicht (siehe 2). </p>" +
                "<p>Wie beim traditionellen Prozess unterbreitet die Wohnungsgenossenschaft dem Immobilienanbieter ein" +
                " Angebot, dass angenommen oder abgelehnt werden kann (siehe 1). </p>" +
                "<p>Erwirbt die Genossenschaft das Objekt kann Sie entscheiden, ob die Immobilie weiterverkauft oder " +
                "behalten werden soll (siehe 3). An dieser Stelle wird die Entscheidung getroffen ob die " +
                "Immobilie im Besitz der Wohnungsgenossenschaft bleibt und somit der klassische Prozess weiterverfolgt " +
                "wird, oder ob die Immobilie weiterverkauft wird und der Commoning Prozess in Kraft treten soll.</p>" +
                "<p>Wird die Immobilie weiterverkauft, bietet die Wohnungsgenossenschaft die Immobilien den dort " +
                "wohnhaften Mietern zum Kauf an. Auch hier haben die Mieter wieder die Entscheidungsfreiheit das " +
                "Angebot anzunehmen oder abzulehnen (siehe 5). </p>" +
                "<p>Nehmen sie das Angebot an, so müssen die Mieter einen Hausverein gründen und die Geldmittel " +
                "aufbringen, um den Kauf abschließen zu können (siehe 7). </p>" +
                "<p>Gemeinsam mit der Wohnungsgenossenschaft gründet der Hausverein eine Hausbesitz GmbH, damit die " +
                "Immobilie „unverkäuflich“ wird und dem Markt entzogen wird, was für den „Common Good“ Gedanken " +
                "besonders wichtig ist (siehe 8 und 9).</p>" +
                "<p>Ist der Kauf abgeschlossen, bedienen die Mieter die Kapitalkosten und den Bewirtschaftungsbeitrag " +
                "in Form von Miete (siehe 6). </p><br>" +
                "<p>Zum besseren Verständnis stellen wir das Domänenmodell und das Commoningmodell wie in diesem " +
                "Abschnitt als BPMNModell bereits detailliert dargestellt nochmal zusammen (siehe Abbildung 3): </p>" +
                "<p></p>" +
                "<p></p>", ContentMode.HTML);

        VerticalLayout image3Layout = new VerticalLayout();
        image3Layout.setWidth("100%");
        image3Layout.setSpacing(true);
        image3Layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        FileResource resource3 = new FileResource(new File(basepath + "/images/Skizze_Modelle.png"));
        Image image3 = new Image("ABBILDUNG 3 - ZUSAMMENFASSUNG SID-MODELLE", resource3);
        image3.setWidth("100%");

        Label text5 = new Label("<p>Die Pfeile zeigen, welche Subjekte aus den einzelnen Modellen " +
                "übernommen werden können. Wie bereits eingangs beschrieben, können einzelne Prozessschritte " +
                "aus den jeweiligen Modellen übernommen und transformiert werden. </p>", ContentMode.HTML);

        content.addComponent(processlabel);
        content.addComponent(text1);
        content.addComponent(table);
        content.addComponent(text2);

        image1layout.addComponent(image1);
        content.addComponent(image1layout);

        content.addComponent(text3);

        image2Layout.addComponent(image2);
        content.addComponent(image2Layout);

        content.addComponent(text4);

        image3Layout.addComponent(image3);
        content.addComponent(image3Layout);

        content.addComponent(text5);

        return content;
    }

    private Component createPageBodyForSchlussfolgerung() {
        VerticalLayout content = new VerticalLayout();
        content.setWidth("100%");

        Label processlabel = new Label("So what?");
        processlabel.addStyleName(ValoTheme.LABEL_H3);
        content.addComponent(processlabel);

        Label text1 = new Label("<p>Zusammenfassend kann man sagen, dass das Commoningmodell, so wie in dem " +
                "Fallbeispiel dargestellt, nur funktioniert, wenn alle Mieter sich über den Kauf einig sind. Sind sie" +
                " dies nicht, dann kann auch das Commoning nicht umgesetzt werden. Es müssen die soziotechnischen" +
                " Gegebenheiten so sein, dass sich diese Form des Wohnens umsetzen lässt. Während sich der" +
                " traditionelle Prozess als relativ simpel darstellt merkt man beim Commoningmodell relativ schnell," +
                " dass wesentlich mehr Personen involviert sind. Es konnte aber auch gezeigt werden, dass die Transformation" +
                " von einem herkömmlichen und einem Commoningmodell sich relativ gut umsetzen lässt, indem einzelne" +
                " Konstrukte aus den jeweiligen Bereichen herausgelöst und neu zusammengefügt wurden. Steigende Miet-" +
                " und Grundstückspreise könnten in Zukunft die Menschen stark beeinflussen, sich auf solche Projekte" +
                " einzulassen. Das Risiko einer einzelnen Person wird durch die GmbH und dem Syndikat miniert und kann" +
                " mit dem herkömmlichen Immobilienmarkt unter Umständen durchaus konkurrieren.</p>", ContentMode.HTML);
        content.addComponent(text1);
        return content;
    }

    private Component createPageBodyForProzesse_Execution() {
        VerticalLayout content = new VerticalLayout();
        content.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        content.addStyleName(ValoTheme.LABEL_LIGHT);
        content.setWidth("100%");
        content.setHeightUndefined();
        Label label = new Label("Prozessausführung", ContentMode.HTML);
        label.addStyleName(ValoTheme.LABEL_H3);
        content.addComponent(label);
        content.addComponent(new Label("<p>Hier könnnen Sie den oben theoretisch erklärten Prozess " +
                "selbst durchlaufen, um ein besseres Verständnis für den Prozess zu bekommen und zu sehen wie einfach " +
                "bzw. wie komplex der jeweilige Prozess ist. Um den Prozess zu durchlaufen müssen sie nur in den jeweiligen " +
                " Subjekten “Weiter“ klicken. Sobald der Prozess zu Ende ist können Sie ihn über einen Button neu starten. </p>" +
                "<p>Um den Überblick in der Ausführung nicht zu verlieren können Sie das Prozessmodell rechts zur" +
                " Orientierung nutzen. Hier können Sie zwischen verschiedensten Sichten wählen. Der aktuelle " +
                "Prozessschritt wird grün hinterlegt. Bereits durchlaufene Prozessschritte werden grau hinterlegt. " +
                " Die Modelle werden automatisch generiert, wodurch sie bei jeder neuen Ausführung geringfügig" +
                " anders aussehen können. Inhaltlich bleiben sie jedoch gleich.</p>", ContentMode.HTML));
        createContentLayout();
        content.addComponent(mainLayoutFrame);
        updateUI();
        return content;
    }

    private void createContentLayout() {
        mainLayoutFrame = new HorizontalLayout();
        mainLayoutFrame.setWidth("100%");
        mainInteractionArea = new VerticalLayout();
        mainInteractionArea.setWidth("100%");

        HorizontalLayout toolBar = createreduzedToolbar();
        Component subjects = createSubjectLayout((int) (this.getPage().getBrowserWindowWidth() * (splitPanel.getSplitPosition() / 100)));
//        Component subjects = mySubjectLayout();

        createTabSheet();
        mainInteractionArea.addComponent(subjects);
        mainInteractionArea.addComponent(toolBar);

        if (onboardingActive) scaffoldingPanel.setVisible(false);
        mainInteractionArea.setMargin(true);
        mainInteractionArea.setSpacing(true);

        splitPanel.setWidth("100%");
        splitPanel.setMinSplitPosition(375, Unit.PIXELS);
        splitPanel.setMaxSplitPosition((float) ((getPage().getBrowserWindowWidth() * 0.7)), Unit.PIXELS);
        splitPanel.addSplitPositionChangeListener(e -> {
            recalculateSubjectLayout(((int) (this.getPage().getBrowserWindowWidth() * (splitPanel.getSplitPosition()) / 100)));
            visualizeModel.changeSize(((int) (this.getPage().getBrowserWindowWidth() * (((100 - splitPanel.getSplitPosition()) / 100)) - this.getPage().getBrowserWindowWidth() * 0.10)), (int) (this.getPage().getBrowserWindowHeight() * 0.75));
        });
        mainLayoutFrame.addComponent(splitPanel);

        splitPanel.setFirstComponent(mainInteractionArea);
        splitPanel.setSecondComponent(tabLayout);

    }

    private HorizontalLayout createreduzedToolbar() {
        toolBar.removeAllComponents();
        toolBar.setWidth("100%");
        toolBar.setHeight("5%");

        restart = new Button("Prozess neustarten");
        restart.addClickListener(e -> {
            LogHelper.logInfo("Execution: " + currentProcess + ": process restarted");
            mainLayoutFrame.removeAllComponents();
            createGlobalLayout();
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForProzesse_Execution()); //todo neu check
            currentInstance = new Instance(currentProcess);
            simulator = new Simulator(currentInstance, subjectPanels, this);
            updateUI();
        });

        if (onboardingActive) simulate.setVisible(false);
        toolBar.addComponent(restart);
//changed        toolBar.addComponent(differentProcess);
        toolBar.setSpacing(true);
        return toolBar;

    }

    private void createTabSheet() {
        tabLayout = new VerticalLayout();
        tabLayout.setMargin(true);
        tabLayout.setSpacing(true);
        tabLayout.setWidth("100%");
        visualizationTabs = new TabSheet();
        visualizationTabs.setWidth("100%");
        tabLayout.addComponent(visualizationTabs);

        VerticalLayout interaction = new VerticalLayout();
        interaction.setCaption("Interaktionen");
        VerticalLayout overall = new VerticalLayout();
        overall.setCaption("Gesamter Prozess");
        VerticalLayout overallFlow = new VerticalLayout();
        overallFlow.setCaption("Ablauf der Aktivitäten");
        visualizationTabs.addTab(overall, "Gesamter Prozess");
        visualizationTabs.addTab(interaction, "Interaktionen");
        visualizationTabs.addTab(overallFlow, "Ablauf der Aktivitäten");

        for (Subject s : currentProcess.getSubjects()) {
            VerticalLayout subjectVizualization = new VerticalLayout();
            subjectVizualization.setCaption(s.toString());
            visualizationTabs.addTab(subjectVizualization, s.toString());
        }

        visualizationTabs.addSelectedTabChangeListener(e -> {
            String selected = e.getTabSheet().getSelectedTab().getCaption();
//            // LogHelper.logDebug("Now processing visualizationTab "+selected);
            if (selected != null) {
                VerticalLayout vl = (VerticalLayout) e.getTabSheet().getSelectedTab();
                vl.removeAllComponents();
                //VisualizeModel visualizeModel = createVisualizeModel(this.getPage().getBrowserWindowWidth()- 400, (int)  (this.getPage().getBrowserWindowHeight() * 0.70));
                VisualizeModel visualizeModel = createVisualizeModel((int) (this.getPage().getBrowserWindowWidth() * ((100 - splitPanel.getSplitPosition()) / 100) - (this.getPage().getBrowserWindowWidth() * 0.10)), (int) (this.getPage().getBrowserWindowHeight() * 0.8));
                vl.addComponent(visualizeModel);
                if (onboardingActive && !doNotNotifyScaffoldingManager) {
                    scaffoldingManager.updateScaffolds(currentInstance, null);
                }
            }
        });
        visualizationTabs.setSelectedTab(interaction);
        visualizationTabs.setSelectedTab(overall);
    }

    private void updateTabSheet() {
        Component prevTab = visualizationTabs.getSelectedTab();
        visualizationTabs.setSelectedTab(visualizationTabs.getTab(visualizationTabs.getComponentCount()-1));
        visualizationTabs.setSelectedTab(prevTab);
    }

    private VisualizeModel createVisualizeModel(int width, int height) {
        String selected = visualizationTabs.getSelectedTab().getCaption();
        visualizeModel = new VisualizeModel(selected, this, width, height);

        visualizeModel.setCaption(selected);
        if (selected.equals("Interaktionen")) {
            visualizeModel.showSubjectInteraction(currentProcess);
            LogHelper.logInfo("Visualization: " + currentProcess + ": showing visualization of interaction model");

        }
        if (selected.equals("Gesamter Prozess")) {
            visualizeModel.showWholeProcess(currentProcess);
            visualizeModel.greyOutCompletedStates(currentInstance.getWholeHistory(), currentInstance.getAvailableStates().values());
            LogHelper.logInfo("Visualization: " + currentProcess + ": showing visualization of overall model");
        }
        if (selected.equals("Ablauf der Aktivitäten")) {
            visualizeModel.showWholeProcessFlow(currentProcess);
            visualizeModel.greyOutCompletedStates(currentInstance.getWholeHistory(), currentInstance.getAvailableStates().values());
            LogHelper.logInfo("Visualization: " + currentProcess + ": showing visualization of overall flow-oriented model");
        }
        if (!selected.equals("Interaktionen") && !selected.equals("Gesamter Prozess") && !selected.equals("Ablauf der Aktivitäten")) {
            Subject s = currentProcess.getSubjectWithName(selected);
            visualizeModel.showSubject(s);
            visualizeModel.greyOutCompletedStates(currentInstance.getHistoryForSubject(s), currentInstance.getAvailableStateForSubject(s));
            LogHelper.logInfo("Visualization: " + currentProcess + ": showing visualization of subject " + s);

        }
        return visualizeModel;
    }


    private void createBasicLayout() {

//        LogHelper.logDebug("Building basic layout");
        mainLayoutFrame = new HorizontalLayout();

//        SliderPanel scaffoldingSlider = createScaffoldingSlider(process, instance);
//        visualizationSlider = createVisualizationSlider();
        historySlider = createHistorySlider();

        mainInteractionArea = new VerticalLayout();

        HorizontalLayout toolBar = createToolbar();
        Component subjects = createSubjectLayout((int) (this.getPage().getBrowserWindowWidth() * (splitPanel.getSplitPosition() / 100)));

        HorizontalLayout hPadding = new HorizontalLayout();
        hPadding.setHeight("1px");
        hPadding.setWidth((this.getPage().getBrowserWindowWidth() - 150) + "px");
        mainInteractionArea.addComponent(hPadding);
        mainInteractionArea.addComponent(subjects);
        mainInteractionArea.addComponent(toolBar);
        createTabSheet();
        mainInteractionArea.addComponent(visualizationTabs);
//        mainInteractionArea.addComponent(scaffoldingPanel);
        if (onboardingActive) scaffoldingPanel.setVisible(false);
        //       mainInteractionArea.setMargin(true);
        //      mainInteractionArea.setSpacing(true);


        VerticalLayout vPadding = new VerticalLayout();
        vPadding.setWidth("50px");
        vPadding.setHeight(this.getPage().getBrowserWindowHeight() + "px");


//        mainLayoutFrame.addComponent(scaffoldingSlider);
//        mainLayoutFrame.addComponent(visualizationSlider);
        mainLayoutFrame.addComponent(historySlider);
//        if (onboardingActive) visualizationSlider.setVisible(false);
//        mainLayoutFrame.addComponent(vPadding);
        mainLayoutFrame.addComponent(mainInteractionArea);

        this.setContent(mainLayoutFrame);

    }

    private SliderPanel createScaffoldingSlider(Process process, Instance instance) {
        return new SliderPanelBuilder(scaffoldingPanel, "What to consider").mode(SliderMode.LEFT)
                .tabPosition(SliderTabPosition.BEGINNING).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();

    }

    private SliderPanel createVisualizationSlider() {
        VerticalLayout visualizationSliderContent = new VerticalLayout();
        visualizationSliderContent.removeAllComponents();
        visualizationSliderContent.setWidth((this.getPage().getBrowserWindowWidth() - 150) + "px");
        visualizationSliderContent.setHeight((this.getPage().getBrowserWindowHeight() - 150) + "px");
        visualizationSliderContent.setMargin(true);
        visualizationSliderContent.setSpacing(true);

        createTabSheet();
        visualizationSliderContent.addComponent(visualizationTabs);

        final SliderPanel visualizationSlider =
                new SliderPanelBuilder(visualizationSliderContent, "Show behaviour").mode(SliderMode.LEFT)
                        .tabPosition(SliderTabPosition.BEGINNING).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();

        visualizationSlider.addListener(this);
        return visualizationSlider;
    }

    @Override
    public void onToggle(boolean b) {
        if (b && !selectionMode) {

            String toBeActivated = null;
            Set<Subject> candidates = new HashSet<>();
            for (Subject s : subjectPanels.keySet()) {
                if (currentInstance.subjectCanProgress(s)) candidates.add(s);
            }
            if (candidates.size() == 0) toBeActivated = "Interaktionen";
            else if (candidates.size() == 1) toBeActivated = candidates.iterator().next().toString();
            else if (candidates.contains(lastActiveSubject)) toBeActivated = lastActiveSubject.toString();
            else toBeActivated = candidates.iterator().next().toString();

            Iterator<Component> i = visualizationTabs.iterator();
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(toBeActivated)) {
                    if (visualizationTabs.getSelectedTab() == tab) {
                        doNotNotifyScaffoldingManager = true;
                        visualizationTabs.setSelectedTab(visualizationTabs.getComponentCount() - 1);
                        doNotNotifyScaffoldingManager = false;
                    }
                    visualizationTabs.setSelectedTab(tab);
                }
            }
        }
        if (!b && selectionMode) {
            selectionMode = false;
            if (stateClickListener != null) {
                stateClickListener.clickedState(null);
                stateClickListener = null;
            }
        }
        if (!b && onboardingActive) {
            scaffoldingManager.updateScaffolds(currentInstance, null);
        }

    }

    private SliderPanel createHistorySlider() {
        VisualizeModelEvolution historySliderContent = new VisualizeModelEvolution(currentProcess, processChangeHistory);
        historySliderContent.setWidth((UI.getCurrent().getPage().getBrowserWindowWidth() - 150) + "px");
        historySliderContent.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight() - 150) + "px");
        final SliderPanel historySlider =
                new SliderPanelBuilder(historySliderContent, "Show history").mode(SliderMode.LEFT)
                        .tabPosition(SliderTabPosition.MIDDLE).style(SliderPanelStyles.COLOR_WHITE).flowInContent(true).animationDuration(500).build();
        historySlider.addListener(new HistoryListener(historySliderContent));
        return historySlider;

    }

    private HorizontalLayout createToolbar() {
        toolBar.removeAllComponents();

        simulate = new Button("Auto-progress");
        simulate.addClickListener(e -> {
            LogHelper.logInfo("Simulator: " + currentProcess + ": auto-progress triggered");
            selectionMode = true;
            Iterator<Component> i = visualizationTabs.iterator();
            String targetTab = new String("Interaktionen");
            if (currentProcess.getSubjects().size() == 1)
                targetTab = currentProcess.getSubjects().iterator().next().toString();
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(targetTab)) {
                    if (visualizationTabs.getSelectedTab() == tab) {
                        if (targetTab.equals("Interaktionen")) visualizationTabs.setSelectedTab(0);
                        else visualizationTabs.setSelectedTab(visualizationTabs.getComponentCount() - 1);
                    }
                    visualizationTabs.setSelectedTab(tab);
                }
            }
            Notification.show("Please select where to progress to.", Notification.Type.WARNING_MESSAGE);
            visualizationSlider.expand();
        });

        restart = new Button("Restart Process");
        restart.addClickListener(e -> {
            LogHelper.logInfo("Execution: " + currentProcess + ": process restarted");
            // LogHelper.logDebug("starting restart");
            mainLayoutFrame.removeAllComponents();
            // LogHelper.logDebug("creating layout");
//changed            createBasicLayout();
            createGlobalLayout();
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForProzesse_Execution()); //todo neu check
            // LogHelper.logDebug("updating scaffolds for finished instance");
            //scaffoldingManager.updateScaffolds(currentInstance);
            // LogHelper.logDebug("creating new instance");
            currentInstance = new Instance(currentProcess);
            // LogHelper.logDebug("updating scaffolds for finished step after reset");
            //scaffoldingManager.updateScaffolds(currentInstance,null);
            // LogHelper.logDebug("resetting simulator");
            simulator = new Simulator(currentInstance, subjectPanels, this);
            // LogHelper.logDebug("updating UI");
            updateUI();
        });

        elaborationHistory = new Button("Open Process Change History");
        elaborationHistory.addClickListener(e -> {
            LogHelper.logInfo("Elaboration: " + currentProcess + ": process change history opened");
            HistoryUI historyUI = new HistoryUI(processChangeHistory);
            this.getUI().addWindow(historyUI);
            historyUI.addCloseListener(e1 -> {
                rollbackChangesTo(historyUI.getSelectedTransaction());
                if (historyUI.getSelectedTransaction() != null)
                    LogHelper.logInfo("Elaboration: " + currentProcess + ": rolling back changes made through elaboration. Last undone transaction: " + historyUI.getSelectedTransaction());
                else
                    LogHelper.logInfo("Elaboration: " + currentProcess + ": process change history closed again without any changes");
            });
        });
        if (processChangeHistory.getHistory().isEmpty()) {
            elaborationHistory.setVisible(false);
            historySlider.setVisible(false);
        }

//        if (!currentProcess.getSubjects().isEmpty()) toolBar.addComponent(simulate);
        if (onboardingActive) simulate.setVisible(false);
        toolBar.addComponent(restart);
        toolBar.addComponent(differentProcess);
        //       toolBar.addComponent(elaborationHistory);
        toolBar.setSpacing(true);
        return toolBar;

    }

    private void rollbackChangesTo(ProcessChangeTransaction rollbackTo) {
        if (rollbackTo != null) {
            for (ProcessChangeTransaction transaction : processChangeHistory.getHistory()) {
                transaction.undo(currentProcess);
                if (transaction == rollbackTo) break;
            }
            InstanceHistoryStep instanceState = rollbackTo.getAffectedInstanceHistoryState();
            currentInstance.reconstructInstanceState(instanceState);
            processChangeHistory.removeUntil(rollbackTo);
            //changed            createBasicLayout();
            createGlobalLayout();
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForProzesse_Execution()); //todo neu check
            updateUI();
        }
    }

    public void notifyAboutClickedState(StateClickListener listener) {
        stateClickListener = listener;
    }

    public void informAboutSelectedNode(String vizName, String name) {
        if (!selectionMode) return;

        if (vizName.equals("Interaktionen")) {
            Iterator<Component> i = visualizationTabs.iterator();
            Subject selectedSubject = currentProcess.getSubjectByUUID(UUID.fromString(name));
            while (i.hasNext()) {
                Component tab = i.next();
                if (tab.getCaption().equals(selectedSubject.toString())) {
                    visualizationTabs.setSelectedTab(tab);
                    return;
                }
            }

        }

        State selectedState = currentProcess.getStateByUUID(UUID.fromString(name));
        if (selectedState == null) return;

        selectionMode = false;
        visualizationSlider.collapse();

        if (stateClickListener == null) {
            simulate(selectedState);
        } else {
            stateClickListener.clickedState(selectedState);
            stateClickListener = null;
        }
    }

    public void expandVisualizationSlider(Subject withSubject) {
        selectionMode = true;
        Iterator<Component> i = visualizationTabs.iterator();
        while (i.hasNext()) {
            Component tab = i.next();
            if (tab.getCaption().equals(withSubject.toString())) {
                doNotNotifyScaffoldingManager = true;
                if (visualizationTabs.getSelectedTab() == tab) {
                    visualizationTabs.setSelectedTab(visualizationTabs.getComponentCount() - 1);
                }
                visualizationTabs.setSelectedTab(tab);
                doNotNotifyScaffoldingManager = false;
            }
        }
        Notification.show("Please select the existing step you want to use.", Notification.Type.WARNING_MESSAGE);
        visualizationSlider.expand();
    }

    private Component recalculateSubjectLayout(int availableWidth) {
        int numberOfSubjects = subjectPanels.keySet().size();
        int numberOfColumns;
        int numberOfRows;
        if (availableWidth <= 350) {
            numberOfColumns = 1;
            numberOfRows = numberOfSubjects;
        } else {
            numberOfColumns = availableWidth / 350;
            numberOfRows = numberOfSubjects / numberOfColumns + 1;
        }

        GridLayout oldLayout = subjectLayout;

        subjectLayout = new GridLayout(numberOfColumns, numberOfRows);
        subjectLayout.setSpacing(true);
        for (Panel p : subjectPanels.values()) {
            subjectLayout.addComponent(p);
        }

        mainInteractionArea.replaceComponent(oldLayout, subjectLayout);

        return subjectLayout;
    }

    private Component createSubjectLayout(int availableWidth) {
        int numberOfSubjects = currentProcess.getSubjects().size();
        int numberOfColumns = availableWidth / 350;
        int numberOfRows = numberOfSubjects / numberOfColumns + 1;

        subjectLayout = new GridLayout(numberOfColumns, numberOfRows);

        subjectPanels = new HashMap<>();
        for (Subject s : currentProcess.getSubjects()) {
            Panel panel = new Panel(s.toString());
            panel.setWidth("300px");
            panel.setHeight("225px");
            VerticalLayout panelLayout = new VerticalLayout();
            panelLayout.setSpacing(true);
            panelLayout.setMargin(true);
            panel.setContent(panelLayout);
            subjectPanels.put(s, panel);
            subjectLayout.addComponent(panel);
        }
//        subjectLayout.setMargin(true);
        subjectLayout.setSpacing(true);

        Button addInitialSubject = new Button("Add a first actor");
        addInitialSubject.addClickListener(e -> {
            openElaborationOverlay(null, ElaborationUI.INITALSUBJECT);
        });

        if (currentProcess.getSubjects().isEmpty()) return addInitialSubject;
        else return subjectLayout;

    }

    private void updateUI() {
        differentProcess.setVisible(false);

        for (Subject s : currentInstance.getProcess().getSubjects()) {
            fillSubjectPanel(s);
        }
        updateTabSheet();
        if (initialStartup) {
            differentProcess.setVisible(true);
            initialStartup = false;
        }

        if (!currentInstance.processFinished() && !currentInstance.processIsBlocked()) {
            restart.setVisible(false);
            if (!onboardingActive) scaffoldingPanel.setVisible(true);
        }

        if (!currentInstance.processFinished() && currentInstance.processIsBlocked()) {
            // LogHelper.logDebug("Process blocked, offering to restart ...");
//             scaffoldingPanel.setVisible(false);
            restart.setVisible(true);
            if (currentInstance.isProcessHasBeenChanged()) {
                if (fileStorageHandler == null) fileStorageHandler = new FileStorageHandler();
              /*  if (!fileStorageHandler.isIDCookieAvailable()) {
                    GroupIDEntryWindow groupIDEntryWindow = new GroupIDEntryWindow(fileStorageHandler);
                    this.getUI().addWindow(groupIDEntryWindow);
                    groupIDEntryWindow.addCloseListener(e -> {
                        fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                        fileStorageHandler.saveToServer();
                    });
                } else {
                    fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                    fileStorageHandler.saveToServer();
                }*/
            }
            Button download = new Button("Download");
            download.addClickListener(e -> {
                LogHelper.logInfo("Download: " + currentProcess + ": Download Button clicked");
                fileStorageHandler.openDownloadWindow(this.getUI());
            });
            //changed         toolBar.addComponent(download);
        }

        if (currentInstance.processFinished()) {
            // simulate.setVisible(false);
            // LogHelper.logDebug("Process finished, offering to restart ...");
            mainLayoutFrame.removeComponent(scaffoldingPanel);
            scaffoldingPanel.setVisible(false);
            if (currentInstance.getProcess().getSubjects().size() > 0) {
                restart.setVisible(true);
                if (currentInstance.isProcessHasBeenChanged()) {
                    if (fileStorageHandler == null) fileStorageHandler = new FileStorageHandler();
                   /* if (!fileStorageHandler.isIDCookieAvailable()) {
                        GroupIDEntryWindow groupIDEntryWindow = new GroupIDEntryWindow(fileStorageHandler);
                        this.getUI().addWindow(groupIDEntryWindow);
                        groupIDEntryWindow.addCloseListener(e -> {
                            fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                            fileStorageHandler.saveToServer();
                        });
                    } else {
                        fileStorageHandler.addProcessToStorageBuffer(currentInstance.getProcess());
                        fileStorageHandler.saveToServer();
                    }*/
                }
                Button download = new Button("Download");
                download.addClickListener(e -> {
                    fileStorageHandler.openDownloadWindow(this.getUI());
                    LogHelper.logInfo("Download: " + currentProcess + ": Download Button clicked");
                });
                //changed           toolBar.addComponent(download);
            }
            differentProcess.setVisible(true);
        }
    }

    private void fillSubjectPanel(Subject s) {

        VerticalLayout panelContent = (VerticalLayout) subjectPanels.get(s).getContent();
        final OptionGroup conditions = new OptionGroup("Wählen Sie eine der folgenden Optionen:");
        panelContent.removeAllComponents();

        Label availableMessageList = new Label("");
        Label processMessageLabel = new Label("");
        Label expectedMessageLabel = new Label("<small>The following messages are expected from" + s + ", but are not currently provided:</small>", ContentMode.HTML);
        final ComboBox expectedMessageSelector = new ComboBox("please select:");
        Button expectedMessageSend = new Button("Send");

        Set<Message> availableMessages = currentInstance.getAvailableMessagesForSubject(s);
        if (availableMessages != null && availableMessages.size() > 0) {
            StringBuffer list = new StringBuffer("<small>The following messages are available:<ul>");
            for (Message m : availableMessages) {
                list.append("<li>" + m.toString() + "</li>");
            }
            list.append("</ul></small>");
            availableMessageList = new Label(list.toString(), ContentMode.HTML);
        }
        if (currentInstance.getLatestProcessedMessageForSubject(s) != null) {
            processMessageLabel = new Label("<small>Kürzlich erhaltene Nachricht:<ul><li>" + currentInstance.getLatestProcessedMessageForSubject(s) + "</li></ul></small>", ContentMode.HTML);
        }

        if (s.getExpectedMessages().size() > 0) {
            for (Message m : s.getExpectedMessages()) {
                expectedMessageSelector.addItem(m);
            }
            expectedMessageSelector.setValue(s.getExpectedMessages().iterator().next());
            expectedMessageSend.addClickListener(e -> {
                Message m = (Message) expectedMessageSelector.getValue();
                Subject recipient = currentInstance.getProcess().getRecipientOfMessage(m);
                currentInstance.putMessageInInputbuffer(recipient, m);
                updateUI();
            });
        }

        StringBuffer providedMessages = new StringBuffer();
        if (s.getProvidedMessages().size() > 0) {
            providedMessages.append("<small>The following messages are provided to " + s + " but are not currently used:<ul>");
            for (Message m : s.getProvidedMessages()) {
                providedMessages.append("<li>" + m + "</li>");
            }
            providedMessages.append("</ul></small>");
        }
        Label providedMessagesLabel = new Label(providedMessages.toString(), ContentMode.HTML);

        State currentState = currentInstance.getAvailableStateForSubject(s);
        if (currentState != null) {
            Label label1 = new Label(currentState.toString(), ContentMode.HTML);
            panelContent.addComponent(label1);

            Set<State> nextPossibleSteps = currentInstance.getNextStatesOfSubject(s);
            if (nextPossibleSteps != null && nextPossibleSteps.size() > 0) {
                if (nextPossibleSteps.size() == 1) {
                    State nextState = nextPossibleSteps.iterator().next();
                    if (currentInstance.getConditionForStateInSubject(s, nextState) != null) {
                        Label label2 = new Label("You can only progress under the following condition: <br>" + currentInstance.getConditionForStateInSubject(s, nextState), ContentMode.HTML);
                        panelContent.addComponent(label2);
                    }
                } else {
                    if (currentInstance.subjectCanProgress(s)) {
                        boolean toBeShown = false;
                        for (State nextState : nextPossibleSteps) {
                            Condition condition = currentInstance.getConditionForStateInSubject(s, nextState);
                            conditions.addItem(condition);
                            if (!(condition instanceof MessageCondition)) toBeShown = true;
                        }
                        conditions.addValueChangeListener(event -> {
//                            // LogHelper.logDebug("UI: condition for subject " + s + " changed to " + event.getProperty().getValue());
                        });
                        if (toBeShown) panelContent.addComponent(conditions);
                    }
                }
            }

        } else {
            Label label1 = new Label("Ende");
            if (!(s.toString().equals(Subject.ANONYMOUS))) panelContent.addComponent(label1);
        }

        Button perform = new Button("Weiter");
        perform.addClickListener(e -> {
//            // LogHelper.logDebug("UI: clicking on perfom button for subject "+s);
            lastActiveSubject = s;
            Condition c = null;
            if (conditions.size() > 0) c = (Condition) conditions.getValue();
            currentInstance.advanceStateForSubject(s, c, false);
            updateUI();
            // scaffoldingManager.updateScaffolds(currentInstance,currentState);
        });

        Button elaborate = new Button("I have a problem here");
        elaborate.addClickListener(e -> {
            perform.setEnabled(false);
            elaborate.setEnabled(false);
            openElaborationOverlay(s, ElaborationUI.ELABORATE);
            if (onboardingActive)
                scaffoldingManager.updateScaffolds(currentInstance, currentInstance.getAvailableStateForSubject(s));

        });

        perform.setEnabled(currentInstance.subjectCanProgress(s));

        Button addInitialStep = new Button("Add an initial step");
        addInitialStep.addClickListener(e -> {
            openElaborationOverlay(s, ElaborationUI.INITIALSTEP);

        });

        Button addAdditionStep = new Button("Add an additional step");
        addAdditionStep.addClickListener(e -> {
            openElaborationOverlay(s, ElaborationUI.ADDITIONALSTEP);
        });

        if (!(s.toString().equals(Subject.ANONYMOUS))) panelContent.addComponent(perform);
        //    if (elaborationAvailable /*&& currentInstance.subjectCanProgress(s)*/) panelContent.addComponent(elaborate);
        if (!availableMessages.isEmpty()) panelContent.addComponent(availableMessageList);
        if (!processMessageLabel.getValue().equals("")) panelContent.addComponent(processMessageLabel);
        if (s.getExpectedMessages().size() > 0) {
            panelContent.addComponents(expectedMessageLabel, expectedMessageSelector, expectedMessageSend);
        }
        if (s.getProvidedMessages().size() > 0) {
            panelContent.addComponent(providedMessagesLabel);
        }
        //       if (elaborationAvailable && s.getFirstState() == null && !s.toString().equals(Subject.ANONYMOUS)) panelContent.addComponent(addInitialStep);
//        if (elaborationAvailable && currentInstance.subjectFinished(s) && s.getFirstState() != null) panelContent.addComponent(addAdditionStep);
    }

    private void openElaborationOverlay(Subject s, int mode) {
        elaborationActive = true;
        ElaborationUI elaborationUI = new ElaborationUI(processChangeHistory);
        getUI().addWindow(elaborationUI);

        if (mode == ElaborationUI.ELABORATE) elaborationUI.elaborate(s, currentInstance);
        if (mode == ElaborationUI.INITIALSTEP) elaborationUI.initialStep(s, currentInstance);
        if (mode == ElaborationUI.ADDITIONALSTEP) elaborationUI.additionalStep(s, currentInstance);
        if (mode == ElaborationUI.INITALSUBJECT) elaborationUI.initialSubject(currentInstance);

        elaborationUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                elaborationActive = false;
                currentInstance.removeLatestHistoryStepForSubject(s);
                if (!processChangeHistory.getHistory().isEmpty()) {
                    elaborationHistory.setVisible(true);
                    historySlider.setVisible(true);
                }
//changed            createBasicLayout();
                createGlobalLayout();
                pagebody.removeAllComponents();
                pagebody.addComponent(createPageBodyForProzesse_Execution()); //todo neu check
                scaffoldingManager.updateScaffolds(currentInstance, currentInstance.getAvailableStateForSubject(s));
                updateUI();
            }
        });

    }

    public boolean isElaborationActive() {
        return elaborationActive;
    }

    private void selectDifferentProcess() {
        ProcessSelectorUI processSelectorUI = new ProcessSelectorUI();
        getUI().addWindow(processSelectorUI);
        processSelectorUI.showDemoProcessSelector();
// changed       processSelectorUI.showProcessSelector();
        processSelectorUI.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                Process newProcess = processSelectorUI.getSelectedProcess();
                if (newProcess != null) {
                    processChangeHistory = processSelectorUI.getProcessChangeHistory();

                }
                changeToNewProcess(newProcess);

            }
        });

    }

    public void changeToNewProcess(Process newProcess) {
        LogHelper.logInfo("Execution: " + newProcess + ": New process loaded (former process: " + currentProcess + ")");
        if (newProcess != null) {
            currentProcess = newProcess;
            initialStartup = true;
//changed            processChangeHistory = processSelectorUI.getProcessChangeHistory();
            currentInstance = new Instance(currentProcess);
//changed            createBasicLayout();
            createGlobalLayout();
            pagebody.removeAllComponents();
            pagebody.addComponent(createPageBodyForProzesse_Execution()); //todo neu check
            simulator = new Simulator(currentInstance, subjectPanels, CoMPArEUI.this);
            if (fileStorageHandler == null) fileStorageHandler = new FileStorageHandler();
            fileStorageHandler.newProcessStarted();
            updateUI();
        }
    }

    public ProcessChangeHistory getProcessChangeHistory() {
        return processChangeHistory;
    }

    public boolean simulate(State toState) {
        boolean simSuccessful = simulator.simulatePathToState(toState);
        if (!simSuccessful) Notification.show("Could not go to " + toState,
                "The process has already been executed too far. Finish this round and try again after restarting.",
                Notification.Type.ASSISTIVE_NOTIFICATION);
        return simSuccessful;
    }

    public Map<Subject, Panel> getSubjectPanels() {
        return subjectPanels;
    }

    public Panel getScaffoldingPanel() {
        return scaffoldingPanel;
    }

    public HorizontalLayout getToolBar() {
        return toolBar;
    }

    public GridLayout getSubjectLayout() {
        return subjectLayout;
    }

    public TabSheet getVisualizationTabs() {
        return visualizationTabs;
    }

    public SliderPanel getVisualizationSlider() {
        return visualizationSlider;
    }

    public Button getDifferentProcess() {
        return differentProcess;
    }

    public Button getSimulate() {
        return simulate;
    }

    public Button getRestart() {
        return restart;
    }

    public void setOnboardingActive(boolean onboardingActive) {
        this.onboardingActive = onboardingActive;
        this.elaborationAvailable = !onboardingActive;
        for (Subject s : subjectPanels.keySet())
            fillSubjectPanel(s);
    }

    public void setElaborationAvailable(boolean elaborationAvailable) {
        this.elaborationAvailable = elaborationAvailable;
        for (Subject s : subjectPanels.keySet())
            fillSubjectPanel(s);

    }

    public Subject getLastActiveSubject() {
        return lastActiveSubject;
    }

    @WebServlet(urlPatterns = "/*", name = "CoMPArEServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CoMPArEUI.class, productionMode = false, widgetset = "at.jku.ce.CoMPArE.CoMPArEWidgetSet")
    public static class CoMPArEServlet extends VaadinServlet {

        private final static String resultFolderKey = "at.jku.ce.CoMPAreE.resultfolder";
        private static String resultFolderName = null;

        public static String getResultFolderName() {
            return resultFolderName;
        }

        protected void servletInitialized() throws ServletException {
            super.servletInitialized();

            // Get the result folder as defined in WEB-INF/web.xml
            resultFolderName = getServletConfig().getServletContext().getInitParameter(resultFolderKey);

            File fRf = new File(resultFolderName);
            boolean isWorking = fRf.exists() && fRf.isDirectory()
                    || fRf.mkdirs();
            if (!isWorking) {
                resultFolderName = null;
            }
        }


    }

    public class HistoryListener implements SliderPanelListener {

        VisualizeModelEvolution historySliderContent;

        public HistoryListener(VisualizeModelEvolution historySliderContent) {

            this.historySliderContent = historySliderContent;
        }

        @Override
        public void onToggle(boolean b) {

            if (b) {
                historySliderContent.createLayout();
                LogHelper.logInfo("HistoryVisualization: " + currentProcess + ": history slider opened");
            } else {
                LogHelper.logInfo("HistoryVisualization: " + currentProcess + ": history slider closed");
            }
        }
    }
}