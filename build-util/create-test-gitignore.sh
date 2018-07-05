#!/bin/sh
# Script to create .gitignore files in test folders that do not have
# Run this from the test/regression/commands folder.

#ls -1 -d  * | 

for dir in *; do
	echo ${dir}
	# First create the Results folder
	if [ -e "${dir}/Results" ]
		then
		echo "Have folder ${dir}/Results"
	else
		echo "No folder ${dir}/Results ... creating..."
		mkdir ${dir}/Results
	fi
	# Then create the Results/.gitignore file with correct contents
	gitIgnoreFile="${dir}/Results/.gitignore"
	if [ -e "${gitIgnoreFile}" ]
		then
		echo "Have file ${gitIgnoreFile}"
	else
		echo "No file ${gitIgnoreFile} ... creating..."
		echo '# Ignore all files in this folder except the .gitignore file' > ${gitIgnoreFile}
		echo '/*' >> ${gitIgnoreFile}
		echo '!.gitignore' >> ${gitIgnoreFile}
	fi
done
