# Plant Description Engine, TODO

## Misc
* Make connection retries and delay between retries configurable

## Management service
* Trim and set lowercase on all system names

## Monitor service
* Remove `include` field from DTOs, merge with the included Plant Descriptions.
* Clear 'cannot be monitored' alarms when PD changes.
* Mismatch when connecting ports with differing service definitions?

## Documentation
* Specify the cases where some fields may not be present (AR Kalix's default
  way of handling missing Optionals).

## Persistent data
* Use MySQL instead of files to store persistent data.