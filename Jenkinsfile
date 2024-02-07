node ('master') {
    def uniqueId = UUID.randomUUID().toString()
    def imageName = 'ubuntu:19.10' //can be any image you want to execute

    podTemplate(
        name: env.jaas_owner + '-jaas',
        label: uniqueId,
        containers: [
            // Custom JNLP docker slave needs to be defined in each podTemplate
            containerTemplate(
                name: 'jnlp',
                image: 'docker.wdf.sap.corp:50001/sap-production/jnlp-alpine:3.26.1-sap-02',
                args: '${computer.jnlpmac} ${computer.name}'
            ),

            containerTemplate (
                name: 'container-exec',
                image: imageName,
                // The container needs a long-running command to stay alive
                // until all containers in the pod are pulled and started.
                // Hence a pre-configured ENTRYPOINT in a docker images
                // will be overwritten. This needs to be considered for the
                // execution of the shell block in the container.
                command: '/usr/bin/tail -f /dev/null',
            )
        ]
    )
    {
        node (uniqueId) {
            echo "Execute container content in Kubernetes pod"
            container('container-exec') {

                stage('Checkout') {
                    git 'https://github.wdf.sap.corp/WebSecResearch/java-bytecode-tainter'
                }

                stage('Slave Prepare') {
                    // unstash content from Jenkins master workspace
                    sh "apt-get update && apt-get install -y python3 openjdk-11-jdk-headless && rm -rf /var/lib/apt/lists/*"
                }

                stage('Build') {
                    sh "./gradlew -s assemble"
                    sh "./gradlew -s check"
                    sh "./gradlew -s publishToMavenLocal"
                }
                
                stage('Publish') {
                    junit '**/build/test-results/**/TEST-*.xml'
                    archiveArtifacts artifacts: 'build/libs/fontus-*.jar, build/publications/main/pom-default.xml', fingerprint: true
                }

                stage('Test') {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        dir("tests") {
                            dir("jars") {
                                sh "bash build.sh"    
                            }
                            sh "chmod +x run_system_tests.py"
                            sh "python3 run_system_tests.py --build-first --taint_type=boolean"
                            sh "python3 run_system_tests.py --build-first --taint_type=range"
                        }
                    }
                }
            }
        }
    }
}
