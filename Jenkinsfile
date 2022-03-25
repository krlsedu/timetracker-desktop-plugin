#!groovy

pipeline {
  agent none
  stages {
    stage('Build') {

      agent any
        tools {
                maven 'M3'
            }
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
            sh 'git pull origin master'
            sh 'mvn versions:set versions:commit -DnewVersion='+TAG
            echo "5"
            echo "2"
//                 sh 'mvn clean install'
            echo "3"
            sh "git add ."
            echo "4"
            sh "git config --global user.email 'krlsedu@gmail.com'"
            sh "git config --global user.name 'Carlos Eduardo Duarte Schwalm'"
            sh "git commit -m 'Triggered Build: "+TAG+"'"
            sh 'git show-ref'
            withCredentials([usernamePassword(credentialsId: 'github_global', passwordVariable: 'password', usernameVariable: 'user')]) {
              sh 'git push https://${user}:${password}@github.com/${user}/timetracker-desktop-plugin.git'
            }
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