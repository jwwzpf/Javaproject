package de.itdesign.application.enums;

/**
 * This enum contains all the node name from the XML files.
 */
public enum XMLNode {
    CITY("city"),
    OPERATION("operation"),
    NAME("name"),
    POPULATION("population"),
    AREA("area"),
    TYPE("type"),
    FUNCTION("func"),
    ATTRIBUTE("attrib"),
    FILTER("filter"),
    RESULTS("results"),
    RESULT("result"),
    ;

    private final String nodeName;

    XMLNode(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeName() {
        return this.nodeName;
    }
}
