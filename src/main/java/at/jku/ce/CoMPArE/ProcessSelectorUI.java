package at.jku.ce.CoMPArE;

import at.jku.ce.CoMPArE.elaborate.ProcessChangeHistory;
import at.jku.ce.CoMPArE.process.Process;
import at.jku.ce.CoMPArE.storage.LoadFromArchiveWindow;
import at.jku.ce.CoMPArE.storage.UploadWindow;
import com.vaadin.ui.*;

import java.io.File;

/**
 * Created by oppl on 26/11/2016.
 */
public class ProcessSelectorUI extends Window {
    FormLayout fLayout = new FormLayout();
    File file;

    private Process selectedProcess;
    private ProcessChangeHistory processChangeHistory;

    public ProcessSelectorUI() {
        super("Wählen Sie einen neuen Prozess");
        this.setWidth("900px");
        this.setHeight("500px");
        this.center();
        fLayout.setMargin(true);
        fLayout.setSpacing(true);
        setContent(fLayout);
        selectedProcess = null;
        processChangeHistory = new ProcessChangeHistory();
    }

    public void showDemoProcessSelector() {
        this.getUI().addWindow(new SelectDemoWindow());
    }

    public void showProcessSelector() {

        final Button confirm = new Button("Done");
        final Label questionPrompt = new Label("Select a new process to be explored:");
        final TextField inputField = new TextField("How should it be named?");

        final OptionGroup availableSelectionOptions = new OptionGroup("Please select:");
        final String optionSpecifyMyself = new String("I want to start a new process from scratch.");
        final String optionUpload = new String("I want to upload a process file or an archive.");
        final String optionSelectFromServer = new String("I want to select a process from my archives on the server.");
        final String optionSelectDemo = new String("I want to select one of the demo processes.");

        availableSelectionOptions.addItem(optionSpecifyMyself);
        availableSelectionOptions.addItem(optionUpload);
        availableSelectionOptions.addItem(optionSelectFromServer);
        availableSelectionOptions.addItem(optionSelectDemo);

        availableSelectionOptions.addValueChangeListener(e -> {
            Object selectedItem = e.getProperty().getValue();
            if (selectedItem == optionUpload) {
                confirm.setCaption("Upload");
                inputField.setVisible(false);
            }
            if (selectedItem == optionSelectFromServer || selectedItem == optionSelectDemo) {
                confirm.setCaption("Select");
                inputField.setVisible(false);
            }
            if (selectedItem == optionSpecifyMyself) {
                confirm.setCaption("Done");
                inputField.setVisible(true);
            }
        });

        confirm.addClickListener(e -> {
            if (availableSelectionOptions.getValue() == optionSpecifyMyself) {
                selectedProcess = new Process(inputField.getValue());
                this.close();
            }
            if (availableSelectionOptions.getValue() == optionSelectDemo) {
                this.getUI().addWindow(new SelectDemoWindow());
            }
            if (availableSelectionOptions.getValue() == optionSelectFromServer) {
                this.getUI().addWindow(new LoadFromArchiveWindow(this));
            }
            if (availableSelectionOptions.getValue() == optionUpload) {
                this.getUI().addWindow(new UploadWindow(this));
            }
        });

        fLayout.removeAllComponents();
        fLayout.addComponent(questionPrompt);
        fLayout.addComponent(availableSelectionOptions);
        fLayout.addComponent(inputField);
        inputField.setVisible(false);
        fLayout.addComponent(confirm);
    }

    public Process getSelectedProcess() {
        return selectedProcess;
    }

    public void setSelectedProcess(Process selectedProcess) {
        this.selectedProcess = selectedProcess;
        this.close(); //TODO Exception Invocation of method windowClose in at.jku.ce.CoMPArE.CoMPArEUI$2 failed.
    }

    public ProcessChangeHistory getProcessChangeHistory() {
        return processChangeHistory;
    }

    public void setProcessChangeHistory(ProcessChangeHistory processChangeHistory) {
        this.processChangeHistory = processChangeHistory;
    }

    private class SelectDemoWindow extends Window {

        public SelectDemoWindow() {
            super("Wählen Sie einen neuen Prozess");
            this.setWidth("500px");
            this.center();

            VerticalLayout layout = new VerticalLayout();
            layout.setMargin(true);
            layout.setSpacing(true);
            final Button confirm = new Button("Fertig");
            final Label questionPrompt = new Label("Wählen Sie einen der verfügbaren Prozesse:");

            final OptionGroup availableDemoProcesses = new OptionGroup("Bitte hier auswählen:");
            for (Process p : DemoProcess.getDemoProcesses()) {
                availableDemoProcesses.addItem(p);
            }

            confirm.addClickListener(e -> {
                this.close();
                ProcessSelectorUI.this.setSelectedProcess((Process) availableDemoProcesses.getValue());
            });

            layout.addComponent(questionPrompt);
            layout.addComponent(availableDemoProcesses);
            layout.addComponent(confirm);
            setContent(layout);
        }
    }

}
