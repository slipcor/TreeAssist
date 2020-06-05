# Main Configuration File

This is the default main configuration file.

    # === [ General Settings ] ===
    General:
      # Language file to load. Does not need YML extension!
      Language: lang_en
      Toggle Default: true
      Use Permissions: false
    # === [ Command Settings ] ===
    Commands:
      Force Break:
        Default Radius: 10
        Max Radius: 30
      Force Grow:
        Default Radius: 10
        Max Radius: 30
      No Replant:
        Cooldown Time: 30
    # === [ Automatic Destruction Settings ] ===
    Destruction:
      Falling Blocks: false
      Fast Leaf Decay: true
      Only Above: false
    Placed Blocks:
      Active: true
      Plugin Name: TreeAssist
      Lookup Time: 86400
    # === [ Plugin Integration Settings ] ===
    Plugins:
      # Count broken blocks towards the TreeFeller ability
      mcMMO: true
      # Count broken blocks towards Jobs jobs that fit
      Jobs: true
      # Allow to set up regions with WorldGuard to prevent destruction with 'treeassist-autochop' and sapling replacement with 'treeassist-replant'
      WorldGuard: false
    # === [ World Related Settings ] ===
    Worlds:
      # Enable world based restrictions
      Restrict: false
      Enabled Worlds:
      - world
      - world2
    Modding:
      Disable Durability Fix: false
    Debug: none
    # The config version for update checks
    Version: 7.0