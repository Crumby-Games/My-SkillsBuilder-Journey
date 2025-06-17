package com.example.group56.model;

import jakarta.persistence.*;

/* Generic consumable and equippable information, and a link to its generic spritesheet if relevant. If the equipSlot is null, then it is not equippable. */

@Entity
@Table(name="items")
public class Item {
    @Id
    private String name;

    @ManyToOne
    private Spritesheet spritesheet;

    private boolean consumable;

    private String equipSlot;

    @Lob
    private String tooltip;

    // For sorting equippables
    private int tier;

    public Item() {}
    public Item(String name, Spritesheet spritesheet, boolean consumable, String tooltip, String equipSlot, int tier) {
        this.name = name;
        this.spritesheet = spritesheet;
        this.consumable = consumable;
        this.tooltip = tooltip;
        this.equipSlot = equipSlot;
        this.tier = tier;
    }
    
    public Item(String name, Spritesheet spritesheet, boolean consumable, String tooltip) {
        this(name,spritesheet,consumable,tooltip,null,1);
    }


    public String getName() {
        return name;
    }

    public Spritesheet getSpritesheet() {
        return spritesheet;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public String getEquipSlot() { return equipSlot; }

    public String getTooltip() { return tooltip; }

    public int getTier() { return tier; }

    public boolean isEquippable() { return equipSlot != null; }

}
