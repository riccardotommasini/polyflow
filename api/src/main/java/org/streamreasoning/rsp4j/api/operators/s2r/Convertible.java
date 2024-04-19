package org.streamreasoning.rsp4j.api.operators.s2r;

/**
 * This interface is applied on the 'W' type of the 'I, W, R, O' generics, which should override the methods of the interface in order to:
 * - Have a method to do some computations on data on the W format
 * - Have a method to transform the W type to an R type of the 'I, W, R, O' generics. The R type must be Iterable
 */
public interface Convertible<R extends Iterable<?>> {

    /**
     * Method used to do some computations on the W type, can be empty if no computations are needed
     */
    void compute();

    /**
     * Method used on the W type to convert it into an R type, R must be iterable
     */
    R convertToR();
}
