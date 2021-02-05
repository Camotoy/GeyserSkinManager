package com.github.camotoy.geyserskinmanager.common;

public class RawCape {
    public int width;
    public int height;
    public byte[] data;

    public RawCape(int width, int height, byte[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }
}
