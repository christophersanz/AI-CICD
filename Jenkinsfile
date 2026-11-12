// Pipeline para un Jenkins local de práctica (ADR-0010, ADR-0014). El CI oficial es GitHub Actions (ADR-0009).
// Requiere JDK 21, jq y acceso a Docker en el agente: Testcontainers, docker build (con BuildKit) y los
// contenedores de la verificación y del despliegue local (docker/jenkins/).
// Cada etapa es una columna del Stage View: si algo falla, se ve en qué paso.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    // Jenkins corre en localhost y GitHub no puede llamarlo con un webhook: revisa el repositorio cada
    // ~5 min y construye si hay commits nuevos. El job de docker/jenkins/casc.yaml declara el mismo sondeo para que
    // siga activo cuando JCasC recrea el job al reiniciar Jenkins; mantener ambos con la misma expresión.
    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        // Caché de dependencias persistente en el volumen de Jenkins.
        MVN = "./mvnw -B -ntp -Dmaven.repo.local=${env.JENKINS_HOME}/.m2/repository"
        IMAGE = "contract-api:jenkins-${env.BUILD_NUMBER}"
    }

    stages {
        // Instala domain y application en la caché local para que las etapas siguientes trabajen por módulo.
        stage('Compilar') {
            steps {
                sh 'chmod +x mvnw docker/jenkins/scripts/*.sh .github/scripts/*.sh'
                sh '$MVN clean install -DskipTests -Djacoco.skip=true'
            }
        }

        stage('Pruebas unitarias') {
            steps {
                sh '$MVN -pl domain,application test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'domain/target/surefire-reports/TEST-*.xml, application/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        // Slices web, seguridad y persistencia contra PostgreSQL real con Testcontainers.
        stage('Pruebas de integración') {
            steps {
                sh '$MVN -pl infrastructure test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'infrastructure/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        // Informe JaCoCo y umbral del 80 % de líneas en domain y application (infrastructure solo informa).
        stage('Cobertura') {
            steps {
                sh '$MVN jacoco:report@report jacoco:check@check'
            }
            post {
                always {
                    archiveArtifacts artifacts: '**/target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Imagen Docker') {
            steps {
                sh 'DOCKER_BUILDKIT=1 docker build -t "$IMAGE" .'
            }
        }

        // Entorno efímero: la imagen arranca, Flyway migra y la prueba de humo pasa; después se retira todo.
        stage('Verificación de la imagen') {
            steps {
                sh 'docker/jenkins/scripts/verify-image.sh "$IMAGE"'
            }
        }

        // Entorno persistente en esta máquina (http://localhost:8090 por defecto). Se omite si
        // docker/jenkins/.env no define DEPLOY_ADMIN_PASSWORD.
        stage('Despliegue local') {
            when {
                expression { env.DEPLOY_ADMIN_PASSWORD?.trim() }
            }
            steps {
                sh 'docker/jenkins/scripts/deploy-local.sh "$IMAGE"'
            }
        }
    }
}
