package net.slipcor.treeassist.configs;

public interface ConfigEntry {
    /**
     * @return the comment, can be null
     */
    String getComment();

    /**
     * @return the full config node path
     */
    String getNode();

    /**
     * @return the config node content
     */
    Object getValue();

    /**
     * @return the type of content it is
     */
    Type getType();

    enum Type {
        COMMENT,
        STRING,
        BOOLEAN,
        INT,
        DOUBLE,
        LIST,
        MAP;
    }
}
