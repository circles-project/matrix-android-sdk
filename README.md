matrix-android-sdk

To add remote upstream
git remote add upstream https://github.com/matrix-org/matrix-android-sdk2.git

To update upstream from origin sdk repo:
git pull upstream develop --allow-unrelated-histories

To publish new build into Gitlab Package Registry:
./gradlew assembleRustCryptoRelease
./gradlew publish

PUBLISH_TOKEN variable in local.properties required to publish release to Gitlab
