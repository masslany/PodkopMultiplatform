# Adding A New Screen

This document is the reference for adding a new screen in `Podkop`, especially when the screen needs to work correctly with:

- status bars
- display cutouts
- landscape on phones
- gesture/navigation bars
- `Scaffold`
- home split layout

The goal is simple:

- top bars should respect the top safe area
- content should not accidentally get double horizontal padding
- scrollable content should be able to go under the bottom system bar when desired
- navigation rails and bottom sheets should not consume insets twice

## Where Route Padding Comes From

Standalone routed screens receive `paddingValues` from `App.kt`:

```kotlin
val safeDrawingPaddingValues = WindowInsets.safeDrawing.asPaddingValues()
```

Those `paddingValues` are passed into screen roots like:

```kotlin
entry<HitsScreen> {
    HitsScreenRoot(
        paddingValues = safeDrawingPaddingValues,
    )
}
```

The helper used by most screens lives in:

- `composeApp/src/commonMain/kotlin/pl/masslany/podkop/common/extensions/PaddingValuesInsetsExtension.kt`

```kotlin
@Composable
internal fun PaddingValues.toWindowInsets(
    includeTop: Boolean = true,
    includeBottom: Boolean = true,
): WindowInsets
```

Use that helper instead of rebuilding `WindowInsets` by hand.

## Choose The Right Pattern

Use one of these two patterns:

1. Routed `Scaffold` screen
   Use this for regular standalone screens like `Hits`, `Profile`, `Favorites`, `Notifications`, `Settings`.

2. Home-tab screen
   Use this for screens hosted inside `HomeScreenRoot`, where the home container already manages the outer safe areas and split layout.

Do not mix the two patterns.

## Pattern 1: Routed `Scaffold` Screen

Use this when the screen has a `TopAppBar` and scrollable content.

### Recommended skeleton

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExampleScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ExampleViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    ExampleScreenContent(
        paddingValues = paddingValues,
        state = state,
        actions = viewModel,
        lazyListState = lazyListState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleScreenContent(
    paddingValues: PaddingValues,
    state: ExampleScreenState,
    actions: ExampleActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val layoutDirection = LocalLayoutDirection.current
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)

    // Important:
    // exclude both top and bottom from Scaffold content insets
    // when the scrollable body should manage its own bottom safe space.
    val contentInsets = paddingValues.toWindowInsets(
        includeTop = false,
        includeBottom = false,
    )

    val bottomInsetPadding = paddingValues.calculateBottomPadding()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Example") },
                navigationIcon = {
                    IconButton(onClick = actions::onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = topBarInsets,
            )
        },
        contentWindowInsets = contentInsets,
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPaddingValues ->
        // Important:
        // keep top/horizontal Scaffold padding
        // but do NOT apply bottom here, otherwise the whole body gets pushed
        // above the gesture/navigation bar.
        val bodyPadding = PaddingValues(
            start = innerPaddingValues.calculateStartPadding(layoutDirection),
            top = innerPaddingValues.calculateTopPadding(),
            end = innerPaddingValues.calculateEndPadding(layoutDirection),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bodyPadding),
            state = lazyListState,
            contentPadding = PaddingValues(
                bottom = bottomInsetPadding + 16.dp,
            ),
        ) {
            items(state.items) { item ->
                ExampleItem(item = item)
            }
        }
    }
}
```

### Why this works

- `TopAppBar` owns the top safe area through `windowInsets = topBarInsets`.
- `Scaffold` still provides the normal top slot offset via `innerPaddingValues.calculateTopPadding()`.
- The bottom safe area is not applied to the whole body.
- The list gets the bottom safe area as `contentPadding`, so items can scroll under the navigation area correctly.

### Use this pattern for

- feeds
- paginated lists
- profile/details screens with a top app bar
- screens with FABs anchored above the gesture bar

### FAB rule

If the screen has a FAB, anchor it with the bottom safe inset:

```kotlin
floatingActionButton = {
    FloatingActionButton(
        modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
        onClick = { /* ... */ },
    ) {
        Icon(...)
    }
}
```

Or add the inset to an existing design offset:

```kotlin
bottom = paddingValues.calculateBottomPadding() + 16.dp
```

## Pattern 2: Home-Tab Screen

Home tabs are different.

They do not own the full window safe area themselves. `HomeScreenRoot` already decides how much padding each pane gets in:

- bottom-bar layout
- split layout
- landscape with cutout on the left or right

That means a home-tab screen should not recreate route-level safe-area logic.

### Recommended skeleton

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleHomeTabContent(
    paddingValues: PaddingValues,
    state: ExampleState,
    actions: ExampleActions,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val layoutDirection = LocalLayoutDirection.current
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)

    Box(
        modifier = modifier
            .padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
            )
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopAppBar(
                title = { Text("Example") },
                scrollBehavior = scrollBehavior,
                windowInsets = topBarInsets,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                ),
            ) {
                items(state.items) { item ->
                    ExampleItem(item = item)
                }
            }
        }
    }
}
```

