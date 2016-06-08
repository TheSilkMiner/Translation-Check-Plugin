# TODO
This is a list of TODOs to get ready for next version

**Next version:** `2.0`

## Internal things
- [ ] Add Checkstyle (?)

## Additions
- [ ] More Validators (add to following list)
  - [ ] ...
- [ ] GUI for Standalone part of plugin
  - [ ] Allow auto-generation of configuration file through task-specified options
  - [ ] Run tasks
    - [ ] Configuration
    - [ ] Progress and output
- [ ] More configuration options (add to following list)
  - [ ] Translation needed line prefix (default: `#`)
  - [ ] ...

## Changes
- [ ] Log every-time a `NEEDS TRANSLATION` marker is added and/or found
- [X] **EXTREMELY BREAKING CHANGE:** Move all the task configuration to external `skd` file
  - [X] Add `SKL-Java-Interpreter` as sub-module
  - [X] Construct example `skd` file
  - [X] Full SKD parsing ~~and structure migration~~
  - [X] Have cake!
  - [X] FIX Spaces in strings cause the parser to fail (Done in parser repo)
- [ ] Set `\r\n` as default new line value (otherwise the Git Shell complains... and Notepad too)

## Fixes
- [ ] Fix `FormatValidator` not working with positional indexes (`index$`)
- [ ] Fix typos (see IntelliJ `Code inspection` under `Typos`)

## Deletions
- [ ] Remove `TranslationCheckTask` (`dry-run` option does its job)
