matrix-android-sdk
[![](https://jitpack.io/v/org.futo.gitlab.circles/matrix-android-sdk.svg)](https://jitpack.io/#org.futo.gitlab.circles/matrix-android-sdk)

To add remote upstream
git remote add upstream https://github.com/matrix-org/matrix-android-sdk2.git

To update upstream from origin sdk repo:
git pull upstream develop --allow-unrelated-histories

To publish new build on Jitpack:
./gradlew assembleRelease
./gradlew publish
