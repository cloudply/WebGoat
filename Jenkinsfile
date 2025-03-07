//noinspection GroovyAssignabilityCheck
pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: "10"))
    }
    agent any
    tools {
        jdk 'jdk21'
        maven 'maven'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn spotless:apply install  -Dexec.skip=true -Dspotless.check.skip=true -DskipITs'
            }
        }
    }
}
