#!/usr/bin/env bash

basedir=$(dirname $0)
version=$(cat version  | awk -F- {'print $1'})
instance_name=$(cat ${basedir}/instance_name)
nexusName=$(${basedir}/version/getNexusImageName.sh)
nexusRepo="http://localhost:8082/repository/releases/$instance_name/apk"

echo Pusblishing version ${version} of ${instance_name}
cd $WORKSPACE/app/release
source ~/.nexus_credential
for I in `ls | grep apk`;
do
	artifactName=$(echo $I | awk -F.apk {'print $1'})
	curl -v --user "$NexusUser":"$NexusPass" --upload-file I$  $nexusRepo/$artifactName-v$version.apk
done
