pipeline
{
	agent any

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
                       ./jenkins/test/maven.sh mvn test
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
	}
}