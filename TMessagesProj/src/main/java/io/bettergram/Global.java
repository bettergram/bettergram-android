package io.bettergram;

public class Global {
    public static final String[] AUTOSUB_GROUPS = new String[]{"https://t.me/bettergramapp", "https://t.me/livecoinwatchofficial", "https://t.me/bgsecuritytokens", "https://t.me/bettergramchannel", "https://t.me/join_changelly"};

    private static Global instance = null;
    public int userId;

    private Global() {
    }

    public static Global getInstance() {
        if (instance == null) {
            instance = new Global();
        }
        return instance;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
