matrix-android-sdk
[![](https://jitpack.io/v/org.futo.gitlab.circles/matrix-android-sdk.svg)](https://jitpack.io/#org.futo.gitlab.circles/matrix-android-sdk)

To add remote upstream
git remote add upstream https://github.com/matrix-org/matrix-android-sdk2.git

To update upstream from origin sdk repo:
git pull upstream develop --allow-unrelated-histories

To publish new build on Jitpack:
- change version in :matrix-sd-android publishing{}
- change version in jitpack.yml install script
./gradlew assembleRelease
- move martix-sdk-android-release.aar to root directory
./gradlew :matrix-sdk-android:generatePomFileForReleasePublication
- move pom-default.xml to root directory
- rename to pom.xml
- remove <packaging>aar</packaging> from pom.xml
- create tag
- build new artifact on https://jitpack.io/
