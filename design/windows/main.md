# Main library window

Reference: ../references/01-main.png. Base client area 1252x700dp. Legacy minimum width 812dp; use the compact layout described below at that width. Preserve all core metadata columns; no sidebar.

## Vertical layout budget at 1252x700
- Compact branded header: 56dp. Canonical green open-book icon 32dp and Моя библиотека at left; no subtitle. Caption controls occupy the right when using custom chrome.
- Application menu: 28dp; Файл, Правка, Вид, Справка, left aligned. Do not invent backend commands to populate these menus; only implemented actions may be enabled.
- Toolbar: 52dp with 16dp side gutters and 8dp gaps.
- Active chip strip: 40dp when at least one condition exists; otherwise absent.
- Optional filter editor: up to 80dp, below active chips. Reduce table height, never increase window height.
- Flexible table fills remaining height; status footer 28dp.
These allocations total 284dp with expanded filters and active chips, leaving 416dp for table including its header.

## Toolbar
Order: green plus + Добавить книгу; search icon inside a flexible field with placeholder Название, автор или цикл…; outlined Фильтры with filter icon, active-count badge and down/up chevron; ruble icon; dice icon. No question-mark help or headphones actions. Search accepts text; Enter applies it. Ruble/dice retain legacy cost-summary/random-book intent; do not invent a new statistics dashboard.
Filters button toggles editor. A collapsed editor may still have active filters. Example chips Не прочитано and Жанр: Фантастика are sample selections; display actual conditions and a Сбросить text action. A chip's X removes only that condition, reset clears structured conditions. Preserve free-text query when resetting structured filters.

## Table
Order: cover/status thumbnail, №, Название, Автор, Издание, Цикл, Номер, Завершён, Прочитано, Страницы. The historical Аудио column in the reference is hidden in the current milestone. The user explicitly confirmed that Издание displays the publisher name, as in the legacy table; publication year remains in the form and details. Use ellipsis and a full-name tooltip for narrow publisher cells.
At base width, content width 1220dp. Suggested column widths: 36, 40, 268, 190, 90, 180, 80, 88, 88, 80dp (remaining width may expand title). Header 32dp, rows 38–40dp, single selected row, aligned column geometry across header and body. Text left aligned, numeric fields right or centered consistently, status columns centered. Small covers preserve aspect ratio; missing image uses book-open.
Checked/unchecked are indicators; no inline mutations. Standalone book cycle/position/ended show —, not a completed-series tick. Column sorting only where implemented. Double click/Enter opens details; selection alone does not open a dialog. A vertical scrollbar belongs inside the table. No outer scrollbar or horizontal scrollbar at base size.
Footer shows real totals/results and selected count, for example Найдено: 6 из 248 | Выбрано: 1.

## Compact 812dp mode
Preserve original column order and all current columns. Use 12sp table type, 32dp rows, no thumbnails (keep the narrow placeholder column if needed), short headers with full-label tooltips, ellipsis for name/author/cycle and full-value tooltips. Example available width 780dp: 24, 28, 154, 112, 58, 112, 64, 76, 80, 72dp. Collapse Добавить книгу to plus with tooltip, Фильтры to icon+badge, keep search flexible and ruble/dice 32dp. Do not remove keyboard access or hide data behind a horizontal scrollbar. Filter controls may wrap into two compact rows within the 80dp editor budget. Actual font fit requires runtime verification.

## States
Initial empty library: Нет книг and Добавить книгу. No results: Ничего не найдено and Сбросить. Busy: keep layout and focus stable, prevent duplicate actions. Error: compact message with retry without dropping search/selection. Query/filter UI must call the existing search capabilities; any unsupported filter is disabled until implemented.
