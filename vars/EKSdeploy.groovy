def call (Map configmap){
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
            appVersion = configmap.get("appVersion") // defining here empty and can be used for all stages
            ACC_ID = "799345568171"
            project = configmap.get("project")
            component = configmap.get("component")
            REGION = "us-east-1"
            ENVIRONMENT = configmap.get("environment")
        }
        // parameters {
        //     string(name: 'appVersion', description: 'which version to deploy')
        //     choice(name: 'Environment', choices: ['dev', 'qa', 'prod'])
        // }
        stages{
            stage('Deploy'){ // This is a test stage
                steps {
                    script {
                        withAWS(region = "us-east-1", credentials = "aws-auth"){
                            sh """
                                aws eks update-kubeconfig --region ${REGION} --name ${project}-${ENVIRONMENT}
                                kubectl get nodes
                            """
                        }
                    }
                }
            }
        }
        
        post {
            always {
                echo 'I will say hello again regardless of build result'
                cleanWs()
            }
            success {
                echo "I will run this if build is success"
            }
            changed {
                echo "I will run this if pipeline status is changed"
            }
        }
    }

}