matrix-android-sdk

To update upstream from origin sdk repo:
git pull upstream develop --allow-unrelated-histories

To publish new build into Gitlab Package Registry:
./gradlew assembleRelease
./gradlew publish
