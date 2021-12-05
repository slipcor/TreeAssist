# Command list

Click on a command to view more information about it. The shorthand can be used instead of the command name, E.g. `/treeassist !at`.

## Commands about plugin activity
_Manage where the plugin is active or for whom_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/treeassist toggle](commands/toggle.md) | !tg | toggle plugin usage for yourself, globally
[/treeassist toggle](commands/toggle.md) [player] [world] | /ta !tg [player] [world] | toggle plugin usage for a player in a specific world
[/treeassist toggle](commands/toggle.md) [world] | /ta !tg [world] | toggle plugin usage for yourself in a specific world
[/treeassist toggle](commands/toggle.md) [player] | /ta !tg [player] | toggle plugin usage for a player, globally

***

## Commands about required tools
_Manage which tools are required for the automated removal_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/treeassist addtool](commands/addtool.md) | /ta !at {trunk type} | add a required tool
[/treeassist removetool](commands/removetool.md) | /ta !rt {trunk type} | remove a required tool

***

## Force commands
_Force breaking and growing_

Command | Shorthand | Definition
------------- | ------------- | -------------
[/treeassist forcebreak](commands/forcebreak.md) | /ta !fb | force break trees around you
[/treeassist forcegrow](commands/forcegrow.md) | /ta !fg | force saplings around you to grow

***

## Config management commands
_use with caution_

### Main Configuration

Command | Shorthand | Definition
------------- | ------------- | -------------
[/treeassist config](commands/config.md) get [node] | /ta !c get bStats.Active | get the value of a config node
[/treeassist config](commands/config.md) info [node] | /ta !c info bStats.Active | get information about a config node
[/treeassist config](commands/config.md) set [node] [value] | /ta !c set bStats.Active false | set the value of a config node
[/treeassist config](commands/config.md) add [node] [value] | /ta !c add wor Spawn | add an entry to a config list
[/treeassist config](commands/config.md) remove [node] [value] | /ta !c remove ignoreworlds pvparena | remove an entry from a config list

### Tree Configuration

TreeConfig | ------------ | -------------
[/treeassist treeconfig](commands/treeconfig.md) get [config] [node] | /ta !tc get default AutomaticDestruction.Active | get the value of a config node
[/treeassist treeconfig](commands/treeconfig.md) info [config] [node] | /ta !tc info default AutomaticDestructions.Active | get information about a config node
[/treeassist treeconfig](commands/treeconfig.md) set [config] [node] [value] | /ta !tc set default AutomaticDestruction.Active false | set the value of a config node
[/treeassist treeconfig](commands/treeconfig.md) add [config] [node] [value] | /ta !tc add default GroundBlocks bedrock | add an entry to a config list
[/treeassist treeconfig](commands/treeconfig.md) remove [config] [node] [value] | /ta !tc remove default GroundBlocks bedrock | remove an entry from a config list

***

## Miscellaneous commands

Command | Shorthand | Definition
------------- | ------------- | -------------
[/treeassist debug](commands/debug.md) | /ta !d | start/stop debug
[/treeassist findforest](commands/findforest.md) [treetype] | /ta !ff  | find biome based on tree type
[/treeassist growtool](commands/growtool.md) | /ta !gt [treetype] | toggle the grow tool for [treetype]
[/treeassist noreplant](commands/noreplant.md) | /ta !nr | stop replanting saplings for some time
[/treeassist purge](commands/purge.md) global | /ta !p global | purge block placement entries globally
[/treeassist purge](commands/purge.md) [world] | /ta !p [world] | purge block placement entries of [world]
[/treeassist purge](commands/purge.md) [days] | /ta !p [days] | purge block placement entries older than [days]
[/treeassist reload](commands/reload.md) | /ta !r | reload the plugin
[/treeassist replant](commands/replant.md) | /ta !rp | force replanting saplings for some time
[/treeassist tool](commands/tool.md) | /ta !pt | toggle the sapling protection tool