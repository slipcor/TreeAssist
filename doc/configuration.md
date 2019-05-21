# Configuration File

This is the default configuation file.

    Main:
      Apply Full Tool Damage: true # false will prevent tool damage
      Auto Add To Inventory: false
      Auto Plant Dropped Saplings: false # spawned saplings will automatically turn into placed saplings
      Automatic Tree Destruction: true
      Destroy Only Blocks Above: false # true will leave tree stumps if you don't break the bottom block
      Force Break Default Radius: 10
      Force Grow Default Radius: 10
      Force Break Max Radius: 30
      Force Grow Max Radius: 30
      Ignore User Placed Blocks: false
      Initial Delay: false
      Language: en
      Sapling Replant: true
      Toggle Default: true
      Use mcMMO if Available: true
      Use Permissions: false
    Automatic Tree Destruction:
      Required Lore: ''
      When Sneaking: true
      When Not Sneaking: true
      Forced Removal: false # even if we lack the tool or did not break the bottom block (if needed) the plugin will remove the tree for cosmetic reasons
      Remove Leaves: true
      Initial Delay (seconds): 10
      Delay (ticks): 0 # delay in between block deletions, if your server is affected by lag by the plugin's insta-removal
      Tree Types: # here you can turn off the automatic destruction for single tree types
        Birch: true
        Jungle: true
        BigJungle: true
        BigSpruce: true
        Oak: true
        Spruce: true
        Brown Shroom: true
        Red Shroom: true
        Acacia: true
        Dark Oak: true
      Cooldown (seconds): 0 # a way to limit people killing the forest
    Auto Plant Dropped Saplings: # if activated (!) here are some more options for the automated sapling planting
      Chance (percent): 10
      Delay (seconds): 5
    Leaf Decay:
      Fast Leaf Decay: true
    Sapling Replant:
      Command Time Delay (Seconds): 30
      Bottom Block has to be Broken First: true # false will basically force a possibly existing bottom block to turn into a sapling
      Time to Protect Sapling (Seconds): 0
      Time to Block Sapling Growth (Seconds): 0
      Replant When Tree Burns Down: true
      Block all breaking of Saplings: false
      Delay until Sapling is replanted (seconds) (minimum 1 second): 1
      Enforce: false
      Tree Types to Replant: # here you can turn off the automatic replanting for single tree types
        Birch: true
        Jungle: true
        BigJungle: true
        Oak: true
        Spruce: true
        Brown Shroom: true
        Red Shroom: true
        Acacia: true
        Dark Oak: true
    Tools:
      Sapling Replant Require Tools: true
      Tree Destruction Require Tools: true
      Tools List:
      - DIAMOND_AXE
      - WOOD_AXE
      - GOLD_AXE
      - IRON_AXE
      - STONE_AXE
      Drop Chance:
        DIAMOND_AXE: 100
        WOOD_AXE: 100
        GOLD_AXE: 100
        IRON_AXE: 100
        STONE_AXE: 100
    Worlds:
      Enable Per World: false
      Enabled Worlds:
      - world
      - world2
    Custom Drops:
      APPLE: 0.1
      GOLDEN_APPLE: 0.0
    Placed Blocks:
      Handler Plugin Name: TreeAssist
    Modding:
      Disable Durability Fix: false
    Custom Tree Definitions:
    - - minecraft:birch_sapling
      - minecraft:birch_log
      - minecraft:birch_leaves
    # be careful formatting here. the first hyphen aligns vertically with the C of Custom and starts a definition,
    # then each next hyphen indicates each sub setting.
    # Another definition requires another double hyphen on the level of the C of Custom
    Debug: none