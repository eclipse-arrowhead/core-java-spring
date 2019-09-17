pipeline
{
	agent none

    environment
    {
        DOCKER_REPO_URL = "${env.DOCKER_REPO}"
        NAMESPACE = "${env.DOCKER_BUILD_NAMESPACE}"
        DOCKER_USER = "${env.DOCKER_USER}"
        DOCKER_USER_NAMESPACE = "${env.DOCKER_USER_NAMESPACE}"
        DOCKER_PASS = credentials('portus-push-password')
    }

	stages
	{
		stage( "Create Maven Cache" )
			{
				agent{ label "master" }
				steps
				{
					sh " docker volume create maven-repo "
				}
			}

		stage( "Test" )
            {
                agent { label "master" }
                steps
                {
                    sh '''
                       ./jenkins/copy/copy.sh
                       #./jenkins/test/maven.sh mvn test
                       '''
                }
            }

		stage( "Build" )
			{
				agent { label "master" }
				steps
				{
					sh  '''
					    ./jenkins/copy/copy.sh
					    ./jenkins/build/maven.sh mvn -B -DskipTests clean package
					    ./jenkins/build/build.sh
					    '''

				}
			}
		stage( "Push" )
        	{
        		agent { label "master" }
        	    steps
        		{
                    sh './jenkins/push/push.sh'
        		}
        	}
        stage( "Deploy" )
            {
            	agent { label "master" }
                steps
           		{
                    sh 'echo Deploy'
               	}
           	}
	}
}