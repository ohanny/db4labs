package fr.icodem.db4labs.dbtools.service;

@FunctionalInterface
public interface DataConsumer<T> {
    void accept(T t) throws Exception;
}
