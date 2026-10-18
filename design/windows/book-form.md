# Create and edit book dialogs

References: ../references/02-create-book.png and ../references/03-edit-book.png. Base client size 484x675dp for both. Same positions, field order, column widths and spacing. No outer scrolling, tabs or accordion sections.

## Geometry
Compact 56dp header with 28–32dp book-open icon and Добавить книгу or Изменить книгу; close control at right. Body gutters 16dp, label column 154dp, gap 12dp, input column 286dp (total 452dp). Labels can be left aligned as in the approved new reference; the original two-column order remains. Font 12–13sp. Fourteen rows at 35dp consume 490dp; 30dp controls vertically centered. Body top gap 12dp, note/status area 40dp, footer 64dp leave 13dp flexible slack within 675dp. Reserve status space instead of shifting bottom actions on error.
The total budget is 56+12+490+40+64+13=675dp.

## Exact field order
| Row | Label | Control and meaning |
| --- | --- | --- |
| 1 | Название * | Single-line editable text |
| 2 | Автор * | Editable author combo |
| 3 | Издатель * | Editable publisher combo |
| 4 | Количество страниц * | Numeric text, optional stepper |
| 5 | Год издания * | Numeric text, optional stepper |
| 6 | Твёрдая обложка | Checkbox |
| 7 | Прочитано | Checkbox |
| 8 | Автограф | Checkbox |
| 9 | Жанр | Editable optional genre combo |
| 10 | Цикл | Editable optional cycle combo |
| 11 | Завершённый цикл | Checkbox for selected cycle |
| 12 | Номер в цикле | Position and cycle-size fields separated by из |
| 13 | Обложка | Read-only path display plus folder-open browse action |
| 14 | Стоимость * | Numeric field with ruble suffix |

For row 12 use two 124dp fields and a 38dp separator area. For row 13 reserve 30dp for browse and 8dp gap; the browse button and path field share the same 30dp height. Full external cover path is available in tooltip and never relocated. Canonical chevron-up/down supply optional steppers and combo chevrons. Do not render two currency suffixes.

Revision accepted 2026-10-18: forms have a thin sage outer border to distinguish them from the owner window. Combo controls and their popups stay within the input column, with long suggestions ellipsized and available in full through tooltips. Price entry uses whole rubles and the canonical ruble vector in a separated suffix area. Existing fractional prices are rounded for display only; saving unrelated changes preserves the stored value. Selection shortcuts, including releasing Ctrl after Ctrl+A, must preserve the editor selection.

## Create
Title Добавить книгу, primary Добавить, secondary Отмена. Initial focus is name. Author/publisher placeholder Выберите или введите; absent genre/cycle show Не выбран. Cover placeholder Файл не выбран. No selection means cycle-ended and position controls are disabled; create appropriate lookup data through services when the user supplies it. Do not invent new catalog defaults based on zeros/checked boxes in the mockup: initial domain defaults must follow existing service behavior or a separately accepted decision. Required fields must be entered rather than silently filled to bypass validation.

## Edit
Title Изменить книгу, primary Сохранить, secondary Отмена. Populate actual stored fields and relations; preserve displayed casing and full values. Same field placement as create. Example Дюна values in the PNG are fictional. Keep original external cover path even when unavailable. Do not clear form state on failed save. Changes to shared cycle metadata must follow service semantics, never silently affect unrelated records outside the approved workflow.

## Footer and validation
Note * Обязательные поля. Bottom actions primary left, cancel right on one baseline; no audiobook button. Six required fields: name, author, publisher, pages, publication year, price. Numeric zero is valid, negatives rejected. Optional genre/cycle remain optional. Cycle position has no upper bound against cycle size. Do not change duplicate casing rules. Existing backend owns all mutation validation.
Field-specific errors use outline plus accessible error text and a reserved compact status line; longer explanations can use a tooltip or modal alert. Preserve all fourteen rows and bottom buttons within the base height. Enter submits once; Escape asks before discarding actual unsaved changes. Native file chooser remains native.
