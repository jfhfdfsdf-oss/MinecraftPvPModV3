# PvPMod – Paper 26.2

Eigenständiges serverseitiges PvP-Plugin für Paper 26.2. Kein Client-Mod und keine Fabric-/Forge-Abhängigkeit.

## Voraussetzungen

- Minecraft/Paper **26.2 Stable**
- **JDK 25** (Paper 26.2 benötigt Java 25; der Compiler verwendet Toolchain/Release 25)
- Netzwerkzugriff beim ersten Build (Gradle-Distribution und Paper-API)

## Build

Linux/macOS:

```bash
./gradlew clean test build
```

Windows:

```bat
gradlew.bat clean test build
```

Die Bootstrap-Skripte `gradlew` / `gradlew.bat` laden Gradle 9.2.1 bei Bedarf herunter. Das Archiv enthält bewusst kein konventionelles `gradle-wrapper.jar`; die Skripte starten die heruntergeladene Gradle-Distribution direkt. Das Plugin-JAR liegt anschließend unter `build/libs/PvPMod-1.0.0.jar`.

## Installation

1. Paper 26.2 Stable (`paper-api:26.2.build.135-stable`) mit Java 25 starten.
2. `PvPMod-1.0.0.jar` in `plugins/` kopieren.
3. Server neu starten.
4. Optional `plugins/PvPMod/config.yml` anpassen.

## Befehle

| Befehl | Funktion |
|---|---|
| `/stats` | Eigenes Statistik-GUI |
| `/pvpmod reload` | Konfiguration neu laden |
| `/pvpmod setlevel <spieler> <level>` | Level setzen (-10 bis 30) |
| `/pvpmod addlevel <spieler> <anzahl>` | Level erhöhen |
| `/pvpmod removelevel <spieler> <anzahl>` | Level senken |
| `/pvpmod stats <spieler>` | Statistik-GUI (online) oder Textstatistik (offline) |
| `/pvpmod unban <spieler>` | Plugin-Bann aufheben, Level 0, Whitelist wiederherstellen |
| `/pvpmod reset <spieler>` | Level und Statistiken zurücksetzen |
| `/pvpmod mace reset` | Einzigartigen Mace entfernen und Crafting freigeben |
| `/pvpmod mace give <spieler>` | Einzigartigen Mace vergeben (Spieler muss online sein) |

Admin-Berechtigung: `pvpmod.admin` (standardmäßig OP).

## Daten

- `plugins/PvPMod/players/<uuid>.yml`: Level, Kills, Tode, Bannstatus, Bannzeitpunkt und Whiteliststatus.
- `plugins/PvPMod/state.yml`: Mace-Besitzer und globale Crafting-Sperre; `mace-corrupt.lock` hält bei beschädigtem State fail-closed bis zum manuellen Reset.

Spielerdaten werden pro UUID in YAML-Dateien gespeichert. Schreibvorgänge verwenden eine temporäre Datei und versuchen atomisches Ersetzen. Permanente Banns verwenden die Paper-ProfileBanList (UUID/PlayerProfile), nicht die nicht mehr unterstützten namensbasierten `NAME`-Banns.

## Implementierte Funktionen

- PvP-Level -10 bis 30; PvP-Kills und PvP-Tode; Bann bei erneutem PvP-Tod auf -10.
- Angriffsschaden-, XP- und Schadensreduktionsmultiplikatoren.
- Maximale Herzen gemäß Tabelle im Projektprompt, inklusive Level-20-Bonus und 0,5 Herzen auf -10.
- `/stats`-GUI mit periodischer Aktualisierung.
- Wiedergeburts-Kristall und Seelenbefreiung mit Rezepten.
- Gebanntenliste mit Spieler-Köpfen, Seitenwechsel und Sicherheitsabfrage.
- Persistenter Mace-Besitzer und Crafting-Sperre; Player-Crafting und Crafter werden behandelt.
- Mace-Drop-, Hopper-, Pickup- und Inventar-Transfer-Schutz; Density-Entfernung bei den unterstützten Verzauberungswegen.
- Admin-Befehle und JUnit-Tests für die Levelmathematik.

## Technische Grenzen / vor Produktiveinsatz testen

- In dieser Ausführungsumgebung sind nur Java 21 und kein installiertes Gradle vorhanden; außerdem ist der Netzwerkzugriff auf Maven/Paper nicht verfügbar. **Ein Gradle-Build oder Serverintegrationstest wurde hier nicht ausgeführt.** Bitte lokal mit Java 25 bauen und auf einem Testserver prüfen.
- Der besondere Mace hat keine künstliche 25-%-Schadensbegrenzung mehr. Er verursacht den vollständigen, durch die normale Kampfberechnung (einschließlich des Angriffs-Levelmultiplikators) ermittelten Schaden. Das bisherige besondere Verhalten, Rüstungs- und Magie-Schadensminderung zu neutralisieren, bleibt bestehen. Der normale Schadensevent bleibt aktiv, damit Kill-Zuordnung, Totems, Hurt-Frames, Knockback und Todesverarbeitung erhalten bleiben.
- Der Mace-Schutz deckt normale Paper/Bukkit-Inventar-, Drop-, Pickup-, Hopper- und Crafter-Events ab. Die Inventarlogik erlaubt das Verschieben innerhalb des eigenen Spielerinventars, blockiert aber Containertransfers. Bei Join und Reset werden stale Kopien aus Spielerinventaren und geladenen Containerinventaren entfernt; zusätzlich werden geladene und neu geladene Chunks bereinigt. Andere Plugins, die Inventare direkt per API verändern, oder externe Servermodifikationen können diese Regeln umgehen.
- Die automatische Mace-Vergabe erfolgt beim ersten erfolgreichen Spieler-Crafting. Automatische Crafter werden ab gesetzter globaler Sperre blockiert. Ein automatischer Crafter kann vor diesem Zeitpunkt noch einen gewöhnlichen Vanilla-Mace herstellen.
- Dichte-Verzauberungen werden beim Enchant-Event entfernt und beim Anvil-Ergebnis bereinigt; bereits extern durch andere Plugins manipulierte Items sollten zusätzlich vor dem Livebetrieb geprüft werden.
- Die XP-Multiplikation verwendet `PlayerExpChangeEvent`, damit XP-Orbs und normale Bukkit/Paper-XP-Änderungen nicht doppelt verarbeitet werden. Spezielle Drittanbieter-XP-Systeme sind nicht Bestandteil der Spezifikation.
- Vor Updates von Paper 26.2 und vor Produktivbetrieb vollständige Backups der Welten und `plugins/PvPMod/` erstellen.
