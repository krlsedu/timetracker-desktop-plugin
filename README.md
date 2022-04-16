# csctracker-desktop-plugin

Supports Windows (for now, future suports linux systens):

## How to use

1. Clone and build your jar.

2. Put the config file  (.CscTracker-desktop-plugin-config.json) in your user folder.

3. Run the jar and put to start with windows boot

## Configuring

cscTracker for desktop can be configured via file
(`C:\Users\<user>\.cscTracker\cscTracker-desktop-plugin-config.json`)

## Troubleshooting

First, turn on debug mode from File -> cscTracker Settings..

Now, look for cscTracker related messages in your `.cscTracker/cscTracker-desktop-plugin.log` file:

In that case, add this line to your `~/cscTracker.cfg` file:

    debug = true

(`C:\Users\<user>\.cscTracker\cscTracker.cfg`)

## Back-end
Run the docker-compose in
https://github.com/krlsedu/SBootCscTracker

Or create your own ambient
