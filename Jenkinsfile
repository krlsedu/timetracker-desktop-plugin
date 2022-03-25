#!groovy

pipeline {
  agent none
  stages {
    stage('Build') {
          steps {
            script {
                if (env.BRANCH_NAME == 'master') {
                    echo 'Master'
                    TAG = VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
                } else {
                    echo 'Dev'
                    TAG = 'Alpha-'+VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
                }
            }
            echo "${TAG}"
            sh 'mvn versions:set versions:commit -DnewVersion='+${TAG}
            echo "2"
            sh 'mvn clean install'
            echo "3"
            sh "git add ."
            echo "4"
            sh "git commit -m 'Triggered Build: "+${TAG}+"'"
            echo "5"
            sh 'git push origin '+env.BRANCH_NAME
          }
        }
    stage('Tests') {
      agent any
        tools {
                maven 'M3'
            }
      steps {
        sh 'mvn test'
      }
    }
  }
}