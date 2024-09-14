package org.streamreasoning.polyflow.api.stream.metadata;

public interface SchemaEntry {

    String getID();

    String getTypeName();

    int getIndex();

    int getType();

    boolean canNull();

}
