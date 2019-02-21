#!/bin/sh
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required
# The above line ensures that the script can be run on Cygwin/Linux even with Windows CRNL
#
# git-clone-all-statedmi - clone all StateDMI repositories for new development environment setup
# - this script calls the general git utilities script

# Get the location where this script is located since it may have been run from any folder
scriptFolder=`cd $(dirname "$0") && pwd`

# Git utilities folder is relative to the user's files in a standard development files location
# - determine based on location relative to the script folder
# Specific repository folder for this repository
repoFolder=`dirname ${scriptFolder}`
# Want the parent folder to the specific Git repository folder
gitReposFolder=`dirname ${repoFolder}`

# StateDMI GitHub repo URL root
githubRootUrl="https://github.com/OpenCDSS"

# Main StateDMI repository
mainRepo="cdss-app-statedmi-main"

# Run the general script
${scriptFolder}/git-util/git-clone-all.sh -m "${mainRepo}" -g "${gitReposFolder}" -u "${githubRootUrl}" $@
