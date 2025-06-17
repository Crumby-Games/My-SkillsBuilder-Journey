package com.example.group56.service;

import com.example.group56.model.*;
import com.example.group56.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvatarService {
    @Autowired
    private SpritesheetRepository spritesheetRepository;

    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private AvatarItemRepository avatarItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    // Creates new AvatarItem in database to create link
    @Transactional
    public AvatarItem giveItemToAvatar(Item item, Avatar avatar) {
        return avatarItemRepository.save(new AvatarItem(avatar, item));
    }

    // Equip an AvatarItem for its associated Avatar and unequip any current AvatarItem in that slot.
    @Transactional
    public void equipAvatarItem(AvatarItem avatarItem) {
        String slot = avatarItem.getItem().getEquipSlot();
        if (slot == null) return;
        Avatar avatar = avatarRepository.findById(avatarItem.getAvatar().getId()).orElse(null);
        if (avatar == null) return;

        if(avatar.getEquippedItems().containsKey(slot)) {
            unequipAvatarItem(avatar.getEquippedItems().get(slot));
        }

        avatarItem.setEquipped(true);
        avatarItemRepository.save(avatarItem);
        avatarRepository.save(avatar);
    }

    // Unequip an AvatarItem for its associated avatar.
    @Transactional
    public void unequipAvatarItem(AvatarItem avatarItem) {
        avatarItem.setEquipped(false);
        Avatar avatar = avatarItemRepository.save(avatarItem).getAvatar();
        avatarRepository.save(avatar);
    }

    // Creates an avatar with no items and with a template spritesheet.
    public void saveDefaultAvatar(User user) {
        Avatar avatar = new Avatar(user);
        spritesheetRepository.findById("template").ifPresent(avatar::setCharacterSpritesheet);
        user.setAvatar(avatar);
        avatarRepository.save(avatar);
        userRepository.save(user);
    }
}
