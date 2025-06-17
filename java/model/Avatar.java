package com.example.group56.model;

import jakarta.persistence.*;

import java.util.*;

/* An abstraction of all character-related information of a user. Equippables, consumables, character spritesheets, etc. */

@Entity
@Table(name = "avatars")
public class Avatar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch=FetchType.EAGER)
    private Spritesheet characterSpritesheet;

    // Includes equippables
    @OneToMany(mappedBy = "avatar", fetch=FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvatarItem> ownedItems;

    public Avatar() {}

    public Avatar(User user) {
        this.user = user;
    }

    public long getId() { return id; }
    public User getUser() { return user; }
    public Spritesheet getCharacterSpritesheet() { return characterSpritesheet; }
    public void setCharacterSpritesheet(Spritesheet characterSpritesheet) { this.characterSpritesheet = characterSpritesheet; }
    public List<AvatarItem>  getOwnedItems() { return ownedItems; }
    public Map<String, AvatarItem> getEquippedItems() {
        Map<String, AvatarItem> equippedItems = new HashMap<>();
        for (AvatarItem ownedItem : ownedItems) {
            if (ownedItem.isEquipped()) equippedItems.put(ownedItem.getItem().getEquipSlot(), ownedItem);
        }
        return equippedItems;
    }
}
