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
                sh 'mvn clean install'
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
        stage('Gerar versão') {
            agent any
            tools {
                maven 'M3'
            }
            steps {
                script {
                    result = sh(script: "git log -1 | grep 'Triggered Build'", returnStatus: true)
                    echo 'result ' + result
                    if (result != 0) {
                        if (env.BRANCH_NAME == 'master') {
                            echo 'Master'
                            PRE_RELEASE = ''
                            TAG = VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
//                        TAG = '20221013.1.1'
                        } else {
                            echo 'Dev'
                            PRE_RELEASE = ' --pre-release'
                            TAG = 'Alpha-' + VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
                        }

                        sh 'git pull origin master'
                        sh 'mvn versions:set versions:commit -DnewVersion=' + TAG
                        sh 'mvn clean install'

                        echo "Compressing artifacts into one file"
                        sh 'zip -r csctracker-desktop-plugin.zip target'

                        withCredentials([usernamePassword(credentialsId: 'gitHub', passwordVariable: 'password', usernameVariable: 'user')]) {
                            script {
                                result = sh(script: "git log -1 | grep 'Triggered Build'", returnStatus: true)
                                echo 'result ' + result
                                if (env.BRANCH_NAME == 'master' && result != 0) {
                                    echo "Creating a new release in github"
                                    sh 'github-release release --user krlsedu --security-token ' + env.password + ' --repo timetracker-desktop-plugin --tag ' + TAG + ' --name "' + TAG + '"' + PRE_RELEASE

                                    echo "Uploading the artifacts into github"
                                    sleep(time: 3, unit: "SECONDS")

                                    sh 'github-release upload --user krlsedu --security-token ' + env.password + ' --repo timetracker-desktop-plugin --tag ' + TAG + ' --name csctracker-desktop-plugin-"' + TAG + '.zip" --file csctracker-desktop-plugin.zip'

                                    sh "git add ."
                                    sh "git config --global user.email 'krlsedu@gmail.com'"
                                    sh "git config --global user.name 'Carlos Eduardo Duarte Schwalm'"
                                    sh "git commit -m 'Triggered Build: " + TAG + "'"
                                    sh 'git push https://krlsedu:${password}@github.com/krlsedu/timetracker-desktop-plugin.git HEAD:' + env.BRANCH_NAME
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
