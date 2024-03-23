# ArmorEquipEvent
This is a fork of the original [ArmorEquipEvent](https://github.com/Arnuh/ArmorEquipEvent) by Arnuh written for Minecraft 1.20.4
- Feel free to take out any needed parts of this plugin
- You could use it as a standalone jar or copy it into your plugin
- This plugin does not add any functionality to the event. That it up to you

## Armor Equipping

### Internal Handling
- Any blocks that open an inventory are ignored (see [InventoryHolder]())
- Internally, the plugin checks Spigot's [Tag]() class for multiple lists of block types
  - If more Tag types get added or any are missing, you can add it to the `ArmorListener` class
- The current list of Tags are:
  - `ALL_SIGNS`
  - `ALL_HANGING_SIGNS`
  - `DOORS`
  - `TRAPDOORS`
  - `BUTTONS`
  - `FENCE_GATES`
  - `BEDS`
  - `FLOWER_POTS`
  - `ANVIL`
  - `CANDLE_CAKES`

### External Handling
- Blocks that do not appear in any Tag list need to be defined individually
- Add specific blocks to `interactBlocks` in `config.yml`
```yaml
# A (probably) complete list of additional blocks for 1.20.2
interactBlocks:
  - BEACON
  - CRAFTING_TABLE
  - ENCHANTING_TABLE
  - ENDER_CHEST
  - COMPARATOR
  - REPEATER
  - DAYLIGHT_DETECTOR
  - LEVER
  - CARTOGRAPHY_TABLE
  - GRINDSTONE
  - LOOM
  - STONECUTTER
  - BELL
  - SMITHING_TABLE
  - NOTE_BLOCK
  - CAKE
```

## Dispenser Equipping
- Handles when the player equips armor via a Dispenser
- If the event is cancelled, the item is placed in their inventory

## Essentials Support `/hat`
- Essentials allows players to put additional items in their helmet slot
- For full support, you need to keep `allow-direct-hat: false` in the Essentials `config.yml`
- The plugin listens to the hat command and aliases: `hat` `ehat` `head` `ehead`

## Known Issues
- Certain block types produce unintended behavior for different reasons
- The following blocks will not fire an ArmorEquipEvent when right-clicked
  - `CAKE` and `CANDLE_CAKE` - Different actions depending on the player's hunger level. Fixing it causes cakes to be inedible when holding armor pieces
  - `CHISELED_BOOKSHELF` - The Spigot API has no way to determine the main face. Clicking on any other face will not fire the event

## Final Notes
- Updating Minecraft version(s) adds new blocks. The `Tag` lists will update, but the config file will not
- It is your job to update `config.yml` to contain any new blocks

#### Known Upcoming Blocks
- 1.20.4 (~~1.20.3~~) Adds the ability to place items in a pot. This should be checked
- 1.21 Adds a few new blocks that could cause issues. The Copper Bulb, Trial Spawner, and others should be checked

