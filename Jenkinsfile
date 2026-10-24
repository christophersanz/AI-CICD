// Pipeline para un Jenkins local de práctica (ADR-0010). El CI oficial es GitHub Actions (ADR-0009).
// Requiere JDK 21 y acceso a Docker en el agente: Testcontainers y docker build (con BuildKit).
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    // Jenkins corre en localhost y GitHub no puede llamarlo con un webhook: revisa el repositorio cada
    // ~5 min y construye si hay commits nuevos. Se registra al terminar el primer build con este Jenkinsfile.
    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        // Caché de dependencias persistente en el volumen de Jenkins.
        MAVEN_REPO = "${env.JENKINS_HOME}/.m2/repository"
    }

    stages {
        stage('Build, pruebas y cobertura') {
            steps {
                sh 'chmod +x mvnw && ./mvnw -B -ntp -Dmaven.repo.local="$MAVEN_REPO" clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/*-reports/TEST-*.xml'
                    archiveArtifacts artifacts: '**/target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Imagen Docker') {
            steps {
                sh 'DOCKER_BUILDKIT=1 docker build -t contract-api:jenkins-${BUILD_NUMBER} .'
            }
        }
    }
}
