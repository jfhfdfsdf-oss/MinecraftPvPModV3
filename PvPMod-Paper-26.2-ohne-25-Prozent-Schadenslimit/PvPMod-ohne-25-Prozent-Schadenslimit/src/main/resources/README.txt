PvPMod – Minecraft 26.2 / Paper

Voraussetzungen:
- Paper 26.2 Stable
- Java 25 oder neuer
- Internetzugang beim ersten Gradle-Aufruf (Paper-API und Gradle-Distribution)

Build:
  ./gradlew clean test build

Installation:
1. Baue das Projekt mit Java 25.
2. Kopiere build/libs/PvPMod-1.0.0.jar in den Paper-Ordner plugins/.
3. Starte den Server neu.
4. Konfiguriere permissions bei Bedarf (pvpmod.admin).

Spielerbefehle:
  /stats

Admin:
  /pvpmod reload
  /pvpmod setlevel <spieler> <level>
  /pvpmod addlevel <spieler> <anzahl>
  /pvpmod removelevel <spieler> <anzahl>
  /pvpmod stats <spieler>
  /pvpmod unban <spieler>
  /pvpmod reset <spieler>
  /pvpmod mace reset
  /pvpmod mace give <spieler>

Daten liegen in plugins/PvPMod/players/<uuid>.yml und state.yml.

Wichtige technische Hinweise:
- Plugin ist für API 26.2 gebaut; keine Client-Mod und keine Drittanbieter-Plugins.
- Der Mace-Schadensdeckel wird bei MACE_SMASH durch serverseitige direkte Gesundheitsanpassung erzwungen, damit Rüstung/Protection den Grenzwert nicht verändern. Das umgeht für diesen Spezialtreffer auch Absorption und normale Schadensmodifikatoren.
- Container-/Drop-Schutz wird über Bukkit/Paper-Inventar- und Item-Events durchgesetzt. Mods oder Plugins, die Inventare direkt manipulieren, liegen außerhalb der Kontrolle dieses Plugins.
- Automatische Crafter werden über CrafterCraftEvent blockiert, sobald der einzigartige Mace vergeben ist.
- Bitte auf einem Testserver prüfen und vor Installation Backups erstellen. Ein Build kann nur mit lokal installiertem Java 25 und Netzwerkzugriff durchgeführt werden.
