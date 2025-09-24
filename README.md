### This Addon is for Testing and Implementing new features for BetonQuest

MC: 1.21.4+
BQ: 3.0.0-DEV-416+

# Why?

The reason for this Addon is quite simple:
Contributions to the main plugin require a detailed documentation
and changes to existent features are a well thought decision.

With this Addon I can just throw features that I just want to test
or will still change in functionality or syntax into the world and
don't have to care about consistency and can also use an API version
which is "too new" for what BetonQuest supports.

# Features

## Item

The `simple` item is now allowed to use the new custom model data format and `item-name`.

This means you can now load and save a list of floats and more important use the
`item-model` and `no-item-model` arguments similar to `custom-model-data` and `no-custom-model-data`.

Also, the potion handling is improved and supports data driven potion/"mob" effects.

## Objectives

### - `chat`

Requires the player to write a message in the chat.

If a `variable` value is given the whole content of the message will be stored in the
[
`variable` objective](https://betonquest.org/DEV/Documentation/Scripting/Building-Blocks/Objectives-List/#variable-variable)
with the given Key before the `chat` objective completes.
Like the `variable` event the player is required to have the `variable` objective active.

The optional `cancel` argument cancels the chat message.

Like with every `Objective` you can define `events:` and `conditions:`.

#### Usage: `chat variable:<VariableObjectiveID>#<Key> cancel`

### - `locStore`

Stores the location a placed block in a variable.

The first argument is
the [Block Selector](https://betonquest.org/DEV/Documentation/Scripting/Data-Formats/#block-selectors),
and it requires a `variable` where you define the
[
`variable` objective](https://betonquest.org/DEV/Documentation/Scripting/Building-Blocks/Objectives-List/#variable-variable)
to store the location into.

The format of the stored location can be set with the optional
[`mode`](https://betonquest.org/DEV/Documentation/Scripting/Building-Blocks/Variables-List/#location-variable), and
defaults to `ulfShort`.

It has the optional parameters `loc` and `region`, with which you can define a region
to place the block (similar to the `block` objective).

The stored location can be offset with an optional `vector` (for example `vector:(1;2;3)`).

Like with every `Objective` you can define `events:` and `conditions:`.

#### Usage: `locStore DIAMOND_BLOCK variable:<VariableObjectiveID>#<Key> loc:<Corner1> region:<Corner2> mode:<ulfMode>`

## Playtime

Works with the total ingame playtime.
The amount is in the optional unit. The unit defaults to seconds.

### Condition

#### Usage: `playtime <amount> [unit:<unit>]`

### Objective

The interval is in ticks and defaults to 1200 ticks (= 1 minute).  
The mode is either `total` or `relative`.
Total time means the player needs the time at all, also counting time before starting the objective.
With relative mode only play time after starting the objective counts.

Like with every `Objective` you can define `events:` and `conditions:`.

#### Usage: `playtime <amount> <mode> [unit:<unit>] [interval:<amount>]`

It also has the `left` and `rawseconds` properties, similar to the `delay` objective.

### Variable

#### Usage: `%playtime%` or `%playtime.<unit>%`
