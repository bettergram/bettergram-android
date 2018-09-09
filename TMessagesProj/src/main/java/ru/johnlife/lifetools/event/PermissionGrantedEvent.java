package ru.johnlife.lifetools.event;

import java.util.List;

/**
 * Created by Yan Yurkin
 * 18 November 2017
 */

public class PermissionGrantedEvent {
    private List<String> granted;

    public PermissionGrantedEvent(List<String> granted) {
        this.granted = granted;
    }

    public boolean contains(String permission) {
        return granted.contains(permission);
    }
}
