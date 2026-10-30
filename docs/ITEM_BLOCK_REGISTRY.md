# Custom Item & Block Registry

## Overview
The **Neutron Tools** mod now supports registration of custom items and blocks via simple JSON files placed in the configuration folder. No code changes are required – just add JSON definitions and restart or reload the game.

---

## Directory Structure
```
config/
└─ neutrontools/
   └─ registry/
      ├─ blocks/   # JSON files for custom blocks
      └─ items/    # JSON files for custom items
```
- The `registry/blocks` folder holds block definitions.
- The `registry/items` folder holds item definitions.
- The mod will automatically create these directories on first run, but you can also create them manually.

---

## Block JSON Format
Only a small subset of block properties is currently supported. You can extend the functionality by editing `CustomRegistry.java`.

| Field          | Type   | Required | Description |
|----------------|--------|----------|-------------|
| `name`         | string | **yes**  | Registry name without namespace (e.g., `my_custom_block`). Only lowercase alphanumerics, `/`, `_`, `.` and `-` are allowed. |
| `hardness`     | number | no       | How hard the block is. Defaults to `0` if omitted. |
| `resistance`   | number | no       | Explosion resistance. If omitted, the value of `hardness` is used. |

### Example `my_custom_block.json`
```json
{
  "name": "my_custom_block",
  "hardness": 1.5,
  "resistance": 6.0
}
```
*If `hardness` is omitted, the block will have the default properties of a plain block.*

---

## Item JSON Format
Only stack size is currently configurable.

| Field            | Type   | Required | Description |
|------------------|--------|----------|-------------|
| `name`           | string | **yes**  | Registry name without namespace (e.g., `my_custom_item`). |
| `max_stack_size`| integer| no       | Maximum stack size (1‑64). Defaults to `64`. |

### Example `my_custom_item.json`
```json
{
  "name": "my_custom_item",
  "max_stack_size": 32
}
```

---

## How It Works
1. **Placement** – Put your JSON files in the appropriate folder (`blocks` or `items`).
2. **Loading** – During mod initialization, `CustomRegistry.registerCustomObjects` reads all files and registers the corresponding `Block` or `Item` objects with NeoForge.
3. **BlockItems** – For every custom block that does **not** have a matching item JSON, a default `BlockItem` is automatically created so the block can appear in inventories and the creative tab.
4. **Reload** – Changes take effect after restarting the game. If you have a datapack reload command (`/reload`), the new items/blocks will also be loaded.

---

## Tips & Caveats
- **Naming** – Registry names must be unique across the game. The mod sanitizes the name, stripping any characters outside `[a-z0-9_/.-]`.
- **Extending** – To support more block properties (e.g., material, sound, loot table), edit `CustomRegistry.registerBlocks` and add the appropriate JSON fields.
- **Resource/Datapack Integration** – After adding a block or item, you may want to provide textures, models, and language entries via standard Minecraft resource packs or datapacks located in the usual `resourcepacks`/`datapacks` directories.
- **Debugging** – The mod logs registration messages to the console (`NeutronTools.LOG`). Check the log for any warnings about missing names or JSON parsing errors.

---

## Quick Start
```bash
# Create the folder structure (if not already present)
mkdir -p config/neutrontools/registry/blocks
mkdir -p config/neutrontools/registry/items

# Example block definition
cat > config/neutrontools/registry/blocks/example_block.json <<EOF
{
  "name": "example_block",
  "hardness": 2.0,
  "resistance": 5.0
}
EOF

# Example item definition
cat > config/neutrontools/registry/items/example_item.json <<EOF
{
  "name": "example_item",
  "max_stack_size": 16
}
EOF

# Launch Minecraft – the new block and item will appear in the creative inventory.
```

---

*Enjoy creating custom content with Neutron Tools!*