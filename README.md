# timetracker-desktop-plugin

Supports Windows (for now, future suports linux systens):

## How to use

1. Clone and build your jar.

2. Put the config file  (.timeTracker-desktop-plugin-config.json) in your user folder.

3. Run the jar and put to start with windows boot

## Configuring

timeTracker for desktop can be configured via file
(`C:\Users\<user>\.timeTracker\timeTracker-desktop-plugin-config.json`)

## Troubleshooting

First, turn on debug mode from File -> timeTracker Settings..

Now, look for timeTracker related messages in your `.timeTracker/timeTracker-desktop-plugin.log` file:

In that case, add this line to your `~/timeTracker.cfg` file:

    debug = true

(`C:\Users\<user>\.timeTracker\timeTracker.cfg`)

## Back-end
Run the docker-compose
https://github.com/krlsedu/SBootTimeTracker
