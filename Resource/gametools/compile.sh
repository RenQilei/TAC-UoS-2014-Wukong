javac -classpath . com/botbox/util/*.java
javac -classpath . se/sics/tac/util/*.java
jar cfm gametools.jar ToolManifest.txt com/botbox/util/*.class se/sics/tac/util/*.class
