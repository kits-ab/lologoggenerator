# lologoggenerator
Generate logs for fun

## Quick Start
Prerequisites: JDK11, Maven 3.x

Build with:
mvn clean install

Run with:
java -jar target/log-generator-thorntail.jar  

Directories to use can be specified, e.g.:
java -DlogConfDir=lc -DlogDir=ld/ -DconfDir=co -jar target/log-generator-thorntail.jar

Goto localhost:8080 , see Using section below

## Developer Setup
!! Not tested !!
Output exploded war file `lologoggenerator.war` to `<project_path>/docker-dir/lologoggenerator_war` at build time

IntelliJ setup walkthrough: https://www.jetbrains.com/help/idea/deploying-a-web-app-into-wildfly-container.html

## Building and Running
!! Not tested !!
```
docker build -t <image_tag> . \
&& docker run \
-p <host_ip>:<host_port>:<container_port> \
-v <host_path>:<container_path> \
--name docker-wildfly \
<image_tag> 
```

Map `host_path` to `container_path` with `-v <host_path>:<container_path>` to appoint file location on the host machine.

It is presumed that `<host_path>` and `<host_path>/config`exists on the host machine with read/write rights.

## Using

User can create, edit and delete logfile definitions through `http://localhost:<host_port>/lologoggenerator/`

User can choose a log pattern preset or define their own pattern with freetext in logback formatting syntax

User can run log generation through the JSF view `http://localhost:<host_port>/lologoggenerator/`

### Creating a config of log definitions, with the GUI

A logfile definition consists of several parts, and the GUI can be used to enter necessary data to create a logfile definition.

`Filename:` - The name of the output logfile. This file will be placed in `<host_path>`

`Log Pattern Preset:` - Choose a log preset with a pre-defined log pattern, or to define a custom log pattern for this log definition.

`Log Pattern:` - If `CUSTOM_PATTERN` was selected, enter a log pattern according to the logback formatting syntax.

`Time Skew:` - Simulate a time skew/delay (in seconds) for this logger. Enter a non-negative value.

`Frequency Per Minute:` - Number of log rows per minute for this logger. Must be greater than 0.

`New Config`-button - Wipe current config and add only the logfile definition info from the form

`Add to existing config`-button - Append the new logfile definition info to the current config.

### Generate logs based on the current config

`Start` to start generating logs. 

Log generation will run as long as the application is not stopped. The `RUNNING STATUS` indicates if the log generation is active.

`Stop` to stop generating logs.    

## Useful links
https://logback.qos.ch/manual/layouts.html

