#!/bin/bash

User="ltie"
testEnv="dev-ltie.ltlabs.co"
prodEnv="ltie.ltlabs.co"


basedir=$(dirname $0)
version=$(cat version  | awk -F- {'print $1'})
instance_name=$(cat ${basedir}/instance_name)
nexusName=$(${basedir}/version/getNexusImageName.sh)


checkPassword(){
    pwdFile=~/deploy-password/ltie-${environment}.password
    if [ -f  ${pwdFile} ]; then
        echo "Environment is password protected. Verifying..."
        environmentPassword=$(cat ${pwdFile})
        if [ "${environmentPassword}" != "$password" ]; then
            echo "****************************************"
            echo "Password is invalid for this environment."
            #echo "Provided password: $password"
            #echo "Required password: $environmentPassword"
            echo "****************************************"
            exit 1;
        else
            echo "Password validation successful."
        fi
    else
        echo "Environment is not password protected."
    fi
}
environment="$1"
password=$(echo -n  $PASSWORD | md5sum | awk '{print $1}')
echo $password
##check password
checkPassword

serverAddress=""

if [ "$environment" == "test" ]; then
   serverAddress="$testEnv"
elif [ "$environment" == "staging" ]; then
    serverAddress="$stageEnv"
elif [ "$environment" == "production" ]; then
    serverAddress="$prodEnv"
else
    echo "invalid environment"
    exit 1;
fi
echo "***********************************"
echo "deploying to $environment environment - $serverAddress"
echo "cd /opt/scripts && bash ltie-deployment.sh auth  ${instance_name}-${version}.tar.gz deploy" | ssh $User@$serverAddress  
echo "***********************************"

