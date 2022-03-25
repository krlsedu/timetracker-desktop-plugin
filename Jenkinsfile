pipeline {
  agent any
  tools {
          maven 'M3'
      }
  stages {
    stage('Build') {
      steps {
        env.myVersion = VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
        sh 'mvn versions:set versions:commit -DnewVersion=$TAG'
        sh 'mvn clean install'
        sh "git add ."
        sh "git commit -m 'Triggered Build: $env.myVersion'"
        sh 'git push origin '+env.BRANCH_NAME
      }
    }

  }
}