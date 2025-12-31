def call (Map configmap) {
    pipeline {
        agent {
            node {
                label 'Agent'
            }
        }
        options {
            // timeout(time: 10, unit: 'SECONDS') 
            disableConcurrentBuilds()
        }
        environment {
            COURSE = "DevOps"
            appVersion = "" // defining here empty and can be used for all stages
            ACC_ID = "799345568171"
            project = configmap.get('project')
            component = configmap.get('component')
        }
        stages {
            stage('Read Version') { // This is build stage as example
                steps{
                    script {  //using scripts here is a hybrid approach
                        def packageJSON = readJSON file: 'package.json'
                        appVersion = packageJSON.version
                        echo "appVersion is ${appVersion}"
                    }
                }
            }
            stage('Install Dependencies'){ // This is a test stage
                steps {
                    script {
                        sh """
                            npm install --inlude=dev
                        """
                    }
                }
            }
            // After installing dependencies we have to run test cases and send report to sonarqube server
            stage('unit test'){ // This is a test stage
                steps {
                    script {
                        sh """
                            npm test
                        """
                    }
                }
            }
            //static source code analysis and SAST
            // stage('Code Analysis') {
            //     environment { // this block is to select the version of sonar tool
            //         def scannerHome = tool 'sonar' //name should be same
            //     }
            //     steps {
            //         script {
            //             withSonarQubeEnv('sonar') { //this block gets the sonar sever access 
            //             // also this sonar-scnner tools will get access read code and send report to server using host url and token
            //                 sh """
            //                     ${scannerHome}/bin/sonar-scanner \
            //                 """
            //             }
            //         }
            //     }
            // }
            // stage('Quality Gate') {
            //     steps {
            //         timeout(time: 1, unit: 'HOURS') {
            //             waitForQualityGate abortPipeline: true
            //         }
            //     }
            // }
            // stage('Dependabot Vulnerability Check') {
            //     environment {
            //         GITHUB_TOKEN = credentials('git-auth')
            //         OWNER = 'akhilnaidu1997'
            //         REPO  = 'catalogue'
            //     }
            //     steps {
            //         script {
            //             echo "🔍 Checking Dependabot alerts for ${OWNER}/${REPO}"

            //             def response = sh(
            //                 script: """
            //                 curl -s -H "Authorization: Bearer ${GITHUB_TOKEN}" \
            //                     -H "Accept: application/vnd.github+json" \
            //                     https://api.github.com/repos/${OWNER}/${REPO}/dependabot/alerts
            //                 """,
            //                 returnStdout: true
            //             ).trim()

            //             writeFile file: 'dependabot-alerts.json', text: response

            //             def count = sh(
            //                 script: """
            //                 jq '[.[] | select(
            //                     .state=="open" and
            //                     (.security_advisory.severity=="high" or
            //                     .security_advisory.severity=="critical")
            //                 )] | length' dependabot-alerts.json
            //                 """,
            //                 returnStdout: true
            //             ).trim()

            //             if (count.toInteger() > 0) {
            //                 error "❌ BLOCKING PIPELINE: ${count} HIGH/CRITICAL open Dependabot vulnerabilities found"
            //             } else {
            //                 echo "✅ No HIGH or CRITICAL open vulnerabilities found. Proceeding..."
            //             }
            //         }
            //     }
            // }

            // stage('Build Image'){ // This is a deploy stage for practice
            //     steps {
            //         script {
            //             withAWS(credentials: 'aws-auth') {
            //                 sh """
            //                     aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com
            //                     docker build -t ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${project}/${component}:${appVersion} .
            //                     docker push ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${project}/${component}:${appVersion}
            //                 """
            //             }
            //         }
            //     }
            // }
            // stage('Trivy OS Scan (Local Image)') {
            //     environment {
            //         IMAGE = "${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${project}/${component}:${appVersion}"
            //     }
            //     steps {
            //         script {
            //             echo "🔍 Running Trivy OS-only scan on local image: ${IMAGE}"
            //             sh """
            //                 trivy image \
            //                     --vuln-type os \
            //                     --severity HIGH,CRITICAL \
            //                     --exit-code 1 \
            //                     ${IMAGE}
            //             """

            //             echo "✅ Trivy scan passed — no HIGH or CRITICAL OS vulnerabilities"
            //         }
            //     }
            // }
        }
        
        post {
            always {
                echo 'I will say hello again regardless of build result'
                cleanWs()
            }
            success {
                echo "I will run this if build is success"
            }
            // aborted {
            //     withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
            //             sh '''
            //             curl -X POST -H 'Content-type: application/json' \
            //             --data '{"text":"Build Failed ✅"}' \
            //             $SLACK_URL
            //             '''
            //     }
            // }
            // failure {
            //     withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
            //             sh '''
            //             curl -X POST -H 'Content-type: application/json' \
            //             --data '{"text":"Build Failed ✅"}' \
            //             $SLACK_URL
            //             '''
            //     }
            // }
            changed {
                echo "I will run this if pipeline status is changed"
            }
        }
    }

}