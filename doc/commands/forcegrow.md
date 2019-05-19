# ForceGrow Command

## Description

This command will attempt to grow saplings into trees around you in a certain range.

## Usage Examples

Command |  Definition
------------- | -------------
/ta forgegrow | Grow trees around you
/ta forgegrow 20 | Grow trees in a 20 block radius around you

## Hazards

The plugin will attempt to grow trees. However it has no way of checking whether a grown tree will intrude into a player's building or alike.

We use the API that Minecraft gives us, which should work in mose cases and not break anything either.

## Details

Set the maximal radius to be used with the config setting `Force Grow Max Radius`, it is also the default radius when no number is specified.

