# Menus, reference data and About

The list-and-card layout was agreed with the user on 2026-10-20. The user chose to prohibit deletion of references used by books. A subsequent request on the same date replaces the individual form fields with a property table and adds book links.

## Application menus

Keep only Файлы, Правка and Справка. Файлы contains Экспортировать данные, Открыть директорию and Выход. Creation remains on the toolbar and Ctrl+N. Правка opens the cycles, authors, genres and publishers editors. Справка contains Проверить обновления and О программе. The update dialog and installation behavior are specified in [the update protocol](../update-protocol.md).

Export uses the native folder picker and writes a uniquely named ZIP containing every JSON file under the running application's repository directory, preserving relative subdirectories and UTF-8 contents. Non-JSON files and symbolic links are excluded. Export runs in the background, disables its menu action until finished, and reports the saved path or failure. It does not use backup retention or prune exported archives. Open directory uses the JAR's parent directory; development runs use the working directory.

## Reference editor

Use an owned modal window, 880 × 600 logical pixels, with the shared 48px sage header and close action. It follows the established colors, typography and thin outer border. The left column is 292px wide; the right column fills the remaining space. Content gutters are 16px, with a 20px column gap.

- Left: name search, then a table with Запись and Книг columns. Search ignores case and the е/ё distinction. Long names ellipsize with a tooltip. A selected record is persistent green; keyboard focus remains visible.
- Right, at the top: one two-column table, Свойство and Значение, with a row for each annotated entity property. Localized labels come from `@Property`; the editor is chosen by the field's Java type. UUID uses a read-only text input that permits selection and copying. Strings use text inputs; boolean uses a checkbox with Да/Нет text; integers use a digits-only input. Authors, genres and publishers have identifier and name rows. Cycles also have Законченный цикл and Книг в цикле rows. The cycle total accepts nonnegative integers, including zero, consistently with existing book forms; it does not constrain a book's series position.
- Right, below the property table: Связанных книг: N, then the scrollable list of underlined book-title links, with full book UUIDs prefixed by ID: aligned at the right. This keeps the relationship count and actual books together, outside the editable properties. A zero count shows the existing empty-list message. Long titles ellipsize with full-title tooltips. Click, keyboard activation or Enter on the selected book opens the actual Карточка книги window, owned by the reference editor and modal to it. Closing the book returns to the same record and preserves unsaved property changes. The existing book edit and delete actions remain available; after closing the card, book links, counts and the main catalog are refreshed. Only properties changed in the reference draft are restored over the refreshed saved values.
- Bottom: a reserved status line, then a fixed 64px action row. Удалить is at the left; Отменить изменения and Сохранить are at the right. The single primary action is green. The delete action uses restrained destructive red only when available.

The record list and related books scroll independently. The property table fits its two or four rows without scrolling. Editors and actions stay visible without an outer scrollbar. No selection disables the property table, book links and all mutation actions. Empty results show Записи не найдены; an unreferenced record shows a zero count and an empty-book message.

Save updates the existing record without changing its UUID or book links. It trims the name, rejects empty and duplicate names, validates the cycle total, and preserves the draft on failure. Successful mutation refreshes the main catalog and filter options while retaining applied filters. Enter saves from property inputs; Ctrl+S also saves. Enter on a book link or the book list opens that book. Reset restores the selected record. Escape and the header close action request confirmation when there are unsaved changes, as do selection and search changes that would abandon the draft. Cancellation preserves the draft and previous selection/search.

Deletion is disabled whenever the record has related books, with a visible explanation below the list. The service independently rechecks links before deletion. A zero-link record can be deleted only after confirmation showing its name and the zero count; cancellation has default focus. Unused records persist across application startup and deletion of the last related book, allowing manual management from this editor.

## About

Use a fixed 440 × 398 logical-pixel owned modal window in the same theme. Center BookLibrary, Личная библиотека любимых книг, Автор — Anton Tsel and the dedication for Jane. The dedication uses the locally available Segoe Script handwriting face, with a cursive fallback. Three rose hearts sit below it. The only footer action is Закрыть; Enter and Escape close the window.

On opening, a brief, non-looping burst of 22 rose vector hearts radiates from the dedication and fades away. Particles ignore mouse events. Closing the window stops the timeline and removes particles. The existing `booklibrary.reducedMotion=true` setting suppresses the burst and retains the three static hearts. Decorative rose is limited to this dedication.

## Verification

`ReferenceServiceTest` covers all four types, reference counts, persistence, validation, protected deletion and manual deletion after removing book links. `DataExporterTest` reads exported ZIP contents, including unknown and nested JSON files, and checks unique names and empty-source failures. `ReferenceWindowsTest` opens actual menu actions with an isolated fixture, checks all four windows, the property table, read-only UUID, numeric entry, checkbox edits, deletion availability, draft protection, book links with mouse/action and keyboard activation, count refresh after book deletion, empty search results and About. `SageViewsTest` checks that saving a reference preserves applied catalog filters.

Use `-Dbooklibrary.captureMenus=true -Dtest=ReferenceWindowsTest` to write PNG previews under target. The fixture never reads the user's repository. Repeat the window check with `-Dglass.win.uiScale=1.5` for high DPI.
