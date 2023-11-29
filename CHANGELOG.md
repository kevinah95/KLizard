# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- languages:
  - CLike
  - CodeReader
  - CodeStateMachine

### Added 

- [~] lizard.py
  - [x] Nesting interface
  - [x] Namespace class
  - [~] FunctionInfo class
    - [~] Check `parameters` property
  - [~] FileInformation class
    - [ ] Implement `max_nesting_depth` property this is an extension method
  - [~] NestingStack class
    - [~] Almost done. `var functionStack` probable not necessary
  - [~] FileInfoBuilder class
    - [~] Almost done. I don't know why this is decorated, but I use `_nestingStack` instead of `decorate_nesting_stack` method
  - [~] FileAnalyzer class
    - [ ] `__call__` method
    - [ ] implement `RecursionError`
