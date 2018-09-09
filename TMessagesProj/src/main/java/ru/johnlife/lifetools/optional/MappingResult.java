package ru.johnlife.lifetools.optional;

public interface MappingResult {
    MappingResult SUCCESS = action -> {
        //do nothing
    };

    MappingResult FAIL = Runnable::run;

    void orElse(Runnable action);
}
