package com.example.group56.service;

import com.example.group56.annotation.JsonCompatible;
import com.example.group56.common.EntityUtils;
import com.example.group56.model.Avatar;
import com.example.group56.model.AvatarItem;
import com.example.group56.model.Item;
import com.example.group56.repo.AvatarItemRepository;
import com.example.group56.repo.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {
    @Autowired
    private AvatarItemRepository avatarItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    // Returns all JSON-friendly item data of all equippables in database
    @JsonCompatible
    public List<Map<String, Object>> getAllEquippablesData() {
        List<Map<String, Object>> equippablesData = new ArrayList<>();
        List<Item> equippables = itemRepository.findByEquipSlotIsNotNull();
        for (Item item : equippables) {
            Map<String, Object> itemData = EntityUtils.getFields(item);
            itemData.put("spritesheet", EntityUtils.getFields(item.getSpritesheet()));
            equippablesData.add(itemData);
        }
        return equippablesData;
    }

    // Returns all JSON-friendly item data of all equippables in database in addition to whether each item is owned and equipped.
    @JsonCompatible
    public List<Map<String, Object>> getAllEquippablesData(Avatar avatarContext) {
        List<Map<String, Object>> equippablesData = new ArrayList<>();
        List<Item> equippables = itemRepository.findByEquipSlotIsNotNull();
        for (Item item : equippables) {
            Map<String, Object> itemData = EntityUtils.getFields(item);
            itemData.put("spritesheet", EntityUtils.getFields(item.getSpritesheet()));

            AvatarItem avatarItem = avatarItemRepository.findByAvatarAndItemAndItemEquipSlotIsNotNull(avatarContext,item).orElse(null);
            if (avatarItem == null) {
                itemData.put("owned", false);
                itemData.put("equipped",false);
            } else {
                itemData.put("owned", true);
                itemData.put("equipped",avatarItem.isEquipped());
            }
            equippablesData.add(itemData);
        }
        return equippablesData;

    }
}
