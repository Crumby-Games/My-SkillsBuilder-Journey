package com.example.group56.model;

import jakarta.persistence.*;

/* Link entity between Avatar and Item. Contains instance-specific data, like whether an equippable item is equipped. */

@Entity
@Table(name = "avatar_items")
public class AvatarItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Avatar avatar;

    @ManyToOne(optional = false, fetch=FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Item item;

    boolean equipped = false;

    public AvatarItem() {}
    public AvatarItem(Avatar avatar, Item item) {
        this.avatar = avatar;
        this.item = item;
    }

    public long getId() {
        return id;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public Item getItem() {
        return item;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }
}
