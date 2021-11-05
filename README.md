# wakatime-desktop-plugin

This is an open source unofical [wakatime](https://wakatime.com/) Desktop plugin for metrics, insights, and time tracking automatically generated from your
activity.

Supports Windows (for now, future suports linux systens):

## How to use

1. Clone and build your jar.

2. Put the config file  (.wakatime-desktop-plugin-config.json) in your user folder.

3. Run the jar and put to start with windows boot

## Configuring

WakaTime for desktop can be configured via file
(`C:\Users\<user>\.wakatime-desktop-plugin-config.json`)

For more settings, WakaTime plugins share a common config file `.wakatime.cfg` located in your user home directory
with [these options](https://github.com/wakatime/wakatime#configuring) available.

## Troubleshooting

First, turn on debug mode from File -> WakaTime Settings..

Now, look for WakaTime related messages in your `.wakatime/wakatime-desktop-plugin.log` file:

In that case, add this line to your `~/.wakatime.cfg` file:

    debug = true

(`C:\Users\<user>\.wakatime.cfg`)