### Why this works

- `HomeScreenRoot` already decided which horizontal inset belongs to the rail and which belongs to the content pane.
- the tab content only consumes the horizontal padding it is given
- the list owns the bottom inset

### Important

Do not wrap home-tab content in another route-style `Scaffold` unless you really need one.

## Pattern 3: Non-Scrollable or Form Screens

If the screen should keep all content fully above the bottom system bar, the simpler `Scaffold` pattern is fine:

```kotlin
val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
val contentInsets = paddingValues.toWindowInsets(includeTop = false)

Scaffold(
    topBar = { /* ... */ },
    contentWindowInsets = contentInsets,
) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        // form content, settings cards, etc.
    }
}
```

Use this for:

- settings
- search forms
- simple detail screens without edge-to-edge scrolling needs

## Common Mistakes

### 1. Applying bottom inset to the whole `Scaffold` body

This causes a visible gap above the navigation/gesture area.

Bad:

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(innerPaddingValues),
)
```

Better for scrolling screens:

```kotlin
val bodyPadding = PaddingValues(
    start = innerPaddingValues.calculateStartPadding(layoutDirection),
    top = innerPaddingValues.calculateTopPadding(),
    end = innerPaddingValues.calculateEndPadding(layoutDirection),
)
```

Then give the list:

```kotlin
contentPadding = PaddingValues(
    bottom = paddingValues.calculateBottomPadding() + 16.dp,
)
```

### 2. Giving a `NavigationRail` extra start inset

`NavigationRail` already applies start/vertical insets by default.

Do not add another manual start cutout padding around it, or the left inset will be consumed twice in landscape.

See:

- `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/bottombar/BottomBarRoot.kt`

### 3. Using route-level safe-area logic inside home tabs

Home tabs already get adjusted padding from `HomeScreenRoot`.

If you reapply raw `safeDrawing` assumptions there, landscape split mode will break.

See:

- `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/home/HomeScreenRoot.kt`
- `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/links/LinksScreenRoot.kt`
- `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/entries/EntriesScreenRoot.kt`

### 4. Adding horizontal safe-area padding to a centered modal sheet

A centered bottom-sheet card usually does not need left/right `safeDrawing` padding.

That can create unnecessary empty space when the card is already far from the cutout.

See the composer implementation for the current expected behavior:

- `composeApp/src/commonMain/kotlin/pl/masslany/podkop/common/composer/Composer.kt`

## Copy-Paste Checklist

Before finishing a new screen, verify:

- `TopAppBar` uses `paddingValues.toWindowInsets(includeBottom = false)`
- horizontal padding is applied only once
- `Scaffold` body is not accidentally consuming bottom safe space twice
- scrollable content owns its own bottom `contentPadding`
- FAB and bottom controls include `paddingValues.calculateBottomPadding()`
- home tabs do not recreate route-level safe-area handling
- landscape on a phone with a left or right cutout still looks correct

## Good Current References

Use these files as the closest live references:

- Routed `Scaffold` screen with list-owned bottom inset:
  - `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/hits/HitsScreenRoot.kt`
  - `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/profile/ProfileScreenRoot.kt`

- Home-tab screen:
  - `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/links/LinksScreenRoot.kt`
  - `composeApp/src/commonMain/kotlin/pl/masslany/podkop/features/entries/EntriesScreenRoot.kt`

- Shared inset helper:
  - `composeApp/src/commonMain/kotlin/pl/masslany/podkop/common/extensions/PaddingValuesInsetsExtension.kt`

If a new screen does not clearly fit one of the patterns above, stop and decide first whether it behaves like:

- a routed standalone screen
- a home tab
- a modal/bottom sheet

That decision should happen before writing the layout.
