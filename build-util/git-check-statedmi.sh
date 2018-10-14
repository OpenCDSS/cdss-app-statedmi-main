#!/bin/sh
#
# git-check-statedmi - check the StateDMI repositories for status
# - this script calls the general git utilities script

# StateDMI product home is relative to the user's files in a standard CDSS development files location
# - $HOME/${productHome}
productHome="cdss-dev/StateDMI"

# Main StateDMI repository
mainRepo="cdss-app-statedmi-main"

# TODO smalers 2018-10-12 The following may need to be made absolute to run from any folder
git-util/git-check.sh -m "${mainRepo}" -p "${productHome}" $@
