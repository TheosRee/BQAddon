### This Addon is for Testing and Implementing new features for BetonQuest

# Why?

The reason for this Addon is quite simple:
Contributions to the main plugin require a detailed documentation
and changes to existent features are a well thought decision.

With this Addon I can just throw features that I just want to test
or will still change in functionality or syntax into the world and
don't have to care about consistency and can also use an API version
which is "too new" for what BetonQuest supports.

# Features

## Objectives

### - `chat`

Requires the player to write a message in the chat.

If a `variable` value is given the whole content of the message will be stored in the
[`variable` objective](https://betonquest.org/DEV/Documentation/Scripting/Building-Blocks/Objectives-List/#variable-variable)
with the given Key before the `chat` objective completes.
Like the `variable` event the player is required to have the `variable` objective active.

The optional `cancel` argument cancels the chat message.

Like with every `Objective` you can define `events:` and `conditions:`.

#### Usage: `chat variable:<VariableObjectiveID>#<Key> cancel`
