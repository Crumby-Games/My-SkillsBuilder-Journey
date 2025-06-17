package com.example.group56.model;

import jakarta.persistence.*;

/* Contains avatar display information for equippable items and characters. */

@Entity
@Table(name="spritesheets")
public class Spritesheet {
    private static final int DEFAULT_FRAME_WIDTH = 26;
    private static final int DEFAULT_FRAME_HEIGHT = 22;
    private static final int DEFAULT_PIXEL_SCALE = 8;

    @Id
    private String name;

    @Column(nullable = false, unique = true)
    private String imagePath;

    private String spriteType;

    private final int frameWidth;
    private final int frameHeight;
    public Spritesheet() {}

    public Spritesheet(String type, String name) {
        this(type,name,DEFAULT_FRAME_WIDTH * DEFAULT_PIXEL_SCALE, DEFAULT_FRAME_HEIGHT * DEFAULT_PIXEL_SCALE);
    }

    public Spritesheet(String type, String name, int frameWidth, int frameHeight) {
        this.name = name;
        this.spriteType = type;
        this.imagePath = "/images/" + type + "/" + name.replace(' ', '-') + ".png";
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public String getName() { return name; }
    public String getImagePath() { return imagePath; }
    public String getSpriteType() { return spriteType; }
    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
}

