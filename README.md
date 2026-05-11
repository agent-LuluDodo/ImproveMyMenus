# [Download on Modrinth](https://modrinth.com/mod/improvemymenus)

![A screenshot of the "Video Settings" screen with Improve My Menus enabled.](README-images/Comparison.png)

A collection of small improvements to various aspects of Minecraft's menus.

# Why?

My aim with this mod was to improve the user experience of Minecraft's menus,
while preserving the Vanilla feel and look.

Most features added by this mod only slightly tweak Vanilla behavior to feel smoother and more responsive.
There are also some bigger additions like indicators, but I tried to avoid big changes away from Vanilla.
Finally, I also added the ability to customize some normally hard-coded key-bindings.

# Feedback

Since my goal is to improve user experience with Minecraft's menus I would greatly appreciate feedback.
Feel free to fill out this [short form](https://forms.gle/7f7TYWbX2aogkBnu8) if your interested in providing feedback.

# Features

This mod contains a variety of features split into different categories.

Feel free to create a [Feature Request]() if you want to request something new,
or alternatively you can create a [Bug Report]() if something's broken.

## Cycle Button

### Indicators

These allow you to instantly gauge the number of possible options and 
additionally provide a way to quickly navigate when pressed.

![A screenshot of the "World Type" cycle button with indicators visible.](README-images/Indicators.png)

### Colored On/Off text

Helps you to immediately recognize the value of an On/Off button. 

![A screenshot of the "Auto-Jump" cycle button with the word "OFF" colored red.](README-images/On-Off%20Colors.png)

### Switches

Transforms buttons which only contain the word ON or OFF into simpler, more recognizable switches.

![A screenshot of a switch in the on position](README-images/Switch.png)

## Sliders

### Indicators

Sliders with few values will have indicators, helping you use them.

![A screenshot of the "Profile" slider with an indicator visible.](README-images/Slider%20Indicators.png)

### Snapping

Vanilla has a 600ms delay before snapping for some slider, 
removing this delay makes the slider feel more responsive.

|                | Comparison                                                                                                       |
|----------------|------------------------------------------------------------------------------------------------------------------|
| Vanilla        | ![A screenshot of the "Biome Blend" slider with Vanilla snapping.](README-images/Vanilla%20Snapping.gif)         |
| ImproveMyMenus | ![A screenshot of the "Biome Blend" slider with improved snapping.](README-images/ImproveMyMenus%20Snapping.gif) |

### Spacing

Vanilla leaves big gaps at the edges of sliders, which can act unintuitively.

|                | Comparison                                                                                             |
|----------------|--------------------------------------------------------------------------------------------------------|
| Vanilla        | ![A screenshot of the "Mipmap Levels" slider with area spacing.](README-images/Area%20Spacing.png)     |
| ImproveMyMenus | ![A screenshot of the "Mipmap Levels" slider with visual spacing.](README-images/Visual%20Spacing.png) |

## Cursor

The cursor doesn't lose its arrow shape when dragging a slider or scrollbar outside its area.

## Improved Debug Options

Added translations for debug options, 
redesigned the tri-state toggle and 
restores the previous screen upon closing the settings.

![A screenshot of the improved "Debug Options" screen.](README-images/Improved%20Debug%20Options.png)

## High Contrast Support

Built-in support for the "High Contrast" Vanilla resource pack.

![A screenshot of the "Video Settings" screen with the "High Contrast" resource pack enabled.](README-images/High%20Contrast%20Video%20Settings.png)

## Customization

Every visual change can be modified using a resource pack, none of them are hard-coded.
The "Debug Options" translations are extendable using a resource pack that means,
you can add additional translations if you're using a mod with additional ones.
Furthermore, tooltips are supported.

Check out the [Resource Packs](https://github.com/agent-LuluDodo/ImproveMyMenus/wiki/Resource-Packs) for more information,
on how to customize ImproveMyMenus.

# Configuration

It's recommended to install [Mod Menu](https://modrinth.com/mod/modmenu) in order to access the mods configuration screen.

Without mod menu you can also access the config by running the command `/improvemymenus-config`.

![A screenshot of the "Improve My Menus Config" screen.](README-images/Config%20Screen.png)

Check out [Configuration](https://github.com/agent-LuluDodo/ImproveMyMenus/wiki/Configuration),
if you are interested in detailed explanations of every config option. 
