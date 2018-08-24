# COMMONING-HOUSING

Im Rahmen unseres Praktikums haben wir (Theresa Prinz und Sonja Polt) das Thema Commons untersucht und im Zuge einer Fallstudie Housing – „Selbstorganisiert wohnen, solidarisch wirtschaften“ bearbeitet. 
Die Idee war es einer Drittperson, die mit dem Thema Commoning soweit noch nie etwas zu tun hatte, aber die Grundidee interessant findet, anhand unserer Fallstudie einen kurzen Überblick und ein Grundverständnis zu geben. 
Nach einer Recherche wie wir Prozesse einer Drittperson durch Prozessausführung näher bringen könnten, stießen wir auf ein existierendes Tool des Communication Engineering Institutes, welches die Ausführung von subjektorientierten Prozessen erlaubt. Dieses Tool ist unter http://adaptivetesting.ce.jku.at/VirtualEnactment/ abrufbar und steht unter https://github.com/win-ce/VirtualEnactment zum Download bereit. Die Arbeit von Oppl [1] zur Anwendung steht unter https://zenodo.org/record/207008#.W38asi3qhhF zum Download bereit. 
Um einen Fehler der originalen Anwendung zu beheben wurde die Ordnerstruktur verändert, da bei der Ausführung der originalen Anwendung die web.xml nicht gefunden wurde. Eine weitere Exception konnte behoben werden indem im File log4j.properties die Einstellung für das Log File auf „log4j.appender.file.File=logs/VirtualEnactment.log“ geändert wurde. 
Da wir nicht die gesamte Funktionalität der Applikation benötigten, wurden einige Teile durch Kommentare ausgehängt. Das UI wurde auch etwas umorganisiert und um unsere Inhalte erweitert.
Grundsätzlich wurden von der Applikation VirtualEnactment nur die Klassen „DemoProcess“, „CoMPArEUI“ und „ProcessSelectorUI“ editiert, wobei die Klasse „ProcessSelectorUI“ im Laufe des Projektes ebenfalls ausgehängt wurde und somit die Änderungen hinfällig sind.  

## License

This software is provided under the GPL 3.0 license.

[1]:	http://www.vaadin.com
[2]:	https://zenodo.org/record/207008#.WFQGULGZOb8
[3]:	http://www.oppl.info/files/ArticulationOfWorkProcessModels.pdf
[4]:	http://www.journals.elsevier.com/information-and-management
[5]:	https://github.com/win-ce/VirtualEnactment/issues
[6]:	https://github.com/win-ce/VirtualEnactment/projects
[7]:	https://github.com/oppl# BUSEM-PR
