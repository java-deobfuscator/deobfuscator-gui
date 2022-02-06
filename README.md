# deobfuscator-gui [![downloads](https://img.shields.io/github/downloads/java-deobfuscator/deobfuscator-gui/total.svg)](https://github.com/java-deobfuscator/deobfuscator-gui/releases/latest) 

A GUI for a the popular [java-deobfuscator](https://github.com/java-deobfuscator/deobfuscator).

## What is Deobfuscator-GUI?
Deobfuscator-GUI is a GUI for the command line deobfuscator. User interfaces are more intuitive to the average user, allowing more people to use the tool without needing to concern themselves with syntax or configuration files.
Note that as of version 3.0 very old versions of deobfuscator will no longer be supported.

## How to Use 
A java installation with javafx is required. [Azul Zulu JDK+FX/JRE+FX builds](https://www.azul.com/downloads/?package=jdk-fx) are the easiest option to get these.

1. Download the deobfuscator.jar from https://github.com/java-deobfuscator/deobfuscator. Place it in the same folder as the GUI you will be downloading to avoid selecting the JAR when you open the GUI.
2. Download or build the GUI:
    * Download: [releases](https://github.com/java-deobfuscator/deobfuscator-gui/releases/latest)
    * Build: Clone the repository then run `mvn package`
3. Run the GUI:
    * Specify an input and output file, and then add the required libraries and select your transformers.

Some transformers have their own configuration, right click a selected transformer to edit its config.

## Screenshots

![swing](swing.png)
