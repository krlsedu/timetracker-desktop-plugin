pipeline {
  agent any
  tools {
          maven 'M3'
      }
  stages {
    stage('Build') {
      steps {
        def TAG = VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
        sh 'mvn versions:set versions:commit -DnewVersion=$TAG'
        sh 'mvn clean install'
        sh "git add ."
        sh "git commit -m 'Triggered Build: $TAG'"
        sh 'git push origin '+env.BRANCH_NAME
      }
    }

  }
}