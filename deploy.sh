#!/bin/bash
# ------------------------------------------------------------------
# [Author] Raysmond
#
#          This script automatically deploy the Spring Boot
#          application to remote server.
#
# Dependency:
#     python
# ------------------------------------------------------------------

java -version
node -v

# Get application version from build.gradle
version=$(cat build.gradle |  grep "version = \"[0-9\.]")
tmp=${version#*\"}
version=${tmp%\-*}

# Automatically increment application version
# For example: 0.1.6 -> 0.1.7
increment_version ()
{
  declare -a part=( ${1//\./ } )
  declare    new
  declare -i carry=1

  for (( CNTR=${#part[@]}-1; CNTR>=0; CNTR-=1 )); do
    len=${#part[CNTR]}
    new=$((part[CNTR]+carry))
    [ ${#new} -gt $len ] && carry=1 || carry=0
    [ $CNTR -gt 0 ] && part[CNTR]=${new: -len} || part[CNTR]=${new}
  done
  new="${part[*]}"
  version="${new// /.}"
}

increment_version $version
sed -i -e "s/^version\ =\ .*/version\ =\ \"$version\-SNAPSHOT\"/g" build.gradle

./gradlew  -Dorg.gradle.jvmargs=-Xmx10240m -Pprod clean bootJar

# prod
scp -P 2789 build/libs/*.jar kaidun@223.108.21.108:/opt/deploy/hmfy/releases/
