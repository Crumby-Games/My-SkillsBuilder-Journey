package com.example.group56.controller;

import com.example.group56.annotation.JsonCompatible;
import com.example.group56.model.*;
import com.example.group56.repo.*;
import com.example.group56.common.EntityUtils;
import com.example.group56.service.AvatarService;
import com.example.group56.service.ItemService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Mappings summary:
// /avatar                                      - displays avatar customisation template
// /api/avatar/equippable/{itemName}/equip      - equips AvatarItem in Avatar's collection with matching name
// /api/avatar/equippable/{slot}/unequip        - unequips AvatarItem in Avatar's collection in matching slot
// /api/avatar/consumable/{itemName}/use        - Use a specific consumable item
// /api/avatar/consumable/grant-random          - Gives random consumable item to logged in
// /api/avatar/character/{characterName}/select - changes Avatar's characterSpritesheet to character with matching name
@Controller
public class AvatarController {
    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private AvatarItemRepository avatarItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private SpritesheetRepository spritesheetRepository;

    @Autowired
    private AvatarService avatarService;

    // Display template where user can change equippables and character
    @GetMapping("/avatar")
    public String showAvatarCustomisationTemplate(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("characterSprites", spritesheetRepository.findBySpriteType("character"));

        /* Equippables */

        List<Map<String, Object>> equippablesData = itemService.getAllEquippablesData(user.getAvatar());

        // Group items by type (last part of their name), sort by tier and put all unobtained items at the end
        equippablesData.sort(Comparator
                .comparing((Map<String, Object> item) -> !(Boolean) item.get("owned"))
                .thenComparing(item -> (Integer) item.get("tier"))
                .thenComparing(item -> new StringBuilder((String) item.get("name")).reverse().toString())
        );
        model.addAttribute("equippablesData", equippablesData);

        // Ghost character
        model.addAttribute("templateCharacterSprite", spritesheetRepository.findById("template").orElse(null));

        // Convert AvatarItems to generic Items.
        List<Item> inventory = avatarItemRepository.findByAvatarAndItemEquipSlotIsNotNull(user.getAvatar()).stream()
                                .map<Item>(AvatarItem::getItem)
                                .toList();

        model.addAttribute("inventory", inventory);

        /* Consumables */

        List<AvatarItem> consumableItems = avatarItemRepository.findByAvatarAndItemConsumable(user.getAvatar(), true);

        // Map item names to their quantities
        Map<String, Long> consumableCounts = consumableItems.stream()
                .collect(Collectors.groupingBy(avatarItem -> avatarItem.getItem().getName(), Collectors.counting()));

        model.addAttribute("consumables", consumableCounts);


        return "avatar";
    }

    // Update database to equip AvatarItem (if in Avatar's collection) with matching name and unequip any item already equipped in the slot
    @PostMapping("/api/avatar/equippable/{itemName}/equip")
    public String equipItem(@AuthenticationPrincipal User user, @PathVariable String itemName) {
        Avatar avatar = user.getAvatar();
        Item item = itemRepository.findById(itemName).orElse(null);
        avatarItemRepository.findByAvatarAndItemAndItemEquipSlotIsNotNull(avatar, item).ifPresent(avatarService::equipAvatarItem);

        return "redirect:/avatar";
    }

    // Update database to unequip whichever AvatarItem is in this slot, if any.
    @PostMapping("/api/avatar/equippable/{slot}/unequip")
    public String unequipSlot(@AuthenticationPrincipal User user, @PathVariable String slot) {
        Avatar avatar = user.getAvatar();
        avatarItemRepository.findByAvatarAndEquippedAndItemEquipSlot(avatar,true, slot).ifPresent(avatarService::unequipAvatarItem);

        return "redirect:/avatar";
    }

    // Update database to change Avatar's characterSpritesheet to one that matches the name
    @PostMapping("/api/avatar/character/{name}/select") @Transactional
    public String selectCharacterSpritesheet(@AuthenticationPrincipal User user, @PathVariable String name) {
        Spritesheet characterSpritesheet = spritesheetRepository.findById(name).orElse(null);
        if(characterSpritesheet != null && user != null) {
            Avatar avatar = user.getAvatar();
            if(avatar != null) {
                avatar.setCharacterSpritesheet(characterSpritesheet);
                avatarRepository.save(avatar);
            }
        }
        return "redirect:/avatar";
    }

    // Returns a JSON-friendly mapping of the components of an avatar to their spritesheets
    @GetMapping("/api/avatar/spritesheets")
    @ResponseBody @JsonCompatible
    public Map<String, Object> getAvatarSpritesData(@AuthenticationPrincipal User user) {
        // Must get avatar via repository and not getAvatar(), otherwise you get outdated data
        Avatar avatar = avatarRepository.findByUser(user).orElse(null);
        if (avatar == null) return null;
        
        Spritesheet characterSpritesheet;
        characterSpritesheet = avatar.getCharacterSpritesheet();
        Map<String, Object> componentData = new HashMap<>();
        for (Map.Entry<String, AvatarItem> mapping : avatar.getEquippedItems().entrySet()) {
            componentData.put(mapping.getKey(), EntityUtils.getFields(mapping.getValue().getItem().getSpritesheet()));
        }


        if (characterSpritesheet == null) return null;

        componentData.put("character", EntityUtils.getFields(characterSpritesheet));
        return componentData;
    }
}
