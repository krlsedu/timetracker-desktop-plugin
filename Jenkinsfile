pipeline {
  agent any
  tools {
          maven 'M3'
      }
  environment {
          MY_VERSION = ''
  }
  stages {
    stage('Build') {
      steps {
        env.MY_VERSION = '0.0.1'
        sh 'mvn versions:set versions:commit -DnewVersion=$TAG'
        sh 'mvn clean install'
        sh "git add ."
        sh "git commit -m 'Triggered Build: $MY_VERSION'"
        sh 'git push origin '+env.BRANCH_NAME
      }
    }

  }
}