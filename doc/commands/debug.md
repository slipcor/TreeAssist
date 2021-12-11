# Debug Command

## Description

This command activates/deactivates debug messages.

## Usage Examples

Command |  Definition
------------- | -------------
/ta debug on | activate debugging
/ta debug off | activate debugging
/ta debug why {LOOKUP} | activate debugging and try to find out why something is not working

## Hazards

Don't leave this running for too long, it causes hardware access beyond normal levels as every action the plugin is taking is logged.

Only use when instructed by us!

## Details

In the plugin folder, there will be a logs folder, please send us the contents after stopping the logging.

## LOOKUP

Valid values for the "why LOOKUP" are:

value | explanation
--- | ---
ALL | why is anything and everything?
DECAY | why are leaves not decaying?
GROW | why are trees not growing?
SAPLING | why are there no saplings replanted?
DROPS | why do we not receive log drops / item drops?
AUTOCHOP | why does the tree not get chopped?
CLEANUP | why does the tree not get cleaned up?