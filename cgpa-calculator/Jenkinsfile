// Jenkins Declarative Pipeline for the CGPA calculator
// Written for a Windows Jenkins agent (uses `bat` steps).
// Set DOCKERHUB_USER below, and create a Jenkins credential of type
// "Username with password" with ID "dockerhub-creds" (Docker Hub
// username + an access token, not your account password).

pipeline {
    agent any

    environment {
        DOCKERHUB_USER = 'yourdockerhubuser'       // <-- change this
        IMAGE_NAME     = 'cgpa-calculator'
        IMAGE_TAG      = "${env.BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat "docker build -t %DOCKERHUB_USER%/%IMAGE_NAME%:%IMAGE_TAG% -t %DOCKERHUB_USER%/%IMAGE_NAME%:latest ."
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS')]) {
                    bat 'echo %DOCKER_PASS%| docker login -u %DOCKER_USER% --password-stdin'
                    bat "docker push %DOCKERHUB_USER%/%IMAGE_NAME%:%IMAGE_TAG%"
                    bat "docker push %DOCKERHUB_USER%/%IMAGE_NAME%:latest"
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded: ${DOCKERHUB_USER}/${IMAGE_NAME}:${IMAGE_TAG} pushed to Docker Hub."
        }
        failure {
            echo 'Pipeline failed — check the stage logs above.'
        }
        always {
            bat 'docker logout'
        }
    }
}
