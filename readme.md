# TreeAssist

**Auto Destroy, Auto Replant, and more!**

This plugin will replant trees when they are cut down (or burnt down), and will keep it the same tree type.
It also will take down an entire tree when it is enabled in the config.

***

## Features

- Replants trees (by placing saplings)
- Replants saplings of the tree type you broke
- Automated tree destruction
- Force break command - breaks all trees in a configurable range
- Force grow command - grows all saplings into trees in a configurable range
- mcMMO EXP integration
- Jobs integration
- WorldGuard flag integration

### Options

- Faster leaf decay
- Incrementing of minecraft block break / pickup statistics
- Require certain tools for automated destruction or sapling replanting
- Require lore tool for automated destruction or sapling replanting
- Require breaking of bottom block for automated destruction or sapling replanting
- Prevent tool damage from the automation
- Automatically add broken blocks to the inventory
- Automatically plant saplings that fell from trees
- Only allow automated destruction when sneaking
- Only allow automated destruction when NOT sneaking
- Only allow automated destruction in certain worlds
- Custom item drops, with individual chances

***

## Dependencies

- Spigot 1.13

***

## Downloads

- [spigotmc.org](https://www.spigotmc.org/resources/treeassist.67436/)
- [jenkins - dev builds](https://ci2.craftyn.com/job/TreeAssist/)


***

## How to install

- Stop your server
- Place jar in plugins folder
- Start the server
- Configure if you wish to
- /treeassist reload
- Done !

***

## Documentation

- [Commands](doc/commands.md)
- [Permissions](doc/permissions.md)
- [Configuration](doc/configuration.md)
- [TreeConfiguration](doc/treeconfig.md)

***

## Changelog

- v7.0.100 - first build for Spigot 1.16.1 - should still work on older versions! - first tested build after moving the API to 1.16.1 and changing some things about nether trees
- [read more](doc/changelog.md)

***

## Phoning home

By default, the server contacts www.bstats.org to notify that you are using my plugin.

Please refer to their website to learn about what they collect and how they handle the data.

If you want to disable the tracker, set "enabled" to false in __/plugins/bStats/config.yml__ !

***

## Credits

- FriendlyBaron formerly known as itsatacoshop247 for the original source, the stable foundation that to this day runs strong in TreeAssist
- Bradley Hilton for the Jenkins
- pop4959 for the 1.13 update, and a kick in the butt at the right time
- btilm305 for keeping the repo together in time of need
- uroskn for some critical fixes regarding durability


***