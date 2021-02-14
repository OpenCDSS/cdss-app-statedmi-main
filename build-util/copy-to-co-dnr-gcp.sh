#!/bin/sh
#
# Copy the StateDMI installer to the CO DNR GCP website
# - replace all the files on the web with local files

# Supporting functions, alphabetized.

# Determine the operating system that is running the script
# - mainly care whether Cygwin
checkOperatingSystem()
{
	if [ ! -z "${operatingSystem}" ]; then
		# Have already checked operating system so return
		return
	fi
	operatingSystem="unknown"
	os=`uname | tr [a-z] [A-Z]`
	case "${os}" in
		CYGWIN*)
			operatingSystem="cygwin"
			;;
		LINUX*)
			operatingSystem="linux"
			;;
		MINGW*)
			operatingSystem="mingw"
			;;
	esac
	echo "operatingSystem=$operatingSystem (used to check for Cygwin and filemode compatibility)"
}

# Get the version modifier:
# - for example, from "12.00.00dev", "12.00.00 dev", 12.00.00beta", or "12.00.00 beta"
# - the first function argument is the full version, possibly including a space
# - the modifier is echoed, so capture by assigning in the calling code
getVersionModifier() {
	local fullVersion
	fullVersion="$1"
	# grep will print each found character on a separate line so concatenate output
	modifier=$(echo $fullVersion | grep -o -E '[[:alpha:]]' | tr -d '\n' | tr -d ' ')
	echo $modifier
}

# Parse the command parameters
parseCommandLine() {
	local d h l opt
	while getopts :dhl opt; do
		#echo "Command line option is ${opt}"
		case $opt in
			d) # Indicate that this should be copied to the latest release and version
				dryrun="-n"
				;;
			h) # Usage
				printUsage
				exit 0
				;;
			l) # Indicate that this should be copied to the latest release and version
				copyToLatest="yes"
				;;
			\?)
				echo "Invalid option:  -$OPTARG" >&2
				exit 1
				;;
			:)
				echo "Option -$OPTARG requires an argument" >&2
				exit 1
				;;
		esac
	done
}

# Print the usage
printUsage() {
	echo ""
	echo "Usage:  $scriptName"
	echo ""
	echo "Copy the StateDMI installer to the latest website folder if -l specified:  $gsFolderLatest"
	echo "Copy the StateDMI installer to the versioned website folder:  $gsFolderVersion"
	echo "A version with 'dev' or 'beta' in the filename cannot be copied to latest."
	echo ""
	echo "-d dry run (print actions but don't copy over existing files)"
	echo "-h print usage"
	echo "-l copy to 'latest' folder in addition to auto-detected version folder"
	echo ""
}

# Sync the files to GCP
syncFiles() {
	local answer
	# Copy the local files up to Google Cloud
	# - the -m option causes operations to run in parallel, which can be much faster
	# - the -d option means delete extra files in destination
	# - the -r option means recursive to sync the whole folder tree
	if [ ${copyToLatest} = "yes" ]; then
		if [ -f "$installerFile64" ]; then
			echo ""
			echo "Will copy current development version to latest on GCP..."
			echo "source:       $installerFile64"
			echo "destination:  ${gsFileLatest32}"
			read -p "Continue with copy [y/n/q]? " answer
			if [ "$answer" = "y" ]; then
				gsutil.cmd cp ${dryrun} $installerFile64 ${gsFileLatest32}
			elif [ "$answer" = "q" ]; then
				exit 0
			fi
		else
			echo "File does not exist for 'latest' upload:  $installerFile64"
			exit 1
		fi
	fi
	# For now always upload to the versioned copy
	if [ -f "$installerFile64" ]; then
		echo ""
		echo "Will copy current development version to same version on GCP..."
		echo "source:       $installerFile64"
		echo "destination:  ${gsFileVersion32}"
		read -p "Continue with copy [y/n/q]? " answer
		if [ "$answer" = "y" ]; then
			echo "gsutil.cmd cp ${dryrun} $installerFile64 ${gsFileVersion32}"
			gsutil.cmd cp ${dryrun} $installerFile64 ${gsFileVersion32}
		elif [ "$answer" = "q" ]; then
			exit 0
		fi
	else
		echo "File does not exist for versioned upload:  $installerFile64"
	fi
}

# Update the GCP index that lists files
updateIndex() {
	local answer
	echo ""
	read -p "Do you want to update the GCP index file [Y/n]? " answer
	if [ -z "$answer" -o "$answer" = "y" -o "$answer" = "Y" ]; then
		${scriptFolder}/create-gcp-statedmi-index.bash
	fi
}

# Entry point for the script

# Check the operating system
checkOperatingSystem

# Get the location where this script is located since it may have been run from any folder
scriptFolder=$(cd $(dirname "$0") && pwd)
scriptName=$(basename $0)
repoFolder=$(dirname "$scriptFolder")
srcFolder="$repoFolder/src"
srcMainFolder="${srcFolder}/DWR/DMI/StateDMI"
statedmiFile="${srcMainFolder}/StateDMI.java"
if [ -f "${statedmiFile}" ]; then
	statedmiVersion=$(cat ${statedmiFile} | grep -m 1 'PROGRAM_VERSION' | cut -d '=' -f 2 | cut -d '(' -f 1 | tr -d " " | tr -d '"')
	statedmiModifierVersion=$(getVersionModifier "$statedmiVersion")
else
	echo "Cannot determine StateDMI version because file not found:  ${statedmiFile}"
	exit 1
fi
if [ -z "$statedmiVersion}" ]; then
	echo "Cannot determine StateDMI version by scanning:  ${statedmiFile}"
	exit 1
fi

echo "scriptFolder=$scriptFolder"
echo "repoFolder=$repoFolder"
echo "srcFolder=$srcFolder"
echo "srcMainFolder=$srcMainFolder"
echo "statedmiVersion=$statedmiVersion"

dryrun=""
gsFolderLatest="gs://opencdss.state.co.us/statedmi/latest/software"
# Use historical installer file name
# - might change this to follow other software, 32/64 bit, etc.
gsFolderVersion="gs://opencdss.state.co.us/statedmi/${statedmiVersion}/software"
if [ "$operatingSystem" = "mingw" ]; then
	gsFileLatest32="$gsFolderLatest/StateDMI_CDSS_${statedmiVersion}_Setup.exe"
	gsFileVersion32="$gsFolderVersion/StateDMI_CDSS_${statedmiVersion}_Setup.exe"
else
	echo ""
	echo "Don't know how to handle operating system:  $operatingSystem"
	exit 1
fi

# Whether to copy to latest in addition to the specific version
# - default to no because the script can be run on any version, and can't assume latest
copyToLatest="no"

# Parse the command line
parseCommandLine $@

if [ ! -z "${statedmiVersionModifier}" -a "$copyToLatest" = "yes" ]; then
	# The version contains "dev" or "beta" so don't allow to be used for "latest"
	echo "StateDMI version $statedmiVersion contains modifier $statedmiVersionModifier- not copying to latest."
	copyToLatest="no"
fi

# Sync the files to the cloud
installerFile64="$repoFolder/dist/StateDMI_CDSS_${statedmiVersion}_Setup.exe"
syncFiles

# Also update the index
updateIndex

exit $?
