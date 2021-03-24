# Plant Description Engine, TODO

## Monitor service
* Remove `include` field from DTOs, merge with the included Plant Descriptions.

## Documentation
* Specify the cases where some fields may not be present (AR Kalix's default
  way of handling missing Optionals).
* Document the use of metadata.

## Persistent data
* Use MySQL instead of files to store persistent data.