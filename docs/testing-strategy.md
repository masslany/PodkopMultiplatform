# Testing Strategy (Living Document)

This document captures the current testing approach for the project and should be updated as the test suite grows.

## Goals

- Build a test suite that is easy to extend and hard to break accidentally.
- Keep tests readable by removing repetitive setup from test files.
- Prefer deterministic hand-written fakes over mocks.
- Add tests incrementally, starting from stable business logic (mappers, repositories, use cases).

## Core Principles

- Prefer **fakes** over mocks.
- Use **mocks only when necessary** (for behavior verification that is difficult to express with a fake, or external APIs/frameworks that are expensive to fake).
- Keep tests **small and focused on one behavior**.
- Make inputs and expected outputs **explicit**.
- Treat bug fixes as a trigger to add a **regression test**.

## Test Support Architecture

Shared test utilities should live inside the module being tested under `testsupport`.

Current structure in `business` module:

- `business/src/commonTest/kotlin/pl/masslany/podkop/business/testsupport/fixtures`

Planned structure as tests expand:

- `.../testsupport/fixtures` for valid DTO/domain builders and sample models
- `.../testsupport/fakes` for hand-written fake repositories/data sources/services
- `.../testsupport/assertions` for custom assertions/matchers when repeated patterns appear

### Fixtures

Fixtures are centralized builders that provide:

- Valid defaults
- Deterministic values
- Easy overrides for the fields that matter in a specific test

Convention:

- In test files, use a meaningful alias like `Fixtures` (not `F`).
- Avoid creating large one-off object graphs directly inside tests when a shared fixture builder exists.

## Naming Conventions

### Test names

Use Kotlin backtick function names for test cases.

- Preferred: ``fun `maps null values to defaults`()``
- Avoid: `fun maps_null_values_to_defaults()`

### SUT naming

When testing a **class instance**, name it `sut` (System Under Test).

Example:

```kotlin
private val sut = AppDeepLinkParser()
```

Notes:

- For pure top-level functions / extension mappers there may be no instantiated class, so no `sut` variable is needed.

## Test Structure

Recommended test flow:

1. Arrange input with shared fixtures (override only relevant fields).
2. Act by calling the mapper / function / `sut`.
3. Assert exact expected values for the behavior under test.

For mapper tests specifically, cover:

- Happy-path field mapping
- Fallback/default mapping (`null` -> defaults)
- Enum/string conversions
- Nested mapping composition
- Collection mapping
- Edge cases that have caused bugs or are easy to regress

## Fakes Strategy (Preferred over Mocks)

When adding repository/use-case tests:

- Create small hand-written fakes that implement interfaces used by the `sut`.
- Let fakes expose simple configuration for responses (success/failure data).
- Let fakes record calls/arguments when behavior verification is needed.
- Keep fake behavior obvious and deterministic.

Use a mock framework only if:

- A fake would be disproportionately complex, or
- The test must verify framework-specific interaction behavior that a fake cannot represent clearly.

## Current Progress

Completed:

- Introduced centralized `business` test fixtures (`BusinessFixtures`)
- Added unit tests for all business mappers (common/profile/tags/link mappers)
- Standardized mapper test names to Kotlin backtick format

## Next Recommended Targets

1. Business repositories (`*RepositoryImpl`) with fake data sources
2. Startup/auth business logic classes
3. Compose feature view models (with fakes for dependencies)
4. Parser/utility classes in other modules using the same conventions (`sut`, backtick names)

## Maintenance Rules

- Add new shared builders/fakes in `testsupport` first, then use them in tests.
- Do not introduce ad-hoc duplication across many test files if a shared helper would clarify intent.
- Keep fixture defaults realistic enough to prevent accidental invalid-object tests.
- Update this document when conventions change.
