# TreeAssist Troubleshooting

**Something hasn't gone your way, let's find out why!**

There are a lot of reasons why you might not see ```Automatic Destruction``` happening, or why replanting is not working. If you did not change the config much, read the questions first, otherwise, double check your config settings with the flow charts.

***

## Automatic Destruction

This is when the plugin breaks a block and thinks about what to give you for it.
This might be adding blocks to your inventory or dropping the logs, leaves, custom drops.

```Automatic Destruction``` is not the same as ```Cleanup```, which is active by default,
and removes blocks but does not drop anything, ever!

***

Questions:

1) **Is the Player holding an axe?**

   By default ```Automatic Destruction.Requires Tools``` is active, so without an axe the plugin does not remove the whole tree.


2) **Is the tree instantly disappearing but not dropping anything?**

   Either your ```Cleanup Delay Seconds``` are set too low, or you are in *Creative* mode.


3) **Does the tree disappear later but not drop anything?**
   
   This might be the ```Cleanup``` because of ```Automatic Destruction.Forced Removal```.
   This exists to make sure the tree is always cleaned up.

Full graph for you to check the config: [open graph (png)](images/graph_destruction.png)

***

## Sapling Replant

```Replanting``` only happens after ```Cleanup``` and when the tree has been completely removed.

This occurs when the tree has been automatically removed or when ```Replanting.Enforce``` is active.

***

Questions:

1) **Is ```Automatic Destruction``` working and correctly dropping or giving your items?**
   
   If you want ```Automatic Destruction```, check the section above for guidance. Otherwise, check question 3 below, for how to ```Enforce``` sapling ```Replanting```. 
   

2) **Is the player holding an axe**?
   
   By default ```Replanting.Requires Tools``` is active. If the player is not holding an axe, but you want to replant anyway, use ```Replanting.Enforce```.
   

3) **Is ```Replanting.Enforce``` active?**
   
   By default, it is not. Set it to ```true``` and use ```/treeassist reload```.

Full graph for you to check the config: [open graph (png)](images/graph_replanting.png)

***

Remember that you can always [run a debug](debug.md) and check the results to spot issues. If you need extra support, join the [Discord](https://discord.gg/kZzmAqzQ9j) and we will help you out!
