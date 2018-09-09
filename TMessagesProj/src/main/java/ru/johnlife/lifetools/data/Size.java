package ru.johnlife.lifetools.data;

public final class Size {
    private final int w;
    private final int h;
    private final double ratio;

    public Size(int width, int height) {
        this.w = width;
        this.h = height;
        this.ratio = ((double)w)/h;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public double getRatio() {
        return ratio;
    }
}
