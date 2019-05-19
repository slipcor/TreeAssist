# RemoveCustom Command

## Description

This command removes a custom block definition group.

## Usage Examples

Command |  Definition
------------- | -------------
/ta removecustom | Remove a custom block group

## Details

The plugin looks at the three first items of your hotbar and updates the lists for custom trees in this way:

- first slot: sapling block
- second slot: tree block
- third slot: leaf block

These get removed from the list, if a matching row exists.