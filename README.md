matrix-android-sdk
[![](https://jitpack.io/v/org.futo.gitlab.circles/matrix-android-sdk.svg)](https://jitpack.io/#org.futo.gitlab.circles/matrix-android-sdk)

To add remote upstream
git remote add upstream https://github.com/matrix-org/matrix-android-sdk2.git

To update upstream from origin sdk repo:
git pull upstream develop --allow-unrelated-histories

To publish new build on Jitpack:

- change version in gradle.properties
- run pre_release.sh
- create tag
- build new artifact on https://jitpack.io/

MavenLocal testing:

in circles-rust-components-kotlin project:

- change publishing version in module level build.gradle (at very bottom of build.gradle file in "publishing{}" scope. Version could be any string, should be
  different than published on Jitpack)
- ./gradlew :crypto-android:publishReleasePublicationToMavenLocal

in matrix-android-sdk project:

- switch release/test dependencies in module level build.gradle file:
  //api("org.futo.gitlab.circles:circles-rust-components-kotlin:v0.3.15.8@aar") {
  // transitive = true
  // }
  api "org.futo.gitlab.circles:crypto-android:0.1"
  (use version you set in crypto module before instead of 0.1)
- change publishing version in module level build.gradle (same way as for crypto module before)
- ./gradlew :matrix-sdk-android:publishReleasePublicationToMavenLocal

in circles-android project:

- switch release/test dependencies in build.gradle file for module core:
  //api('org.futo.gitlab.circles:matrix-android-sdk:v1.6.10.35@aar') {
  //transitive = true
  //}
  api "org.futo.gitlab.circles:matrix-android-sdk:0.1"
  (use version you set in matrix-sdk module before instead of 0.1)
