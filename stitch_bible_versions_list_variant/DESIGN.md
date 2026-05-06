# Design System Document: The Sacred Chronometer

## 1. Overview & Creative North Star
This design system is a bespoke framework tailored for the "Bible Planner" iOS Live Activity. It moves beyond the utilitarian nature of standard tracking apps to create an experience titled **"The Sacred Chronometer."**

The Creative North Star is centered on **Reverent Precision**. Unlike traditional trackers that feel frantic or cluttered, this system uses high-end editorial layouts, dramatic typographic scales, and intentional negative space to foster a sense of calm and focus. We reject the "standard grid" in favor of an asymmetric, layered aesthetic that feels as though it were typeset for a premium physical journal.

By utilizing the inherent constraints of iOS 16+ Live Activities—compactness and high visibility—we transform the Lock Screen and Dynamic Island into a sophisticated dashboard for spiritual discipline.

---

## 2. Colors & Tonal Depth
The color palette is rooted in deep obsidian tones, punctuated by a celestial `primary` blue and a `tertiary` gold reserved for moments of completion.

### The "No-Line" Rule
To maintain a high-end, seamless appearance, **1px solid borders are strictly prohibited.** Sectioning must be achieved through background shifts. For example, a card should not be outlined; it should exist as a `surface_container_low` block resting on a `surface` background.

### Surface Hierarchy & Nesting
Depth is communicated through "Tonal Stacking."
- **Level 0 (Base):** `surface` (#121318) - The foundational layer of the Live Activity.
- **Level 1 (Card):** `surface_container` (#1E1F25) - The primary vessel for content.
- **Level 2 (In-set):** `surface_container_high` (#292A2F) - Used for input areas or nested progress backgrounds.

### Glassmorphism & Texture
While gradients are excluded per the project's flat aesthetic, depth is achieved via **Glassmorphism**. Floating elements in the Dynamic Island should utilize semi-transparent versions of `surface_variant` with a heavy backdrop blur. This allows the user's wallpaper to subtly influence the UI, ensuring the component feels "native" rather than "pasted on."

---

## 3. Typography
The typography system uses a modern, clean execution of the SF Pro style (interpreted here via Inter) to balance authority with legibility.

- **Display Scale:** Use `display-sm` for large progress percentages or chapter numbers. This creates an editorial "hero" moment within the small widget.
- **Headline Scale:** `headline-sm` is reserved for the current Book title (e.g., "ROMANS"). It should feel bold and anchored.
- **Body & Label:** Use `body-md` for verse snippets. `label-sm` is used for metadata like "Time Remaining" or "Daily Goal," set in `secondary` text (#C6C6D0) to recede visually.

**Editorial Hierarchy:** Always pair a large `display` element with a `label-sm` element in close proximity. This high-contrast sizing is what separates "app UI" from "editorial design."

---

## 4. Elevation & Depth (The Layering Principle)
Without the use of shadows or gradients, we rely on **Tonal Layering** to define the Z-axis.

- **The Stacking Principle:** Place a `surface_container_lowest` element inside a `surface_container_high` section to create a "sunken" effect for progress tracks. 
- **The Ghost Border:** For high-density information where separation is vital, use the `outline_variant` token at **15% opacity**. This creates a "breath" of a line that defines the edge without adding visual weight.
- **Interaction Depth:** When a component is active or "Live," increase its surface tier (e.g., moving from `surface_container` to `surface_bright`) to indicate status change without needing a glow or drop shadow.

---

## 5. Components

### Dynamic Island Pill Shapes
- **Compact State:** A singular `primary` icon (SF Symbol) on the left, with a `display-sm` numeric value on the right. 
- **Expanded State:** Use an asymmetric layout. The Book/Chapter title sits top-left (`headline-sm`), while the progress bar sits at the bottom, spanning the full width of the pill.

### Progress Bars (Capsule Shape)
- **Track:** `surface_container_highest` (#34343A).
- **Indicator:** `primary` (#B6C4FF).
- **Milestone:** Use a single-pixel gap or a `tertiary` (#FFD700) marker to indicate a "Daily Reading Goal" within a larger plan.
- **Shape:** Full rounded caps (`rounded-full`) are mandatory for all progress elements to match the native iOS 16 aesthetic.

### SF Symbol Icons
- Icons should be "Point" rendered. 
- Use `primary` for active state icons and `on_surface_variant` for inactive or secondary actions.
- **Styling:** Always use the 'Semibold' weight to match the modern SF Pro feel.

### Live Activity Cards
- **Forbid Dividers:** Do not use horizontal lines to separate "Chapter" from "Verse." Use 12px or 16px of vertical whitespace to define the break.
- **Asymmetric Stats:** Place the "Verses Read" count on the far left and the "Time Elapsed" on the far right, leaving a wide void in the center to create an expansive, premium feel.

---

## 6. Do’s and Don’ts

### Do:
- **Do** use `tertiary` (Gold) exclusively for celebratory moments, such as completing a reading plan or hitting a 7-day streak.
- **Do** maximize the use of `surface_container_lowest` for the background of the Live Activity to ensure it integrates perfectly with the iOS Lock Screen's dark aesthetics.
- **Do** lean into the "Sacred" feel by giving text ample "breathing room" (margins of 16pt+).

### Don't:
- **Don't** use shadows. If an element needs to stand out, use a lighter `surface` token.
- **Don't** center-align all text. Use left-aligned headlines paired with right-aligned metadata to create a dynamic, editorial flow.
- **Don't** use standard 100% opaque dividers. They clutter the limited real estate of a Live Activity. Use tonal shifts instead.