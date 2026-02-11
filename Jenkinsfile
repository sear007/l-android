pipeline {
    agent any
    stages {
        stage('Verify changes') {
            steps {
                sh "git log -1 | grep Author | awk '{print \$2}' > lastCommitAuthor.txt "
                script {
                    lastCommitAuthor = readFile("lastCommitAuthor.txt").trim()
                    if ( lastCommitAuthor == "jenkins.a" ) {
                        echo "There are no changes since the last release. Aborting"
                        currentBuild.result = 'ABORTED'
                        error('Stopping early…')
                    } else {
                        echo "Changes detected since last release. Last commit made by ${lastCommitAuthor}. Proceeding."
                    }
                }
            }
        }
        stage('Run build') {
            steps {
                sh "./scripts/apk-build.sh"
            }
        }
        stage('Artifact release') {
            steps {
                sh "./scripts/apk-release.sh"
            }
        }
	stage('Finish release') {
            steps {
                sh "./scripts/apk-close-release.sh"
            }
        }
    }
    post {
        always {
	    cleanWs()
            emailext body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}\n More info at: ${env.BUILD_URL}",
                to: 'chanthoan.k@ltlabs.co,jimmy.p@ltlabs.co,khorn.s@ltlabs.co,channarith.b@ltlabs.co',
                subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}"
            
        }
    }
}
