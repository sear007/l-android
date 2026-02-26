#!/bin/bash

##releasing standard ltlabs apk structure to nexus
##by chanthon.k@ltlabs.co
#Checkout

		#Close version
		currentVersion=$(cat version)
		if [[ "${currentVersion}" == *-SNAPSHOT ]]
		then
   		 newVersion=$(echo ${currentVersion} | cut -d\- -f1)
		 echo $newVersion > version

		 #build apk
		   basedir=$(dirname $0)
		   version=$(cat version  | awk -F- {'print $1'})
		   instance_name=$(cat ${basedir}/instance_name)
 		   echo Building version ${version} of ${instance_name}

		   if [ "$GIT_BRANCH" == "origin/alpha" ];then  
			   nexusRepo=alpha
			   echo $nexusRepo
		    elif [ "$GIT_BRANCH" == "origin/beta" ];then
			    nexusRepo=beta
                            echo $nexusRepo
		    elif [ "$GIT_BRANCH" == "origin/master" ];then
                            nexusRepo=master
                            echo $nexusRepo
		    fi		    

 		    chmod 755 gradlew 
		   ./gradlew assembleRelease
		  
	         	 #Release-APK
    		    if [ "$GIT_BRANCH" == "origin/alpha" ];then
                           nexusRepo=alpha
                           echo $nexusRepo
                    elif [ "$GIT_BRANCH" == "origin/beta" ];then
                            nexusRepo=beta
                            echo $nexusRepo
                    elif [ "$GIT_BRANCH" == "origin/master" ];then
                            nexusRepo=master
                            echo $nexusRepo
                    fi
		    	instance_name=$(cat ${basedir}/instance_name)
			version=$(cat version  | awk -F- {'print $1'})

	  	  	  nexusURL="http://localhost:8082/repository/releases/$instance_name/apk/$nexusRepo"
		          echo Pusblishing version ${version} of ${instance_name}
	        	  cd app/build/outputs/apk/release/
			  source ~/.nexus_credential

	 			for I in `ls | grep apk`;
				do
					artifactName=$(echo $I | awk -F.apk {'print $1'})
					curl  --user "$NexusUser:$NexusPass" --upload-file $I  $nexusURL/$artifactName-$nexusRepo-v$version.apk
				done
      

		 

		 #Finish release
		  cd $WORKSPACE
		  versionMinor=$(echo ${currentVersion} | rev | cut -d\. -f1 | rev)
		  versionMajor=${currentVersion: 0: -${#versionMinor} }
		  newVersion=${versionMajor}$((versionMinor+1))-SNAPSHOT
		  echo ${newVersion} > version                
                  git add version
                  git commit -m "JENKINS: Created new snapshot version"
                  git push origin HEAD:$nexusRepo
    		
		else
   			 echo "Current version is not a snapshot, can't be closed"
    			 exit 1
	        fi
fi

