# lologoggenerator
Generate logs for fun

## Developer Setup

Output exploded war file to `<project_path>/docker-dir/lologoggenerator_war`

## Building and Running

```
docker build -t <image_tag> . \
&& docker run \
-p <host_ip>:<host_port>:<container_port> \
-v <host_path>:<container_path> \
--name docker-wildfly \
<image_tag> 
```

Map `host_path` to `container_path` with `-v <host_path>:<container_path>` to appoint file location on the host machine.

It is presumed that `host_path` exists on the host machine.
