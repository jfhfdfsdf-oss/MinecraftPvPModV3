# PvPMod – Validierungsbericht

Stand: 2026-11-03

## Geprüfte Zielversionen

- Paper API: `io.papermc.paper:paper-api:26.2.build.135-stable`
- `plugin.yml` API-Version: `26.2`
- JDK/Bytecode: Java 25 (`toolchain` und `--release 25`)
- Gradle-Bootstrap: 9.2.1

Die konkrete API-Version ist in den Paper-26.2-Javadocs als `26.2.build.135-stable` ausgewiesen. Paper dokumentiert Java 25 als Mindestversion für 26.1+. Gradle 9.1+ unterstützt Java 25; die im Projekt hinterlegte Gradle-Version 9.2.1 liegt darüber.

## Änderungen in dieser Überarbeitung

- `CrafterCraftEvent` auf das tatsächliche Package `org.bukkit.event.block` korrigiert.
- Veraltetes `PlayerLoginEvent` durch `PlayerConnectionValidateLoginEvent` ersetzt; beide Login-Phasen werden über die Paper-Connection-Profile ausgewertet.
- Mace-Besitz wird vor der Item-Ausgabe persistent geschrieben. Bei Schreibfehler wird der Claim zurückgerollt und das Item nicht ausgegeben.
- Mace-Reset wird nur dann als erfolgreich gemeldet, wenn der freigegebene Zustand dauerhaft geschrieben wurde. Bei Fehler bleibt die Sperre im Arbeitsspeicher fail-closed. Beschädigte State-Dateien erzeugen zusätzlich einen persistenten Sidecar-Lock.
- Mace-Transfer innerhalb des eigenen Spielerinventars bleibt möglich; Containertransfer, Shift-Transfer, Creative-Clone, Drop, Hopper-Pickup/-Move, Dispense und Crafter werden blockiert.
- Bereinigung von Mace-Kopien in geladenen Containern/Item-Entities, beim Chunk-Load, beim Join und beim Admin-Reset ergänzt.
- Doppelte Mace-Kopien im Spielerinventar werden beim Join entfernt; Enderchest-Kopien werden immer entfernt.
- Levelabhängige Multiplikatoren werden ebenfalls auf -10 bis 30 begrenzt.
- Herztabelle für alle 41 Level in JUnit-Tests abgedeckt; Tests für Multiplikatoren, Grenzwerte und Integer-Overflow ergänzt.

## Tatsächlich ausgeführte Prüfungen

| Prüfung | Ergebnis |
|---|---|
| Java-Kompilierung von `PlayerData` und `LevelManager` mit vorhandenem Java 21 | Erfolgreich |
| Ausgeführter Smoke-Test für alle 41 Herzwerte, Multiplikatoren, Clamping und Integer-Overflow | Erfolgreich |
| YAML-Parsing von `plugin.yml` und `config.yml` | Erfolgreich |
| Gradle `clean test build` | Nicht ausführbar: Download von Gradle 9.2.1 scheiterte an DNS/Netzwerk (`curl: (6) Could not resolve host: services.gradle.org`; Exitcode 6) |
| Vollständige Kompilierung gegen Paper API 26.2 | Nicht ausgeführt |
| JUnit über Gradle | Nicht ausgeführt |
| Paper-Server-Integrationstest | Nicht ausgeführt |

## Noch offene Freigabepunkte

1. Auf einem Rechner mit JDK 25 und Netzwerkzugriff `./gradlew clean test build` ausführen.
2. Alle Java-Quellen vollständig gegen `paper-api:26.2.build.135-stable` kompilieren und eventuelle Compilerfehler beheben.
3. Das Plugin auf einem separaten Paper-26.2-Testserver starten und die GUI-, Ban-, Whitelist-, Crafter-, Container- und Neustartabläufe testen.
4. Den Mace-Schadensdeckel mit Rüstung/Protection, Absorption, Resistance, Hurt-Frames, Totem und anderen installierten Plugins prüfen. Die aktuelle Implementierung bricht das Schadensevent ab und zieht die begrenzte Gesundheit direkt ab; damit werden auch Absorption und Teile der Vanilla-Schadensverarbeitung umgangen.

**Status:** Quellcode überarbeitet und Teilprüfungen bestanden. Ein erfolgreicher vollständiger Gradle-Build, eine JAR und die Produktionsreife sind ausdrücklich noch nicht bestätigt.

## Offizielle Referenzen zur Zielkonfiguration

- Paper Java-Anforderungen: https://docs.papermc.io/paper/getting-started/
- Paper Gradle-Projektsetup und Versionsschema: https://docs.papermc.io/paper/dev/project-setup/
- Paper 26.2 API-Javadocs (Build 135 Stable): https://jd.papermc.io/paper/26.2/
- Gradle 9.2.1 Release Notes: https://docs.gradle.org/9.2.1/release-notes.html
- Gradle Java-Kompatibilität: https://docs.gradle.org/current/userguide/compatibility.html
