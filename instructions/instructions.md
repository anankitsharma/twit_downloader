Feature: Inline single active download card on Home

Screenshots and current app attached. Implement this UI behavior only (do not change download engine logic):

1) Single inline card
- When the user taps Download on the Home screen, show a single inline download card **directly below the paste/input + Download button area**.
- The inline card must **reuse the existing Downloads-list item component** (thumbnail/title/progress/status) — do not invent a new visual style.
- Only one inline card is allowed on Home at any time. If a new download starts while an inline card already exists, the new download **replaces** the existing inline card (the replaced item continues or remains in the Downloads list as usual).

2) Behavior while downloading
- The inline card appears immediately when the user taps Download and shows live progress exactly like the Downloads screen item (percentage / progress bar / status).
- Tapping the inline card opens the Downloads screen.
- If a download fails, show the same error state and retry affordance used in the Downloads list.

3) Completion behavior
- When the download completes, update the inline card to its completed state (same visuals as Downloads).
- The inline card may remain on Home until the user navigates away; it should not duplicate in the list (the Downloads screen continues to hold full history).

4) Replacement rule
- If the user starts another download while an inline card is present, immediately replace the Home inline card with the new download’s card (so Home always shows the most recent active download).
- Replaced card must still be tracked in the Downloads list (no data loss).

5) Consistency & stability
- Use the same data/model that the Downloads list uses so progress/state remain consistent between screens.
- Preserve inline card state across rotation while the download is active.
- Ensure no duplicate banners or duplicate inline cards.

Acceptance criteria / QA steps:
1. Paste link → tap Download.
   - Expect: an inline card appears below the input immediately and shows `Downloading…` with live progress.
2. While downloading → tapping the inline card opens the Downloads screen and shows the same item and progress.
3. Start a second download while the first is ongoing:
   - Expect: the inline card is replaced by the new download’s card immediately; the first download continues in background and appears in Downloads.
4. On completion:
 `✅ Download completed` banner appears for ~2–3s in Home area.
5. Rotate device while download is active:
   - Expect: inline card remains and progress continues to update.

Deliverable:
- Code changes limited to Home UI (mounting the Downloads item component and replacement logic), plus a short 1–2 line note describing what was added and where.
