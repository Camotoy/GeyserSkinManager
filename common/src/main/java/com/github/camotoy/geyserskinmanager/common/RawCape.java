package com.github.camotoy.geyserskinmanager.common;

public class RawCape {
    public int width;
    public int height;
    public String id;
    public byte[] data;

    public RawCape(int width, int height, String id, byte[] data) {
        this.width = width;
        this.height = height;
        this.id = id;
        this.data = data;
    }
}
