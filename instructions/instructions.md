Screenshots attached. Fix the share-to-app flow and remove duplicate share targets:

1) Share target selection
- Ensure the Android share sheet only shows the normal app entry (not the "quick" download entry) for our app. If the "quick" target is produced by a separate activity/alias or shortcut, remove or disable that shortcut so only the main app entry appears.

2) Share handling behavior
- When the user shares a video URL to our app (via the share sheet), open the app on the Home tab.
- Automatically paste the shared URL into the Home screen input field (as if the user had pasted it). Do NOT start the download automatically — require the user to tap the Download button to begin.
- If the Home screen is not the current screen, navigate to the Home tab before pasting so the user sees the input and the pasted link.

3) Ads / business note
- Use the normal app entry specifically (not the quick action) so we retain the ability to show the normal ad flow on the Home screen.

Acceptance criteria (verify these)
- Share sheet shows only one app target (the normal app). The quick/fast entry no longer appears.
- Tapping the app from the share sheet opens the app to the Home tab and pastes the shared URL into the input field.
- The download does NOT begin automatically; the user must press Download.
- If the Home tab was already visible, the input field receives focus and the pasted URL is visible.
- No crashes or duplicate paste events occur when sharing multiple times in a row.

Deliverables
- Code changes (files and lines) or manifest edits that removed the quick share target.
- A 1–2 line note explaining what was removed/changed and why.
