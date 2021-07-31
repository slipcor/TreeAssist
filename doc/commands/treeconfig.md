# TreeConfig Command

## Description

This command allows to manipulate a tree config file

## Usage Examples

Command |  Definition
------------- | -------------
/ta config get [config] [node] | Tells you the value of a node
/ta config info [config] [node] | Tells you information about the node
/ta config set [config] [node] [value] | Tries to set the node to a value
/ta config add [config] [node] [value] | Add a string value to a string list node
/ta config remove [config] [node] [value] | Remove a string value from a string list node

## Details

The plugin will do its best to auto tab complete the config and the node, always make sure you use tab or know what you are doing.

Please look into the inheritance of the configs. All configs inherit from the `default` config, and then for example we have `oak` which is a parent of `tall_oak`. Please let tab complete help you.