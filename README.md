# Raspi Monitor

Verify if the host is online using `isReachable`.

Connect over SSH and send a `sudo shutdown now` command.

The configuration file must be placed in user home path with the name `raspi_monitor.env`.

```dosini
HOST=192.168.0.10
# PORT=22
USERNAME=pi
PASSWORD=raspberry
# PING_TIMEOUT=5000
# PING_SLEEP=5000
```