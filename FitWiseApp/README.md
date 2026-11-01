# FitWise — Experience-Based Online Learning

## What this version does
FitWise now uses a **per-user online-learning model** instead of fixed-example KNN. The model learns from each user's own weekly experience and persists its learned strategy scores between app sessions.

### How learning happens
1. During the week, the app records the user's completed workouts, meal status, sleep logs, and weight/progress entries in `~/.fitwise/fitwise_data.ser`.
2. The user opens **Workout Plan → WEEKLY EXPERIENCE REVIEW** and submits their experience (difficulty, energy, hunger, meal satisfaction, discomfort).
3. Starting with the second weekly review, the app evaluates the previous week's workout and diet strategies using observed completion/adherence, sleep, feedback, and weight trend relative to the user's goal.
4. It calculates an outcome/reward score for the strategies that were actually used and updates their per-user reward estimates using an online moving-average learning update.
5. The learned scores influence the next week's workout and diet strategies. New logs and the next review can update the model again.

### Persistence
- Main user/progress/log data: `~/.fitwise/fitwise_data.ser`
- Per-user learned model: `~/.fitwise/experience_model_user_<userId>.ser`

The model file is separate so upgrading the app does not require changing the existing serialized data format.

### Important details
- The first review establishes a baseline; learning from outcomes begins after the next review has a week of experience to evaluate.
- This is online reward-based learning from the user's own experience, not a model pretrained on a large medical or fitness dataset. It learns strategy reward estimates, not medical truths.
- Safety constraints take priority: reported discomfort forces low-impact/pain-free suggestions; hard difficulty, low energy, or poor sleep can trigger recovery recommendations; high hunger and poor meal adherence can trigger more satiating or simpler meals.
- Calorie changes are deliberately small and bounded. This is an educational prototype, not medical advice.

## Run on Windows
1. Install JDK 17 or newer.
2. Extract the ZIP.
3. Double-click `run.bat`.

The script compiles all Java source files and starts the Swing application.

## Backend-enabled version

A local Java REST backend is included in the `backend/` folder. It provides persistent account storage, salted PBKDF2 password hashing, login tokens, and an experience-history endpoint. Start `backend/run-backend.bat` first, then run the existing `run.bat` desktop app. The desktop registration/login screens authenticate against the backend. After authentication, the app retrieves the saved profile from `/api/profile`; if the profile is not present in the local desktop store, it restores the account profile locally. Existing local user IDs are preserved when refreshing profile fields so local progress remains linked to the account. The weekly experience review can also save a copy of the review to the backend. Detailed trackers and the per-user online-learning model still use local persistence, so this is a desktop app with a local REST backend—not a cloud-hosted, cross-device synchronized service. Allergy filtering is based on food names and common allergen aliases; it is not a substitute for checking ingredient labels or professional allergy guidance.

See `backend/README.md` for endpoints, data location, and run instructions.
