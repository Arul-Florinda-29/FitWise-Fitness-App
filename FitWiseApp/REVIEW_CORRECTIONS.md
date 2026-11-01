# FitWise correction notes


## Requested fixes applied
- Fixed the backend response compilation error by encoding the response body as UTF-8 bytes before writing it.
- Backend registration now validates email syntax and requires at least 8 password characters, matching the desktop registration policy.
- Removed first-launch demo-user creation. Older local data files are also cleaned of the retired `student@test.com` demo record during migration.
- Registration and profile editing now expose an explicit fitness-goal selector. BMI remains informational and no longer silently chooses or overwrites the user's goal.
- Added authenticated `POST`/`PUT /api/profile` support and wired profile editing to persist changes to the backend.
- Removed the 1200 kcal minimum from `DietRecommendation`; the target is now based on the personalized calculation and bounded adaptation adjustment. Calorie estimates remain estimates, not medical prescriptions.

## Verification performed for this revision
- Compiled all Java sources under `src` and `backend/src` successfully with the installed JDK.
- Started the backend and verified health check, rejection of an invalid email, successful registration with an 8+ character password, authenticated profile retrieval, and authenticated profile update/retrieval.
- A full interactive desktop GUI test on the target operating system was not performed.

## Changes applied in this revision
- Registration now validates email format and requires an eight-character minimum password.
- The backend applies the same email/password policy, so requests cannot bypass the desktop form validation.
- Newly registered desktop users no longer have their plaintext password retained in the local serialized user record. Backend authentication uses salted PBKDF2-HMAC-SHA256 hashes.
- BMI rejects non-positive or non-finite inputs rather than returning misleading values.
- BMR/TDEE calculations validate inputs, distinguish Male/Female constants, and use a midpoint estimate for Other. Activity multipliers are explicit.
- Calorie targets use a moderate 300 kcal adjustment for weight change goals and are not forced below estimated BMR.

## Machine-learning / recommendation approach
The project contains a per-user adaptive recommendation engine, not a trained neural-network model. It is a rule-guided online reward learner:
1. Inputs include weekly workout completion, meal logging/adherence, sleep, weight trend, perceived workout difficulty, energy, hunger, meal satisfaction and discomfort.
2. It calculates workout and diet reward scores, then updates per-user strategy scores in `ExperienceLearningModel`.
3. Rule-based safety/comfort checks take priority (reported discomfort, low energy, poor sleep, high hunger or poor adherence).
4. The selected strategy adjusts the next workout/meal plan; the learning state and weekly adaptation are persisted by `DataStore`.
5. This is a small heuristic/reward-based adaptation system. It does not train a predictive model from a large dataset, and its outputs should be treated as wellness suggestions, not medical advice.

## Remaining limitations / security review
- The desktop `DataStore` persists Java-serialized objects under the user's home directory. Existing legacy user records may still contain plaintext passwords; this patch prevents new registration passwords from being retained but does not migrate or securely erase old records.
- Backend binds to 127.0.0.1, so it is a local desktop backend, not remotely reachable from an Android phone or another device.
- The backend uses in-memory bearer tokens and Java serialization for persistence; token expiry/revocation and robust database migrations are not implemented.
- CORS is restricted to loopback origins. Do not expose this prototype backend directly to the public internet.
- Food allergy filtering is name-based and cannot guarantee ingredient-level allergen safety. Verify food labels and ingredients.
- BMI and calorie equations are estimates; calorie targets should be reviewed by a qualified professional, particularly for minors, pregnancy, illness or eating-disorder history.

## Verification status
The source changes in this ZIP must be compiled and the app exercised on the target computer. A successful Java compilation alone does not prove every UI workflow, persistence path, backend request or recommendation screen works end-to-end. No claim of complete end-to-end testing is made here.

## Registration form text visibility update
- Increased registration text-field and dropdown font sizes and control heights for better readability on Windows high-DPI displays.
- Applied consistent styling to the password field as well as other registration controls.
- Added a vertical scroll container around the registration form so controls keep their intended height on smaller windows instead of being compressed and clipping text.
- Switched to a predictable cross-platform Swing look-and-feel to avoid Windows native control metric inconsistencies.
- Recompiled the desktop application and backend successfully after these UI changes.


## Registration BMI guidance update
- Removed the Fitness Goal dropdown from registration so users are not asked to choose weight loss/gain during account creation.
- Added a live BMI and category/guidance display that updates as height and weight are entered.
- The BMI-derived goal is stored internally for recommendation defaults; the registration screen presents it as guidance, not as a question or a required user choice.
- BMI is a screening estimate, not a diagnosis or a standalone instruction to gain or lose weight.
