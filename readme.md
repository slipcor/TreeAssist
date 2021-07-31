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
- My [Core](https://github.com/slipcor/Core) library (automatically added to the plugin before release)

***

## Downloads

- [spigotmc.org](https://www.spigotmc.org/resources/treeassist.67436/)
- [Discord - #treeassist-builds](https://discord.gg/kZzmAqzQ9j)


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
- [Troubleshooting](doc/troubleshooting.md)

***

## Changelog

- v7.2.26 - add extra spacing to the config files for readability (thanks RandomGgames!)
- [read more](doc/changelog.md)

***

## Phoning home

By default, the server contacts [bstats.org](https://bstats.org) to notify that you are using my plugin.

Please refer to their website to learn about what they collect and how they handle the data.

If you want to disable the tracker, set "bStats.Active" to false in the __config.yml__ !

***

## Support

I am developing this plugin in my free time, so if you have an issue, please create an issue describing the problem in detail. I will do my best to reply as soon as possible, but that can take a few days sometimes.

For some problems, it might be easier to join the [Discord](https://discord.gg/DSNfjYA) and open a ticket there. It allows for quicker transfer of log files, debug files and so on. Moreover, simple questions can be answered more quickly there rather than opening an issue for them.

Joining the Discord Server gets you early access to latest builds, and maybe it is your preferred method to interact with me? Be my guest!

***

## Todos

* Cooldown for forcebreak and forcegrow
* Refactor configuration nodes to not include spaces to allow for config command logic
* Add more custom WorldGuard flags to deny functionality by region
* Improve debug functionality [Core?] by implementing String filter logic

***

## Credits

- FriendlyBaron formerly known as itsatacoshop247 for the original source, the stable foundation that to this day runs strong in TreeAssist
- Bradley Hilton for the Jenkins
- pop4959 for the 1.13 update, and a kick in the butt at the right time
- btilm305 for keeping the repo together in time of need
- uroskn for some critical fixes regarding durability

