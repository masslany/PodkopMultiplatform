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

Android integration tests live in `androidApp/src/androidTest`. Run them on the Gradle managed
emulator with `./gradlew :androidApp:pixel6Api34DebugAndroidTest`, or on a connected device with
`./gradlew :androidApp:connectedDebugAndroidTest`.

How the harness works:

- Android Test Orchestrator runs every test in a fresh app process and clears the app's data
  (`clearPackageData`), so storage, Koin and the mock server start clean for each test.
- `PodkopTestRunner` starts `TestMainApplication`, which extends `MainApplication`. Before the app
  starts it brings up `MockApiServer`, answers the app-token request (`AuthRoutes`) and adds
  `integrationTestModule`: the mock server's base URL and telemetry that never reports to Firebase.
- `BaseTest` registers each test's routes before launching `MainActivity`.
- The mock dispatcher is strict: a request without a route fails the test, and the failure lists
  every such request. Stub everything a flow requests, including prefetches of the next page.

When writing tests:

- Put repeated UI operations and assertions in feature robots that extend `BaseRobot`. Find list
  items by their lazy-list key (`scrollToKey`), not by position.
- Build responses from API samples (below), with short sentinel values such as `Link 2-25` that
  make UI assertions obvious.
- Prefer stubbing the API over faking app classes, so the real repositories, mappers and startup
  code run. Fake only what HTTP cannot cover, such as telemetry.

#### API samples

Mocked responses must look like what website really serves, and the shape differs by endpoint and
by whether the user is logged in (see `FeaturePaginationPolicies`): guests get numbered pages,
logged-in feeds get opaque cursors sent back as `page`, and most notification groups send cursors
as `key`. So responses are not written by hand:

- `androidTest/assets/api-samples` holds sanitized captures of real responses, usually trimmed to
  one item. A sample fixes the shape: every field, null, enum and pagination key the API sends.
- Fixture builders such as `LinkFixtures` take an item from a sample as the template and generate
  full pages from it, changing only values (ids, titles, sentinel text). They use values the API
  itself sends for missing media (`""` avatars, `null` photos), so tests load no images.
- A new response shape needs a new capture, not a guess.

To capture a sample:

1. Run the debug build (signed in, if the flow is for logged-in users) and open Android Studio's
   App Inspection > Network Inspector.
2. Use the flow, then save the response body of each request you need to `captures/api/` (ignored
   by git). Capture the first page and the next one, so the sample shows how pages link up.
3. Sanitize it into a sample, which replaces names, text, URLs, ids and cursors deterministically
   but keeps the shape:
   `python3 -I scripts/api-samples/sanitize.py captures/api/<capture>.json androidApp/src/androidTest/assets/api-samples/<name>.json`

Integration flow tests should assert visible UI behavior, not implementation details of the mocked
web requests. Pagination tests should prove the user can reach content from later pages by checking
that later-page content is displayed after scrolling.

Feature flows should not pass only because the correct URL was requested.

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
