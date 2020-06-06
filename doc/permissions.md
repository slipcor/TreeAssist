# Permission Nodes

The following nodes can be used:

## Automated tree destruction

Node |  Definition | Default
------------- | ------------- | -------------
treeassist.replant | allows to replant trees | op |
treeassist.destroy.* | **allows all trees** | false
treeassist.destroy.birch | allows birch trees | false
treeassist.destroy.jungle | allows jungle trees | false
treeassist.destroy.oak | allows oak trees | false
treeassist.destroy.spruce | allows spruce trees | false
treeassist.destroy.acacia | allows acacia trees | false
treeassist.destroy.darkoak | allows dark oak trees | false
treeassist.destroy.brownshroom | allows giant brown mushrooms | false
treeassist.destroy.redshroom | allows giant red mushrooms | false

## Commands

Node |  Definition | Default | Function
------------- | ------------- | ------------- | -------------
treeassist.commands | **allows all commands** | op | ALL THE COMMANDS
treeassist.addtool | allows the addtool command | op | add a required tool
treeassist.debug | allows the debug command | op | debug log information
treeassist.findforest | allows the findforest command | op | find forest by tree type
treeassist.forcebreak | allows the forcebreak command | op | force break trees
treeassist.forcegrow | allows the forcegrow command | op | force grow saplings
treeassist.noreplant | allows the noreplant command | **true** | stop replanting saplings for some time
treeassist.purge | allows the purge command | op | purge the database or data.yml
treeassist.reload | allows the reload command | op | reload the TreeAssist config
treeassist.removetool | allows the removetool command | op | remove a required tool
treeassist.toggle | allows the toggle command | op | toggle TreeAssist usage
treeassist.toggle.global | allows the global toggle command | op | toggle global TreeAssist usage
treeassist.toggle.other | allows the player toggle command | op | toggle TreeAssist usage for a player
treeassist.tool | allows the tool command | op | gain the sapling protection tool