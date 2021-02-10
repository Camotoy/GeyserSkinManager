/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Floodgate
 */

package com.github.camotoy.geyserskinmanager.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SkinUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    /**
     * Get the ARGB int for a given index in some image data
     *
     * @param index Index to get
     * @param data  Image data to find in
     * @return An int representing ARGB
     */
    private static int getARGB(int index, byte[] data) {
        return (data[index + 3] & 0xFF) << 24 | (data[index] & 0xFF) << 16 |
                (data[index + 1] & 0xFF) << 8 | (data[index + 2] & 0xFF);
    }

    /**
     * Convert a byte[] to a BufferedImage
     *
     * @param imageData   The byte[] to convert
     * @param width  The width of the target image
     * @param height The height of the target image
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(byte[] imageData, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, getARGB((y * width + x) * 4, imageData));
            }
        }
        return image;
    }

    /**
     * @return the texture location on the Java format for the specified bone
     */
    private static int[] getNewTextureOffset(String bone, float scale) {
        switch (bone) {
            case "body":
                return new int[]{(int) Math.ceil(16 * scale), (int) Math.ceil(16 * scale)};
            case "head":
                return new int[]{0, 0};
            case "hat":
                return new int[]{(int) Math.ceil(32 * scale), 0};
            case "leftArm":
                return new int[]{(int) Math.ceil(32 * scale), (int) Math.ceil(48 * scale)};
            case "rightArm":
                return new int[]{(int) Math.ceil(40 * scale), (int) Math.ceil(16 * scale)};
            case "leftSleeve":
                return new int[]{(int) Math.ceil(48 * scale), (int) Math.ceil(48 * scale)};
            case "rightSleeve":
                return new int[]{(int) Math.ceil(40 * scale), (int) Math.ceil(32 * scale)};
            case "leftLeg":
                return new int[]{(int) Math.ceil(16 * scale), (int) Math.ceil(48 * scale)};
            case "rightLeg":
                return new int[]{0, (int) Math.ceil(16 * scale)};
            case "leftPants":
                return new int[]{0, (int) Math.ceil(48 * scale)};
            case "rightPants":
                return new int[]{0, (int) Math.ceil(32 * scale)};
            case "jacket":
                return new int[]{(int) Math.ceil(16 * scale), (int) Math.ceil(32 * scale)};

            default:
                return new int[]{-1, -1};
        }
    }

    private static int[] sizeToTexSize(float[] size) {
        // Round the bone size to ints
        int width = Math.round(size[0]);
        int height = Math.round(size[1]);
        int depth = Math.round(size[2]);

        // Work out the size of the texture based on the bone size
        int texWidth = (depth * 2) + (width * 2);
        int texHeight = depth + height;

        return new int[]{texWidth, texHeight};
    }

    public static BufferedImage toBufferedImage(RawSkin rawSkin) {
        BufferedImage newSkinImg = new BufferedImage(rawSkin.width, rawSkin.height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage skinImg = toBufferedImage(rawSkin.data, rawSkin.width, rawSkin.height);

        // Should move scaling to after remap based on height
        float heightScale = 64 / (float) rawSkin.height;

        // If the skin has custom geometry that we need to map
        if (rawSkin.geometry != null && !rawSkin.geometry.trim().equals("null")) {
            // Setup the geometry data ready for JSON
            JsonNode geometry;
            List<Bone> bones = new ArrayList<>();

            // Parse the geometry JSON
            try {
                geometry = OBJECT_MAPPER.readTree(rawSkin.geometry);
                String geometryName;
                if (geometry.get("format_version").textValue().equals("1.8.0")) {
                    geometryName = OBJECT_MAPPER.readTree(rawSkin.geometryName).get("geometry").get("default").textValue();
                    for (JsonNode node : geometry.get(geometryName).get("bones")) {
                        bones.add(OBJECT_MAPPER.readValue(node.traverse(), Bone.class));
                    }
                } else { // Seen with format_version 1.12.0
                    geometryName = "minecraft:geometry";
                    for (JsonNode node : geometry.get(geometryName)) {
                        for (JsonNode subNode : node.get("bones")) {
                            bones.add(OBJECT_MAPPER.readValue(subNode.traverse(), Bone.class));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Please send the following text and errors to the GeyserSkinManager developers!");
                e.printStackTrace();
                System.out.println(rawSkin.geometryName);
                System.out.println(rawSkin.geometry);
                return newSkinImg;
            }

            // Loop through each bone mapping it to the new texture
            for (Bone bone : bones) {
                if (bone.getCubes() == null || bone.getCubes().isEmpty()) {
                    continue;
                }
                // Check if we should map this bone to the new texture
                int[] newOffset = getNewTextureOffset(bone.getName(), heightScale);

                if ("leftArm".equals(bone.getName()) || "rightArm".equals(bone.getName())) {
                    int size = (int) bone.getCubes().get(0).getSize()[0];
                    if (size == 3 || size == 4) {
                        // Modify skin arm size if relevant
                        rawSkin.alex = size == 3;
                    }
                }

                if (newOffset[0] != -1) {
                    // Get the cube data and work out the texture size
                    Cube firstCube = bone.getCubes().get(0);
                    int[] texSize = sizeToTexSize(firstCube.getSize());

                    // Get the texture offset from the UVs
                    float[] tmpTexOffset = firstCube.getUV();
                    int[] texOffset = new int[]{Math.round(tmpTexOffset[0]), Math.round(tmpTexOffset[1])};

                    // Loop through the texture for that bone mapping it into the correct place for the new image
                    for (int x = 0; x < texSize[0]; x++) {
                        for (int y = 0; y < texSize[1]; y++) {
                            newSkinImg.setRGB(newOffset[0] + x, newOffset[1] + y, skinImg.getRGB(texOffset[0] + x, texOffset[1] + y));
                        }
                    }
                }
            }
        } else {
            // No geometry to convert, so we assume its a normal layout
            newSkinImg = skinImg;
        }

        // Scales the image down if bigger than 64x64
        if (rawSkin.width > 64 || rawSkin.height > 64) {
            newSkinImg = scale(newSkinImg);
        }

        return newSkinImg;
    }

    private static BufferedImage scale(BufferedImage bufferedImage) {
        BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(bufferedImage, 0, 0, 64, 64, null);
        g2.dispose();
        return resized;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Geometry {
        @JsonProperty("format_version")
        private String version;

        @JsonProperty("minecraft:geometry")
        private List<GeometryData> geometryData;

        public String getVersion() {
            return this.version;
        }

        public List<GeometryData> getGeometryData() {
            return this.geometryData;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeometryData {
        @JsonProperty("bones")
        private List<Bone> bones;

        public List<Bone> getBones() {
            return this.bones;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Bone {
        @JsonProperty("name")
        private String name;

        @JsonProperty("parent")
        private String parent;

        @JsonProperty("rotation")
        private float[] rotation;

        @JsonProperty("pivot")
        private float[] pivot;

        @JsonProperty("cubes")
        private List<Cube> cubes;

        public String getName() {
            return this.name;
        }

        public List<Cube> getCubes() {
            return this.cubes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Cube {
        @JsonProperty("inflate")
        private Float inflate;

        @JsonProperty("mirror")
        private Boolean mirror;

        @JsonProperty("origin")
        private float[] origin;

        @JsonProperty("size")
        private float[] size;

        @JsonProperty("uv")
        private float[] UV;

        public float[] getSize() {
            return this.size;
        }

        public float[] getUV() {
            return this.UV;
        }
    }
}
