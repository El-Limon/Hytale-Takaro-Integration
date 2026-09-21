# HytaleTakaroMod

This is the module that is actually built. See the [repository README](../README.md) for:

- what works and what does not (status table, honest limits)
- install steps and the exact config file path
- every configuration key and its default
- build instructions

Build from this directory with `mvn clean package` once `../libs/HytaleServer.jar` is in place.
Run `mvn test` for the unit tests (none of them need the server jar at runtime).

## Note on HytaleCharts

This module contains an optional integration with the third-party site hytalecharts.com. It is
**off by default** and sends nothing anywhere unless `HYTALECHARTS_SECRET` is set. Setting it makes
the server POST its player list — usernames and UUIDs — to hytalecharts.com every five minutes.
