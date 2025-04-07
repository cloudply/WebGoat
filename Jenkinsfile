//noinspection GroovyAssignabilityCheck
pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: "10"))
    }
    agent {
        label 'k8s'  // This should match the label of your Kubernetes pod template
    }
    tools {
        jdk 'jdk21'
        maven 'maven'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn spotless:apply install  -Dexec.skip=true -Dspotless.check.skip=true -DskipITs -Dsurefire.excludes=**/BlindSendFileAssignmentTest*'
            }
        }
    }
}
