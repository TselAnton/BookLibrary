# BookLibrary visual specification

Status: approved sage direction and four window designs, accepted by the user on 2026-10-18. This directory is the implementation source of truth for the current and future UI. Documentation is English; visible application copy is Russian.

## Read in order
1. [Design system](design-system.md): shared visual, interaction and accessibility rules.
2. [Main window](windows/main.md), including the [visual filters revision](windows/filters.md).
3. [Create and edit book](windows/book-form.md).
4. [Book details](windows/book-details.md).
5. [Shared SVG icons](../src/main/resources/com/tsel/home/project/booklibrary/icons/).
6. [Machine-readable tokens](tokens.json).
7. [Menus, reference editors and About](windows/reference-data.md).

Approved visual references are copied into [references](references/). Preserve the originals in artifacts/backend-foundation/ui-design-approved-sage/. The PNGs are scaled mockups, not pixel-exact implementation plans. Written requirements resolve ambiguous generated details. Each future window must follow the shared system and gain its own specification before implementation.

The [SVG gallery](references/svg-icons.svg) shows the application icon set generated from these references. Individual SVGs are bundled with the JavaFX application; the checkbox and combo skins use their paths directly and keep native control behavior.

## Scope
These specifications guide the current JavaFX implementation. The backend remains independently usable. Book selection uses the visual filter drawer and applied tags described in the filter revision; the library search field and legacy command parser have been removed. No new business defaults are introduced.

## Resolved mockup discrepancies
- Use the canonical book-open vector for every window, including the logo, rather than tracing different generated logos.
- The main reference retains an Audio column from the legacy design. Audiobook features are excluded from the current product milestone: hide that column and all audiobook actions until support is explicitly approved. Keep the remaining columns in their original order.
- Generated cycle-ended ticks on books without a cycle are not data rules; render an em dash when the property is not applicable.
- Sample books, covers, paths, prices, timestamps and counts are illustrative only.
- Zero is valid, but the generated prefilled zeros are not an instruction to silently default blank required form fields.
- Images show checkboxes as status indicators in the table. They are read-only there; mutation goes through edit.
- Base logical sizes are requirements; font metrics, 100/125/150/200% DPI and long-text layout still need live Compose verification.

## Future window checklist
Reuse tokens, canonical icons, compact header and button hierarchy. Preserve familiar field order. Keep actions visible; do not introduce whole-window scrolling to the approved forms. Specify all loading, empty, error, disabled and focus states. Keep business mutations in existing services, provide accessible names and keyboard behavior, and add a matching per-window design note. Do not automatically add sidebar navigation, dashboards, gradients or decorative illustrations.
