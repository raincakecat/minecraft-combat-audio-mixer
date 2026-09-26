package dev.combataudio.core;

import java.util.List;

public enum SoundGroup {
    EXPLOSIONS("explosions", "Explosions", List.of("entity.generic.explode")),
    TOTEMS("totems", "Totem pops", List.of("item.totem.use")),
    EATING("eating", "Eating & drinking", List.of("entity.generic.eat", "entity.generic.drink", "entity.player.burp", "item.honey_bottle.drink")),
    BLOCKS("blocks", "Stone & glass placement", List.of("block.stone.place", "block.glass.place")),
    ANCHORS("anchors", "Anchor interactions", List.of("block.respawn_anchor.charge", "block.respawn_anchor.set_spawn", "block.respawn_anchor.deplete")),
    PEARLS("pearls", "Pearl throws", List.of("entity.ender_pearl.throw")),
    MELEE("melee", "Melee attacks", List.of("entity.player.attack.crit", "entity.player.attack.knockback", "entity.player.attack.nodamage", "entity.player.attack.strong", "entity.player.attack.sweep", "entity.player.attack.weak")),
    EQUIPMENT("equipment", "Equipping armor", List.of("item.armor.equip_generic", "item.armor.equip_diamond", "item.armor.equip_netherite", "item.armor.equip_iron", "item.armor.equip_gold", "item.armor.equip_chain", "item.armor.equip_leather", "item.armor.equip_elytra", "item.armor.equip_turtle"));

    public final String key;
    public final String label;
    public final List<String> events;

    SoundGroup(String key, String label, List<String> events) {
        this.key = key;
        this.label = label;
        this.events = events;
    }

    public static SoundGroup fromKey(String key) {
        for (SoundGroup group : values()) if (group.key.equals(key)) return group;
        return null;
    }
}
