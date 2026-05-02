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
- `business/src/commonTest/kotlin/pl/masslany/podkop/business/testsupport/fakes`

Planned structure as tests expand:

- `.../testsupport/fixtures` for valid DTO/domain builders and sample models
- `.../testsupport/fakes` for hand-written fake repositories/data sources/services
- `.../testsupport/assertions` for custom assertions/matchers when repeated patterns appear

### Android Integration Tests

Android integration tests live in `androidApp/src/androidTest` and use a test-only application
started by `PodkopTestRunner`.

The integration harness should:

- Start `MainActivity` through Android instrumentation.
- Put common activity/server/Koin lifecycle setup in `BaseTest`.
- Put repeated UI operations and assertions in feature robots that extend `BaseRobot`.
- Point the app network stack at `MockWebServer` using the injected network base URL.
- Serve deterministic response JSON from `androidTest/assets/mock-api`.
- Use synthetic fixtures first, with short sentinel text values that make UI assertions obvious.
- Keep the mocked dispatcher strict: unknown requests should fail loudly instead of falling through.
- Replace startup/auth/background polling dependencies with deterministic fakes unless the test is
  explicitly covering those flows.

Integration flow tests should assert visible UI behavior, not implementation details of the mocked
web requests. Pagination tests should prove the user can reach content from later pages by checking
that later-page content is displayed after scrolling.

Mock server request recording can remain available for diagnostics or lower-level harness tests, but
feature flows should not pass only because the correct URL was requested.

When a synthetic fixture proves a regression path, add real trimmed API JSON later for contract
coverage without replacing the simpler sentinel fixture.

Feature UI test tags should live in one feature-level object, for example `LinksTestTags`, with
nested groups such as `Screen` when a feature grows. Keep tag names stable and hierarchical, for
example `links:screen:list`.

### Fixtures

Fixtures are centralized builders that provide:

- Valid defaults
- Deterministic values
- Easy overrides for the fields that matter in a specific test

Convention:

- In test files, use a meaningful alias like `Fixtures` (not `F`).
- Avoid creating large one-off object graphs directly inside tests when a shared fixture builder exists.

### Fakes

Fakes are shared, hand-written test doubles that should:

- Be deterministic
- Expose simple stubbing points (usually mutable `Result`/value fields)
- Record calls/arguments for assertions
- Fail fast when a required stub is missing

Current shared fakes in `business` tests include:

- Dispatcher provider fake
- In-memory key-value storage fake
- Recording data-source fakes for repository tests (`Auth`, `Entries`, `Hits`, `Links`, `Profile`, `Tags`)

## Naming Conventions

### Test names

Use Kotlin backtick function names for test cases.

- Preferred: ``fun `maps null values to defaults`()``
- Avoid: `fun maps_null_values_to_defaults()`
- Exception: Android instrumented tests should use dex-safe camelCase names because older dex
  targets reject spaces in method names.

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
- Added centralized repository fakes (`testsupport/fakes`)
- Added repository tests for all business repositories (`Auth`, `Entries`, `Hits`, `Links`, `Profile`, `Tags`)
- Added the first Android integration harness for deterministic MockWebServer-backed UI flows.

## Next Recommended Targets

1. Startup/auth business logic classes
2. Compose feature view models (with fakes for dependencies)
3. Parser/utility classes in other modules using the same conventions (`sut`, backtick names)
4. Add shared fake helpers for repeated patterns (queued results, call assertions) only when duplication appears

## Maintenance Rules

- Add new shared builders/fakes in `testsupport` first, then use them in tests.
- Do not introduce ad-hoc duplication across many test files if a shared helper would clarify intent.
- Keep fixture defaults realistic enough to prevent accidental invalid-object tests.
- Update this document when conventions change.
