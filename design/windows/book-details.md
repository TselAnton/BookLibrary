# Book details window

Reference: ../references/04-book-details.png. Base client area 812x600dp; no scrolling. Compact sage header 56dp, book-open icon, Карточка книги and close. No subtitle, application menu or audiobook action.

## Layout
16dp outer gutters; 20dp gap between left cover and right metadata. Left column 244dp, right 516dp (16+244+20+516+16=812). Content begins 16dp below header. Cover fits inside 244x370dp preserving original aspect ratio; never crop/stretch. No caption below the cover. Loading, missing and unreadable external covers use `img/default-book-cover.png`, the same ivory crossed-out book on dark sage as the table. The image loads in the background and replaces the placeholder automatically; metadata remains usable throughout loading.

Metadata has twelve ordered rows at 34dp (408dp). Labels are muted secondary text, values dark. Use a 190dp label column, 12dp gap and 314dp values. Name value may be 18sp semibold; other values 13sp. Long values use available space, ellipsis with full-value tooltip when needed; no outer scrolling. Dividers are thin and subdued.

| Order | Label | Display |
| --- | --- | --- |
| 1 | Название | Actual book name |
| 2 | Автор | Author name |
| 3 | Издатель | Publisher name |
| 4 | Количество страниц | Integer |
| 5 | Год издания | Integer |
| 6 | Жанр | Name or — |
| 7 | Твёрдая обложка | Да / Нет |
| 8 | Прочитано | Да / Нет |
| 9 | Автограф | Да / Нет |
| 10 | Стоимость | Localized numeric value and ₽ |
| 11 | Цикл | Name · position из size, or — |
| 12 | Цикл завершён | Да / Нет, or — with no cycle |

Use text booleans, not disabled input controls. No additional editing fields, rating, description or invented timestamps.

## Actions
Footer stays at bottom, height 64dp, top divider. Green Изменить left, red outlined Удалить next, outlined Закрыть right. Editing opens the matching edit dialog; success refreshes actual values. Deletion asks for confirmation with book name and a neutral cancel choice before invoking the atomic service workflow. On success close details and refresh the main table. Escape and Закрыть close without mutation. Errors preserve the window and display a localized message.

## Layout budget
Header 56 + top gap 16 + metadata 408 + bottom gap 56 + footer 64 = 600dp. Cover fits within the content region. A full-value tooltip or small accessible detail popover handles exceptionally long strings without adding a whole-window scrollbar.
