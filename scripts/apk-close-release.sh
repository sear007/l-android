#!/bin/bash

#Finish release
echo "closing env.."
#cd $WORKSPACE
releasedVersion=$(cat version  | awk -F- {'print $1'})
versionMinor=$(cat version | rev | cut -d\. -f1 | rev | awk -F- {'print $1'})
versionMajor=$(cat version | rev  | cut -d\. -f2- | rev)
versionMinorIncreasing=$(echo $versionMinor + 1 | bc)
newVersion="$versionMajor.$versionMinorIncreasing"
echo $newVersion-SNAPSHOT > version

#branch=$(echo "$GIT_BRANCH" | awk -F/ {'print $2'})

git add version
git commit -m "JENKINS: Generated ${releasedVersion} version"
git push origin HEAD:$GIT_BRANCH

