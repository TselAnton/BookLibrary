# Visual filters revision

This specification records the accepted requirements from the filter-design discussion. It supersedes the inline filter-editor geometry in the original main-window specification. The implementation uses the current JavaFX application and the shared sage tokens.

## Boolean filter: fixed appearance

Use an expandable group heading and vertically stacked checkbox rows, following the supplied online-store reference. Do not use segmented buttons, switches or a permanently visible Any option.

- Header: collapse/expand chevron and the field label. Collapsing a group preserves its draft selections.
- Rows: native checkbox semantics, canonical 18dp checkbox geometry, a localized option label and a secondary count in parentheses. The whole labeled row is interactive. Checked uses the primary green and a white tick; unchecked uses the neutral border.
- Default labels are Есть / Нет; field metadata can supply Прочитано / Не прочитано or other domain wording.
- All rows start unchecked when there is no condition. Checked rows are independent and combine with OR within the group. Conditions for different fields combine with AND.
- A missing value is distinct from false. When missing-value filtering is available, add a checkbox row whose label comes from field metadata. Do not silently convert null to false. Select all values only removes a condition when the selection covers the entire value domain, including missing values where applicable.
- Counts use the other draft groups, excluding the group's own selection. They are computed from actual data. A zero-result row remains visible and can be unchecked if selected.
- Surface #FFFFFF, text #20372C, secondary #52665A, border #C4CFC7, primary #2F6B4F; Segoe UI 13sp, counts 12sp; 8dp label gaps and 28-32dp option rows. Use shared canonical vectors in the application. Respect existing keyboard/focus rules.

## Panel and applied tags

The Filters button opens an overlay drawer from the right, about 380dp wide and limited to the available client width. Keep the main window geometry stable. The drawer header and footer do not scroll; only its field area scrolls vertically. Apply and Cancel are always reachable. Use the existing primary/secondary button hierarchy and 120-160ms motion with reduced-motion support.

Opening copies applied conditions into a draft. Apply validates and commits the draft, refreshes results and tags, then closes. Cancel, the close action and Escape after closing any inner popup discard the draft. Closing or collapsing a field group does not clear applied conditions.

Applied conditions appear as compact pale-sage capsule tags below the toolbar. Each has full readable text and a separate close target. Exceptionally long text can wrap without hiding the close action. Show one row initially. When additional rows exist, use an icon-only down chevron to expand and an up chevron to collapse. The collapsed state displays the hidden-tag count in a separate forest-green circle with white text next to the button; hide this count while expanded. Preserve expansion and applied filters across book details/edit windows and refreshes. Removing a tag immediately removes its condition. Reset clears every applied condition and restores the full catalog.

All option lists show their first five values by default. A localized show-more action reveals the full list and can collapse it again without changing selections. Lists with more than five options include search. A nonempty search shows every matching value, including matches beyond the first five, and hides the collapse action while searching. Clearing search restores the prior manual expansion state. Count refreshes preserve both selection and expansion.

Keep the expansion control, hidden-count badge and reset action at their preferred minimum widths during resizing. Determine badge visibility before measuring the strip; changing the managed children during tag layout must not shrink the action area or overlap reset.

Prepare filter option domains and counts in the background at startup and after catalog refreshes following create/edit/delete. Capture normalized immutable row values on the JavaFX thread first; background work must not read CheckBox controls or mutate nodes. Replace the cache when the catalog changes, reuse it for identical snapshots when viewing unchanged books, ignore obsolete completions, and release its daemon executor when the main window closes. A bounded cache also reuses results for recent condition combinations. Loading uses the reserved footer status area without shifting fields or delaying the opening animation.

Reuse built-in editor instances across drawer openings, restoring applied conditions instead of reconstructing controls. Create checkbox rows only for the currently displayed five values or visible search/expanded results, retaining existing rows for reuse. Opening a warmed drawer must not rebuild domains.

## Dynamic discovery and extension

Use one field-level FilterProperty annotation. Its metadata describes the field: display label, grouping/order, option labels, empty-value labels/policy, value/option providers and validated processor-specific options. Do not repeat the Java type or add separate field annotations for boolean, numeric or text filters.

Each processor declares its supported Java types through an annotation on the processor class. Boolean, primitive boolean and the legacy JavaFX CheckBox can map to the same boolean processor and checkbox-list editor, with explicit value adaptation. A new Java type may reuse an existing processor/editor. A new processing model registers its own processor and editor without changes to the main-window controller. Annotation metadata is validated; missing or ambiguous type mappings produce diagnostics.

Keep discovery, value adaptation, typed conditions, matching, option/count providers and UI rendering separate. Generate typed conditions directly instead of serializing them into special-character search strings. Book selection uses only visual filters. Remove the library search field, legacy command parser, aliases and command help. Ctrl+F opens the filter drawer; the refresh action retains the applied conditions. Search within an option list changes its visible choices without filtering books until Apply. Text conditions and option-list search ignore case with Locale.ROOT and treat е/ё as equivalent; operators and special characters in text remain literal. UI components are not stored inside conditions.

## Verification

Verify independent checkbox choices and OR semantics, missing versus false, counts unaffected by their own group selection, draft/apply/cancel behavior, scrolling with a pinned footer, long labels/tags, compact width, high DPI, keyboard access, tag expansion across book windows, and mapping an additional Java type to an existing processor. Audiobook fields follow the existing milestone visibility policy.

