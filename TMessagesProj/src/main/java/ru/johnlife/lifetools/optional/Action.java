package ru.johnlife.lifetools.optional;

public interface Action<T> {
    void applyTo(T item);
}
