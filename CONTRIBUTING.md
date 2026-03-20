# Contributing

Thanks for your interest in contributing to `consorsbank-mcp`.

This project is still early, so clarity, safety, and maintainability matter more than speed.

## Before You Start

- Search existing issues and pull requests before starting new work.
- For larger changes, open an issue or discussion first so the direction can be aligned before implementation starts.
- Keep in mind that this repository deals with trading-related workflows, so changes should favor explicitness and safe behavior.

## Good Contribution Areas

- documentation improvements
- endpoint and DTO modeling
- MCP tool design and ergonomics
- sandbox test coverage
- error handling and observability
- workflow safety around polling, transaction states, and session TAN handling

## Development Expectations

- Target Java 21.
- Prefer small, focused pull requests.
- Keep public APIs and tool names consistent and unsurprising.
- Avoid introducing hidden automation around order placement or authentication flows.
- Document meaningful behavior changes.
- Add or update tests when changing logic.

## Pull Request Guidelines

Please aim for pull requests that:

- explain the problem being solved
- describe the chosen approach
- mention any safety, auth, or workflow implications
- include test notes
- keep unrelated refactors out of scope

## Commit Style

There is no rigid commit convention yet, but concise, descriptive commit messages are preferred.

Examples:

- `add account tool scaffolding`
- `implement order transaction polling`
- `document session tan limitations`

## Code Style

- Prefer readable, explicit Java over overly clever abstractions.
- Keep MCP tool contracts stable and well-documented.
- Normalize Consorsbank API errors consistently.
- Favor small workflow services over large god classes.

## Licensing Reminder

By contributing, you agree that your contribution may be distributed under the repository's `LICENSE` terms.

If you are unsure whether a planned contribution has licensing or usage implications, raise that before submitting the pull request.
