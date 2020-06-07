# v7.X Changelog

## v7.0 - Minecraft 1.16 Rewrite
- v7.0.95 - add large ferns to the natural blocks, improve mushroom detection
- v7.0.94 - when warning about "lower than minimum", show more information
- v7.0.90 - First rewrite commit
- Complete rewrite of tree determination and tree definition.
- Outsourcing of over 30 config.yml nodes to the new trees/default.yml node.
- The folder `trees` contains all tree definitions. The examples should help set up custom definitions.
- config.yml rewrite with attempted migration and warning in case there are wrong entries.
- config.yml has custom comments in hopes to clear up some confusion.
- Support and defaulting to the new material IDs, e.g. `minecraft:oak_sapling` instead of `OAK_SAPLING`.
- Migrating of all chances to probabilities - instead of percentages we use factors where 1.0 is 100% chance.
- Reorganization of Utils class, separation into separate utility classes
- JavaDocs for every public and every important method
- TreeAssist events fully documented, added some more configurability, and changed the replacement event to work with Material instead of its name
- mcMMO hooks now should support all block types, mcMMO should decide what kind of EXP to add
- `noreplace` command renamed to `noreplant`, including permissions and config nodes
- config default for "Only When Bottom Broken First" changed, as this setting never worked anyway
- logs should break from bottom up (thick trees look much better now)
- leaves should break from inside out
- destruction should be much faster now as we check for duplicates
