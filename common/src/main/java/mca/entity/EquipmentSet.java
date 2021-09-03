package mca.entity;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum EquipmentSet {
    NAKED(null, null, null, null, null, null),
    LEATHER(Items.IRON_SWORD, null, null, Items.IRON_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
    IRON(Items.IRON_SWORD, Items.SHIELD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
    DIAMOND(Items.DIAMOND_SWORD, Items.SHIELD, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
    NETHERITE(Items.NETHERITE_SWORD, Items.SHIELD, Items.DIAMOND_HELMET, Items.NETHERITE_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.NETHERITE_BOOTS),
    ROYAL(Items.DIAMOND_AXE, Items.DIAMOND_AXE, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);

    EquipmentSet(Item main_hand, Item off_hand, Item head, Item chest, Item legs, Item feet) {
        this.mainHand = main_hand;
        this.getOffHand = off_hand;
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
    }

    Item mainHand;
    Item getOffHand;
    Item head;
    Item chest;
    Item legs;
    Item feet;

    public Item getMainHand() {
        return mainHand;
    }

    public Item getGetOffHand() {
        return getOffHand;
    }

    public Item getHead() {
        return head;
    }

    public Item getChest() {
        return chest;
    }

    public Item getLegs() {
        return legs;
    }

    public Item getFeet() {
        return feet;
    }
}
