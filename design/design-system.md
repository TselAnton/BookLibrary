# Shared design system

## Visual character
A calm desktop catalog: light sage canvas, white inputs/table, dark green text and restrained forest-green emphasis. Dense enough for book metadata without becoming cramped. No subtitle beneath the branded header. No gradients, glass, heavy shadows or large dashboard cards. Green is reserved for primary actions, focus, selection and identity.

## Geometry and color
All geometry is logical dp, text is sp. Use design/tokens.json as the numeric source.
- Canvas #F3F6F2; surface/input #FFFFFF; header #E8F0E9.
- Main text #20372C; secondary #52665A; border #C4CFC7; row divider #DFE6E0.
- Primary #2F6B4F, on-primary #FFFFFF; hover #285D44, pressed #214F3A.
- Selected row #E0EDE3; hovered row #EEF4EF; focus ring #2F6B4F.
- Destructive text/border #B3261E, destructive hover #FCEAE8. Red indicates errors/destructive actions, never routine emphasis.
- Disabled background #EEF1EE and text/icon #64756A. Disabled controls retain readable labels but cannot receive actions.
- Spacing scale 4, 8, 12, 16, 20, 24. Main content gutters 16; form gutters 16–20.
- Control radius 6; table/section surface radius 8; chip radius 12. Thin borders 1dp. Focus outline 2dp. No layout jump on focus.
- Row hover, keyboard focus and persistent selection must be visually distinguishable. Selection is not only a change in checkbox value.

## Typography
Use Segoe UI on Windows with a system sans-serif fallback. Do not bundle proprietary fonts. Main title 22sp semibold; dialog title 20sp semibold; controls/body/table 13sp; form labels 12–13sp; status/caption 12sp. Avoid mathematical Unicode bold and English decorative headings. Long names may ellipsize in table cells with a full-text tooltip; editing fields preserve the full value. Dialog values use the available width and do not overflow.

## Components
- Primary button: green filled, white text, 32dp minimum height. One primary action per action group.
- Secondary button: surface background, thin neutral/green border. Text actions such as Сбросить are green and clearly focusable.
- Icon action: 32x32dp target, 18–20dp glyph centered. No Unicode glyph substitutes for icons. Tooltip and accessible Russian name required.
- Text input and editable combo: white, 1dp border, 28–30dp height in dense forms. Dropdown chevron at right. Focus: 2dp green outline. Required marker follows label and is also exposed semantically.
- Validation: use existing service rules, show specific localized error associated with the field. Reserve a compact form-level status line and indicate error fields; details can be shown in tooltip or alert. Error state must not push bottom actions off-screen or require outer scrolling.
- Checkboxes: 18dp glyph with a larger label/row interaction target. Checked is green with white tick, unchecked is outlined. Use actual Compose Checkbox semantics; assets are visual references, not replacements for accessibility.
- Numeric controls: text entry with compact up/down chevrons if steppers are implemented. Reject negative values according to services; do not infer a series-position maximum from its size. Spinner adornments must not change business rules.
- Chips: pale sage background, border, text and close glyph. Removing a chip clears that condition. Closing the filter editor preserves conditions.
- Dropdown menus: surface background, thin border, subtle 2dp elevation, 28–32dp items. Overlay content; do not resize the window or move the table.
- Status indicators in the main table are read-only. Details use Да/Нет text.
- Window controls: the user selected custom chrome on 2026-10-18 after reviewing the native Windows/Java appearance. Integrate the title and caption controls into the sage header using the provided minimize/maximize/restore/close geometry. Implement dragging, resizing, double-click maximize and keyboard system-menu behavior. Main has all caption actions; fixed-size forms/details have close. Never display native and custom caption controls together. Native file selection remains an OS dialog.
- Caption actions share a neutral filled highlight on hover or visible keyboard focus, with no corner outlines or shadow. Automatic initial focus and mouse focus alone do not highlight them. Closing a window uses the same neutral color as minimize/maximize. Fixed-size windows have a 1dp outer border in #8DA696 so that they remain distinct when overlapping the main window.
- The SVG files in `src/main/resources/com/tsel/home/project/booklibrary/icons/` are the shared icon source. Render them as vectors, including the checkbox tick and combo chevron; do not fall back to the platform theme's glyphs. Dice uses the three-face cube from the main reference.

## Layout and scrolling
Main is a fixed outer Column: header, application menu, toolbar, active chips, flexible table, footer. The [visual filters revision](windows/filters.md) replaces the inline editor with a right overlay drawer. The table and the drawer field area scroll independently; the drawer header and bottom actions stay visible. Create/edit and details have no outer scrollbar and no hidden lower fields. Keep approved form sizes and reduce header/empty space before considering larger windows. Native file chooser may use its standard layout.
Avoid animated window resizing. Filter editor may expand over 120–160ms; respect reduced motion.

## Accessibility and interaction
Tab order follows visible order. Enter triggers the primary form action unless a combo popup or IME consumes it; Escape closes a popup first, then cancels/closes the dialog. Unsaved edits require a discard choice; deletion requires confirmation with the book name. Avoid destructive default focus. Ctrl+F opens the filter drawer, Ctrl+N opens creation, Enter on a selected row opens details. Menus support keyboard traversal and visible focus. All icon-only controls have accessible names. Do not encode read/ended states only through color.
Empty tables show a concise message and a relevant action; no-results provides Сбросить. Failures preserve typed values and show a localized message; technical codes belong in diagnostics. A missing cover uses the book outline placeholder, not an error dialog.

## Acceptance before shipping UI
Check the four base sizes, compact main width, high DPI, keyboard-only navigation, Cyrillic and long names, missing covers, empty/no-results tables, expanded/collapsed filters, validation messages and all bottom actions. Verify no new whole-window scrollbar. These checks are pending until Compose UI exists.
