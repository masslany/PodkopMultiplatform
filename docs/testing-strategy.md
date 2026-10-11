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
- Scrolling by key does not hide the bottom bar, which stays over the end of a list. Before tapping
  something at the end, such as the retry button under a failed page, swipe like a user
  (`swipeUpOn`), starting mid-list since the list's lower edge lies under the bar.
- Build responses from API samples (below), with short sentinel values such as `Link 2-25` that
  make UI assertions obvious.
- Prefer stubbing the API over faking app classes, so the real repositories, mappers and startup
  code run. Fake only what HTTP cannot cover, such as telemetry.

#### API samples

Mocked responses must look like what website really serves, and the shape differs by endpoint and
by whether the user is logged in (see `FeaturePaginationPolicies`): guests get numbered pages,
logged-in feeds get opaque cursors sent back as `page`, and notifications come in numbered pages.
So responses are not written by hand:

- `androidTest/assets/api-samples` holds sanitized captures of real responses, trimmed to one item
  per distinct item shape. A sample fixes the shape: every field, null, enum and pagination key the
  API sends. `-guest` and `-user` samples are captured logged out and signed in.
- Fixture builders such as `LinkFixtures` pick their templates from a sample by shape and generate
  full pages from them, changing only values (ids, titles, sentinel text). They use values the API
  itself sends for missing media (`""` avatars, `null` photos), so tests load no images.
- A new response shape needs a new capture, not a guess.

What the captures showed (website, 2026-10-09), and the builders reproduce:

- Guest homepage pages are numbered, `{per_page: 25, total: 10000}`, but hold 29 items: a promoted
  link (`published_at: null`) on top and three entries at positions 5, 11 and 17 repeat on every
  page. The app drops repeats by id when appending a page.
- Signed-in homepage pages are cursor pages with only `{next, prev}` (`prev` is null on the first
  page). The first holds 40 links plus the promoted items, later ones 40 links.
- Upcoming stays numbered when signed in, with a real `total`; the last page is simply short.
- Some links carry `recommended: true`; hits links add `related`, `comments.items` and
  `media.photos`.
- Signed-in entries (35 per page, 12-character cursors), tag streams (links and entries mixed) and
  observed feeds are cursor pages too; observed discussions answer `{next: null, prev: null}`.
- Numbered pages are not always full before the end: profile tabs return e.g. 23 of 25 items on
  page 1 with `total: 17381`. Only `total` tells whether more pages exist.
- Link comments report `{per_page: 25, total, total_items}` (`total` counts top-level comments,
  `total_items` includes replies) and inline two replies each; replies page by 50, entry comments
  by 50. Voter lists use `per_page: 100000` and come in one page. Related links have no pagination.
- Favourites came back numbered (`{per_page, total}`, no `next`) for the app's unpaged first
  request, although the app pages them by cursor when signed in.
- Notifications are asked for the way the website does, grouped (`show_grouped=1`): one row per
  group, marked `show_as_group` with `group_id`, `group_count` and `group_updated_at` (when the
  group last got a notification), in numbered pages. A group's own notifications come from
  `notifications/groups/{id}?page=N`, numbered by 25, each carrying the group's id and count.
- `PUT notifications/{group}/{id}` marks one notification read and answers 204 with no body.
  Opening a tag's stream marks that tag's notifications read on the server, so the app sends
  nothing for it; once a group is read, new notifications start a new group.
- Single tag notifications are `new_entry_with_observed_tag` or `new_link_with_observed_tag`, and
  the website titles them as an entry using the tag or a link added with it, not as comments.
- Entry threads (`entries-threads/{id}`, captured as a guest on 2026-10-10) serve the entry with
  its first 25 top-level comments under `comments: {count, total, items}`, where `count` counts
  top-level comments only (45 of 80 in the capture). Comments nest replies the same way, and a
  reply list can be partly inlined (`count: 2` with one item). Later top-level comments and further
  replies come from `.../comments` and `.../comments/{id}/comments` as `data: {count, total,
  items}` with no pagination block, each page starting after the `id` the app sends.
- Not captured yet: vote responses. The app reads no body from them, so tests answer 204.

To capture a sample, use the flow in the debug build with Android Studio's Network Inspector, or on
website in a browser. The website uses the same API, but its requests often differ (`limit=25`
for guests, `entries-threads` and `tags-threads` for feeds), so in a browser prefer replaying the
app's exact GET requests through the site's own HTTP client, which carries the session. Then:

1. Save the response body of each request you need to `captures/api/` (ignored by git). Capture the
   first page and the next one, so the sample shows how pages link up. Never capture token
   responses (`/auth`, `/refresh-token`).
2. Sanitize it into a sample, which replaces names, text, URLs, ids and cursors deterministically
   but keeps the shape:
   `python3 -I scripts/api-samples/sanitize.py captures/api/<capture>.json androidApp/src/androidTest/assets/api-samples/<name>.json --items 5`

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
