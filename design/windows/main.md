# Main library window

Reference: ../references/01-main.png. Base client area 1252x700dp. Legacy minimum width 812dp; use the compact layout described below at that width. Preserve all core metadata columns; no sidebar.

## Vertical layout budget at 1252x700
- Compact branded header: 56dp. Canonical green open-book icon 32dp and Моя библиотека at left; no subtitle. Caption controls occupy the right when using custom chrome.
- Application menu: 28dp; Файл, Правка, Вид, Справка, left aligned. Do not invent backend commands to populate these menus; only implemented actions may be enabled.
- Toolbar: 52dp with 16dp side gutters and 8dp gaps.
- Active chip strip: 40dp when at least one condition exists; otherwise absent.
- Filter drawer: overlays the right side; see [visual filters revision](filters.md). Its scrolling field area has a pinned header and action footer.
- Flexible table fills remaining height; status footer 28dp.
These allocations total 204dp with active chips, leaving 496dp for the table including its header. Additional tag rows reduce table height only while expanded.

## Toolbar
Order: green plus + Добавить книгу; flexible spacing; outlined Фильтры with filter icon, active-count badge and down/up chevron; ruble icon; dice icon. No question-mark help or headphones actions. Book selection uses only the filter drawer; there is no library search field or command syntax. Ctrl+F opens filters. Ruble/dice retain legacy cost-summary/random-book intent; do not invent a new statistics dashboard.
Filters button opens the right drawer described in [visual filters revision](filters.md). A closed drawer may still have active filters. Example chips Не прочитано and Жанр: Фантастика are sample selections; display actual conditions and a Сбросить text action. A chip's X removes only that condition, reset clears every condition and restores the full catalog.

## Table
Order: cover/status thumbnail, №, Название, Автор, Издание, Цикл, Номер, Завершён, Прочитано, Страницы. The historical Аудио column in the reference is hidden in the current milestone. The user explicitly confirmed that Издание displays the publisher name, as in the legacy table; publication year remains in the form and details. Use ellipsis and a full-name tooltip for narrow publisher cells.
At base width, content width 1220dp. Suggested column widths: 36, 40, 268, 190, 90, 180, 80, 88, 88, 80dp (remaining width may expand title). Header 32dp, rows 38–40dp, single selected row, aligned column geometry across header and body. Text left aligned, numeric fields right or centered consistently, status columns centered. Small covers preserve aspect ratio. Loading, missing and unreadable images use `img/default-book-cover.png`: a high-contrast ivory crossed-out book on dark sage, readable at 24x32. External covers load in the background and replace the placeholder in the existing thumbnail without rebuilding the table.
Checked/unchecked are indicators; no inline mutations. Standalone book cycle/position/ended show —, not a completed-series tick. Column sorting only where implemented. Double click/Enter opens details; selection alone does not open a dialog. A vertical scrollbar belongs inside the table. No outer scrollbar or horizontal scrollbar at base size.
Footer shows real totals/results and selected count, for example Найдено: 6 из 248 | Выбрано: 1.

## Compact 812dp mode
Preserve original column order and all current columns. Use 12sp table type, 32dp rows, no thumbnails (keep the narrow placeholder column if needed), short headers with full-label tooltips, ellipsis for name/author/cycle and full-value tooltips. Example available width 780dp: 24, 28, 154, 112, 58, 112, 64, 76, 80, 72dp. Collapse Добавить книгу to plus with tooltip, Фильтры to icon+badge, keep flexible spacing and ruble/dice 32dp. Do not remove keyboard access or hide data behind a horizontal scrollbar. The filter drawer fits the available width and keeps bottom actions outside its scrolling area. Actual font fit requires runtime verification.

## States
Initial empty library: Нет книг and Добавить книгу. No results: Ничего не найдено and Сбросить. Busy: keep layout and focus stable, prevent duplicate actions. Error: compact message with retry without dropping filters/selection. Only applied typed conditions determine table results; option-list search narrows the available choices inside the drawer.
