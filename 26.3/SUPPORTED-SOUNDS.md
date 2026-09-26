# Supported sounds: Minecraft Java 26.3

Mapping version: `1.21.11-v1` (unchanged since the 1.21.11 release, so existing presets import without warnings). Every entry has the `minecraft:` namespace. All 27 IDs were checked against the running game's sound-event registry.

## Explosions

- `entity.generic.explode`

This is shared across explosion sources. It does not distinguish crystals, anchors, TNT, or any other source using the event.

## Totem pops

- `item.totem.use`

## Eating and drinking

- `entity.generic.eat`
- `entity.generic.drink`
- `entity.player.burp`
- `item.honey_bottle.drink`

## Stone and glass placement

- `block.stone.place`
- `block.glass.place`

This includes other blocks that share those material sounds; it is not an obsidian-only selector.

## Anchor interactions

- `block.respawn_anchor.charge`
- `block.respawn_anchor.set_spawn`
- `block.respawn_anchor.deplete`

The anchor's explosion belongs to the shared Explosions group.

## Pearl throws

- `entity.ender_pearl.throw`

This does not classify arbitrary teleport events as pearl sounds.

## Melee attacks

- `entity.player.attack.crit`
- `entity.player.attack.knockback`
- `entity.player.attack.nodamage`
- `entity.player.attack.strong`
- `entity.player.attack.sweep`
- `entity.player.attack.weak`

## Equipping armor

- `item.armor.equip_generic`
- `item.armor.equip_diamond`
- `item.armor.equip_netherite`
- `item.armor.equip_iron`
- `item.armor.equip_gold`
- `item.armor.equip_chain`
- `item.armor.equip_leather`
- `item.armor.equip_elytra`
- `item.armor.equip_turtle`

## Preview behavior

Previews use the same event-based gain routing as game audio. Explosion/placement/anchor samples use the Blocks category, pearl throws use Neutral (Friendly Mobs), and the remaining samples use Players. They are local relative samples with no spatial attenuation, played at a reduced source level. A real event keeps its original category and spatial properties.

Changing a resource pack's underlying sample for a listed event does not change the group mapping. A newly introduced event ID is unaffected unless explicitly added to a future mapping.
