package ru.johnlife.lifetools.event;

/**
 * Created by Yan Yurkin
 * 18 November 2017
 */

public class MissingPermissionsEvent {
    private String[] permissions;

    public MissingPermissionsEvent(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getPermissions() {
        return permissions;
    }
}
