This is a Kotlin Multiplatform app that allows the user to create and manage checklists e.g. packing list, grocery list, etc. It runs on Android, iOS, Web, and Desktop.

* `/composeApp` contains UI code that will be shared across all Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if we are sharing the UI with Compose Multiplatform, 
  we need this entry point for our iOS app.

* `/server` is for the Ktor server application.

* `/shared` is for all non-UI code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. Similar to the UI code organization, we can have platform-specific code here too.