#!groovy
env.RELEASE_COMMIT = "1";

pipeline {
    agent none
    stages {
        stage('CheckBranch') {
            agent any
            steps {
                script {
                    result = sh(script: "git log -1 | grep 'Triggered Build'", returnStatus: true)
                    echo 'result ' + result
                    env.RELEASE_COMMIT = result == 0 ? '0' : '1'
                }
            }
        }
        stage('Build') {
            agent any
            tools {
                maven 'M3'
            }
            when {
                expression { env.RELEASE_COMMIT != '0' }
            }
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Tests') {
            agent any
            tools {
                maven 'M3'
            }
            when {
                expression { env.RELEASE_COMMIT != '0' }
            }
            steps {
                sh 'mvn test'
            }
        }
        stage('Gerar versão') {
            agent any
            tools {
                maven 'M3'
            }
            when {
                expression { env.RELEASE_COMMIT != '0' }
            }
            steps {
                script {
                    echo 'RELEASE_COMMIT ' + env.RELEASE_COMMIT
                    if (env.BRANCH_NAME == 'master') {
                        echo 'Master'
                        PRE_RELEASE = ''
                        VERSION = VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yy"}.${BUILDS_THIS_YEAR,XXX}.${BUILDS_TODAY,XXX}')
                        TAG = 'Release-' + VERSION
                    } else {
                        echo 'Dev'
                        PRE_RELEASE = ' --pre-release'
                        VERSION =  VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
                        TAG = 'Alpha-' + VERSION
                    }

                    echo "removing old files"
                    sh 'rm -rf target'
                    sh 'rm -rf csctracker-desktop-plugin.zip'

                    echo "Creating a new tag"
                    sh 'git pull origin master'
                    sh 'mvn versions:set versions:commit -DnewVersion=' + VERSION
                    sh 'mvn clean install'

                    echo "Compressing artifacts into one file"
                    sh 'zip -j csctrackerDesktopPlugin.zip target/*.jar target/classes/*.*'

                    withCredentials([usernamePassword(credentialsId: 'gitHub', passwordVariable: 'password', usernameVariable: 'user')]) {
                        script {
                            sh "git add ."
                            sh "git config --global user.email 'krlsedu@gmail.com'"
                            sh "git config --global user.name 'Carlos Eduardo Duarte Schwalm'"
                            sh "git commit -m 'Triggered Build: " + VERSION + "'"
                            sh 'git push https://krlsedu:${password}@github.com/krlsedu/timetracker-desktop-plugin.git HEAD:' + env.BRANCH_NAME

                            echo "Creating a new release in github"
                            sh 'github-release release --user krlsedu --security-token ' + env.password + ' --repo timetracker-desktop-plugin --tag ' + TAG + ' --name "' + TAG + '"' + PRE_RELEASE

                            echo "Uploading the artifacts into github"
                            sleep(time: 3, unit: "SECONDS")

                            sh 'github-release upload --user krlsedu --security-token ' + env.password + ' --repo timetracker-desktop-plugin --tag ' + TAG + ' --name csctrackerDesktopPlugin-"' + TAG + '.zip" --file csctrackerDesktopPlugin.zip'

                        }
                    }
                }
            }
        }
    }
}
