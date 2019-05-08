# Realizing dataflow tainting via non-intrusive Java Bytecode instrumentation

Im Anhang ist eine (simple) Beispiel Java Datei (TestString.java) welche ein paar String Operationen ausführt, diese als Attribut einer Klasse speichert und über Getter/Setter nutzt.

Der zugehörige Bytecode ist in TestString.decompiled.txt gegeben. (``javac TestString.java; javap -l -v -p -s TestString.class``)

Wenn man das Tool ausführt (``gradle jar; cd build/libs; java -jar asm_test-0.0.1-SNAPSHOT.jar -f ~/Projects/TU_BS/java_bytecode_rewriting/testing/TestString.class -o ~/Projects/TU_BS/java_bytecode_rewriting/testing/temp/TestString.class``) schreibt es den Bytecode der Quelldatei (Pfad nach "-f") um und speichert diesen in in einer Zieldatei (Pfad nach "-o")

Wichtig ist das im Zielverzeichnis die compilierten Formen von IASString.java und IASStringBuilder.java (aus dem src Folder) liegen (das Tool kann dies mit den "-c" Flag auch direkt prüfen)
Die Beispielapplikation lässt sich dann regulär mit ``java TestString`` ausführen (Ausgabe "hellohello")

Wenn man sich die Zieldatei mit ``javap -l -v -p -s TestString.class`` anschaut (Ausgabe als TestString.rewritten.decompiled.txt im Anhang) sieht man, folgende Änderungen:
- Funktionsparameter, Rückgabewerte und Attribute wurden von ``java/lang/String`` zu ``IASString`` umgeschrieben.
- Die ``main`` Methode nimmt ja regulär ein ``String`` Array als Parameter. Um diese Parameter zu unserem String Typ zu ändern, wird aller Code der alten ``main`` Methode in ``$main`` verschoben und die normale ``main`` Methode wandelt nur das Parameter Array in ein ``IASString`` Array um und ruft damit ``$main`` auf.
- ``String`` Konstanten (werden regulär mit ``ldc`` aus dem Constant Pool geladen) werden nun in eine ``IASString`` Instanz umgewandelt.
- ``String`` Methoden (z.B. ``equals`` oder ``hashCode``) werden auf Instanzmethoden von ``IASString`` umgeschrieben. (Dies tritt in der Beispieldatei nicht auf)
- Ausgabemethoden werden (``System.out.println`` im Quellprogramm) werden durch Proxymethoden ersetzt die ``IASString`` Instanzen nehmen und diese intern umwandeln. Diese sind zur Zeit noch handgeschrieben, aber das lässt sich -- für einfache Fälle -- mit etwas Zeit auch gut automatisieren.
- Aufrufe zu ``makeConcatWithConstants`` (String Konkatenation im Java Quellcode) werden in der Beispieldatei zu ``IASString.concat`` ersetzt. Das ist ein Hack um das Beispiel ans laufen zu kriegen. Mit JDK9 wurde geändert wie String Konkatenation behandelt wird (cf [1] und [2]). Dies entsprechend im Bytecode um zuschreiben ist etwas aufwendiger, aber sollte auf jeden Fall lösbar sein.

Die Namen sind natürlich nur temporär, aber alles was in packages liegt macht das testen etwas aufwendiger.

[1]: http://openjdk.java.net/jeps/280
[2]: http://cr.openjdk.java.net/~shade/8085796/notes.txt