## Implementation and extension points

- `filter.FilterCatalog` discovers visible annotated DTO fields. `FilterProcessorRegistry` resolves exact Java types declared by `@FilterTypes(value = {...}, editor = "...")` on processors. Duplicate, missing or malformed registrations are errors.
- `FilterProperty` supplies `label`, `group`, `order`, `visible`, optional `editor`, boolean option labels, `allowEmpty`, `emptyLabel`, `notEmptyLabel`, `notApplicableLabel`, `unit`, value/option provider classes and processor-specific `settings` in `key=value` form. Java types are not repeated on fields. Built-in processors reject unrecognized settings; custom processors validate their own settings.
- `FilterValueProvider` adapts a DTO field into a value before processor normalization. `FilterOptionsProvider` supplies canonical option values; the default observes the catalog. Providers use public no-argument constructors. `MissingValue.EMPTY` is distinct from `MissingValue.NOT_APPLICABLE`; the cycle-status provider distinguishes a missing cycle from an existing cycle with an unknown completion flag.
- Boolean/primitive boolean/legacy CheckBox use the same checkbox-list editor. Integer and decimal processors share the number editor. Strings default to text conditions; author/publisher/genre/cycle override the editor to searchable checkbox choices. Option lists default to five visible values, with search and expansion for longer lists. Numeric entry accepts comma or dot, ranges include their boundaries, and either bound can be absent. Empty operators use the configured field label.
- `ui.filter.editor.FilterEditorRegistry` maps editor identifiers to factories. To reuse processing for a new Java type, list the type in the existing processor annotation and adapt it in `normalize`, or register a specialized processor using the existing editor identifier. To add a new processing model, register a processor and an editor factory in their registries; no main-controller branches are needed.
- `FilterOptionCache` captures `FilterRow` snapshots and maintains eight recent condition-result futures. Processors and option providers may opt into snapshot-based background domain generation with `supportsSnapshots()`. Snapshot-safe providers read normalized values through `FilterField.value(row)` and return stable canonical values. Providers requiring original DTOs are evaluated once on the UI thread during capture. Custom editors can implement `restore(condition)` to reuse their controls; the default preserves compatibility by recreating only that custom editor.
- Conditions are immutable and contain no controls. `FilterState` owns applied/draft snapshots; `FilterChips` retains expansion independently of data refreshes. The applied badge counts conditions, not selected values.
- The drawer is 380dp wide, below the application menu and above the status footer. Motion takes 140ms; `booklibrary.reducedMotion=true` disables it. Tab/Shift+Tab remain inside the drawer and reveal focused fields; Escape closes an active combo popup before canceling the drawer.
- Tags are at least 26dp high, use 12sp text, a 12dp radius and a separate 24dp close target to fit the accepted compact tag requirement. Long labels wrap. Additional rows reduce the flexible table height only while expanded.

`FilterCatalogTest` covers matching, literal text, numeric validation, missing values, counts, draft isolation and type reuse. `FilterOptionCacheTest` verifies background generation, cached reuse, CRUD invalidation and obsolete-build rejection. `ChoiceFilterEditorTest` checks five-value collapse, expansion, search, selection preservation and lazy control creation for 10,000 options. `SageViewsTest` exercises the actual drawer, compact geometry, fixed actions, numeric errors, tag chevrons/counts, resizing without overlapping actions and a book edit while filters stay applied. Use `booklibrary.captureFilters=true` to capture the checked views into `target/filters-*.ppm`.

## Package responsibilities

```text
filter/
  FilterCatalog, FilterField        discovery, matching and field descriptors
  annotation/                      FilterProperty and FilterTypes
  condition/                       typed predicates and operations
  model/                           values, options, snapshots and applied/draft state
  processor/                       processor contract and type registry
    impl/                          boolean, string and numeric processors
  provider/                        value/option provider contracts
    impl/                          reflection, observed options and cycle adaptation
  cache/                           background option/count cache
ui/filter/
  FilterDrawer, FilterChips         application-facing components
  editor/                          editor contract, factories, registry and controls
    impl/                          checkbox, text and numeric editors
```

Tests mirror their responsibility in `filter`, `filter.cache`, `filter.processor.impl` and `javafx.filter.editor`. The old `SearchFilter` hierarchy, `SearchService`, operand parser and their command-syntax tests are removed. Useful matching behavior is covered by the typed filter tests, including exact numeric boundaries and the distinction between missing values and zero.

## Field-specific operations and price semantics

Pages and publication year are required fields: hide empty/not-empty operations. Required read/autograph/hard-cover flags have only their two boolean choices. Numeric filters support comparisons and inclusive ranges; digit-substring conditions have been removed from the model, processor and editor.

Price preserves the original distinction: null means «Бесценно», zero means «Подарок», and a nonzero value is shown in rubles. The numeric empty operation is captioned «Бесценно», while not-empty is «Стоимость указана» and includes gifts. A zero-price filter is numeric equality to zero. Editor captions and tags both use the field metadata. The form still requires a numeric price for new books; null remains meaningful for existing catalog entries.

See [cache ownership and memory analysis](../filter-cache-analysis.md) for the choice of ordinary maps, LRU bounds and explicit disposal instead of weak keys.
