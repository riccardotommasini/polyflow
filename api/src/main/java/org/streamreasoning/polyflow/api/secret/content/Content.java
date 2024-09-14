package org.streamreasoning.polyflow.api.secret.content;


public interface Content<I, W, R> {

    int size();

    void add(I e);

    R coalesce();

    /**
     * used in cases where the reporting strategy depends on a state of the content (received X elements, event happened etc..).
     * @return True if the content is ready to be reported
     */
    default boolean toReport(){return false;}

}
