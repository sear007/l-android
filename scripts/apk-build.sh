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
		   instance_name=$(cat scripts/instance_name)
 		   echo Building version ${version} of ${instance_name}

		   if [ "$GIT_BRANCH" == "alpha" ];then  
			   nexusRepo=alpha
			   API_EndPoint="suntex-dev.ltlabs.co"
			   echo $nexusRepo
		    elif [ "$GIT_BRANCH" == "beta" ];then
			    nexusRepo=beta
			    API_EndPoint="staging.ltlabs.co"
                            echo $nexusRepo
		    elif [ "$GIT_BRANCH" == "training" ];then
                            nexusRepo=training
                            API_EndPoint="training.ltlabs.co"
                            echo $nexusRepo
		    elif [ "$GIT_BRANCH" == "master" ];then
                            nexusRepo=master
			    API_EndPoint="suntex.ltlabs.co"
                            echo $nexusRepo
		    fi		   
	  	    export JAVA_HOME="/opt/jdk-11.0.2"
 		    chmod 755 gradlew
		    ##Reduce memory for gradle build
		    sed -i "s/Xmx2048m/Xmx512m/g" gradle.properties 
		    ##Change endpoint api per environment
		    sed -i "s/suntex-dev.ltlabs.co/$API_EndPoint/g"  app/src/main/java/co/ltlabs/ltmechanic/constant/AppConfig.kt
		   ./gradlew assembleRelease
		 else
   			 echo "Current version is not a snapshot, can't be closed"
    			 exit 1
	        fi

