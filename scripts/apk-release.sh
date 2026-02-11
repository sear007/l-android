#!/bin/bash


    	versionMinor=$(cat version | rev | cut -d\. -f1 | rev | awk -F- {'print $1'})
    	versionMajor=$(cat  app/build.gradle  | grep versionName | awk {'print $2'} | head -1 | sed 's/"//g')
    	version=${versionMajor}.${versionMinor} 
    	instance_name=$(cat scripts/instance_name)
	artifactName=$(cat scripts/instance_name | awk -F- {'print $1'})
  	nexusURL="https://nexus.ltlabs.co/repository/apk-releases/$instance_name/$GIT_BRANCH"
	nexusPubURL="https://d1nhq76y9x8aou.cloudfront.net/$instance_name/$GIT_BRANCH"
	apkDir="app/build/outputs/apk/release"
	DATE=$(date '+%Y-%m-%d %H:%M:%S')
	appName="LTm"
	dbName="stxltmdb"

	 if [ "$versionMajor" != "$(cat version  | awk -F- {'print $1'} | cut -c1-7)" ];then
		version="$versionMajor.000"
		echo "$version-SNAPSHOT" > version
	fi
        
          echo Pusblishing version ${version} of ${instance_name}
	  cd $apkDir
	  source ~/.nexus_credential
	  for I in `ls | grep release.apk`;
	  do
	     if [ "$GIT_BRANCH" == "master"  ];then
	      	curl  --user "$NexusUser:$NexusPass" --upload-file $I  $nexusURL/$artifactName-v$versionMajor.apk
	     else
	     	curl  --user "$NexusUser:$NexusPass" --upload-file $I  $nexusURL/$artifactName-v$version-$GIT_BRANCH.apk
	     fi
	  done

