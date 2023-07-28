#!/bin/bash
#
# Copy the StateDMI installer to the CO DNR GCP website:
# - replace all the files on the web with local files

# Supporting functions, alphabetized.

# Determine the operating system that is running the script:
# - mainly care whether Cygwin
checkOperatingSystem() {
  if [ ! -z "${operatingSystem}" ]; then
    # Have already checked operating system so return.
    return
  fi
  operatingSystem="unknown"
  os=$(uname | tr [a-z] [A-Z])
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
  echoStderr "operatingSystem=${operatingSystem} (used to check for Cygwin and filemode compatibility)"
}

# Echo a string to standard error (stderr).
# This is done so that output printed to stdout is not mixed with stderr.
echoStderr() {
  echo "$@" >&2
}

# Get the version modifier:
# - for example, from "12.00.00dev", "12.00.00 dev", 12.00.00beta", or "12.00.00 beta"
# - the first function argument is the full version, possibly including a space
# - the modifier is echoed to stdout, so capture by assigning in the calling code
getVersionModifier() {
  local fullVersion modifier
  fullVersion="$1"
  # grep will print each found character on a separate line so concatenate output.
  modifier=$(echo ${fullVersion} | grep -o -E '[[:alpha:]]' | tr -d '\n' | tr -d ' ')
  echo ${modifier}
}

# Parse the command parameters:
# - use the getopt command line program so long options can be handled
parseCommandLine() {
  local optstring optstringLong
  local exitCode
  local GETOPT_OUT

  # Single character options.
  optstring="dhlv"
  # Long options.
  optstringLong="copy-to-latest,debug,dryrun,help,version"
  # Parse the options using getopt command.
  GETOPT_OUT=$(getopt --options ${optstring} --longoptions ${optstringLong} -- "$@")
  exitCode=$?
  if [ ${exitCode} -ne 0 ]; then
    # Error parsing the parameters such as unrecognized parameter.
    echoStderr ""
    printUsage
    exit 1
  fi
  # The following constructs the command by concatenating arguments.
  eval set -- "${GETOPT_OUT}"
  # Loop over the options.
  while true; do
    #echo "Command line option is ${opt}"
    case "${1}" in
      --debug) # --debug  Indicate to output debug messages.
        echoStderr "--debug detected - will print debug messages."
        debug="true"
        shift 1
        ;;
      -d|--dryrun) # -d or --dryrun  Indicate to do a dryrun but not actually upload.
        echoStderr "--dryrun detected - will not change files on GCP"
        dryrun="-n"
        shift 1
        ;;
      -h|--help) # -h or --help  Print the program usage.
        printUsage
        exit 0
        ;;
      --l,--copy-to-latest) # --l or --copy-to-latest  Also copy to "latest".
        echoStderr "--copy-to-latest detected."
        copyToLatest="yes"
        shift 1
        ;;
      -v|--version) # -v or --version  Print the program version.
        printVersion
        exit 0
        ;;
      --) # No more arguments.
        shift
        break
        ;;
      *) # Unknown option.
        echoStderr ""
        echoStderr "Invalid option: ${1}" >&2
        printUsage
        exit 1
        ;;
    esac
  done
}

# Print the usage.
printUsage() {
  echoStderr ""
  echoStderr "Usage:  ${scriptName} [options]"
  echoStderr ""
  echoStderr "Copy the StateDMI installer (zip file) to the versioned website folder:"
  echoStderr "  ${gsFolderVersion}"
  echoStderr "Optionally, copy the StateDMI installer (zip file) to 'latest' website folder if -l specified:"
  echoStderr "  ${gsFolderLatest}"
  echoStderr ""
  echoStderr "--debug                Turn on debug for troubleshooting."
  echoStderr "-d, --dryrun           Dry run (print actions but don't execute upload)."
  echoStderr "-h, --help             Print usage."
  echoStderr "-l, --copy-to-latest   Copy to 'latest' folder in addition to auto-detected version folder."
  echoStderr "-v, --version          Print the program version."
  echoStderr ""
}

# Print the version of this script.
printVersion() {
  echoStderr "${version}"
}

# Sync the files to GCP.
syncFiles() {
  local answer
  # Copy the local files up to Google Cloud:
  # - the -m option causes operations to run in parallel, which can be much faster
  # - the -d option means delete extra files in destination
  # - the -r option means recursive to sync the whole folder tree

  # THIS IS DISABLED.
  # TODO smalers 2021-09-02 have traditionally only copied the documentation to "latest".
  if [ "1" = "2" ]; then
  if [ ${copyToLatest} = "yes" ]; then
    if [ -f "${installerFile}" ]; then
      echoStderr ""
      echoStderr "Will copy current development version to latest on GCP:"
      echoStderr "  from:  ${installerFile}"
      echoStderr "    to:  ${gsFileLatest}"
      echoStderr "If 'n' is entered, the index file can still be regenerated."
      read -p "Continue with copy [Y/n/q]? " answer
      if [ -z "{answer}" -o "${answer}" = "y" -o "${answer}" = "Y" ]; then
        gsutil.cmd cp ${dryrun} ${installerFile} ${gsFileLatest}
      elif [ "${answer}" = "q" -o "${answer}" = "Q" ]; then
        exit 0
      fi
    else
      echoStderr "[WARNING] File does not exist for 'latest' upload:  ${installerFile}"
    fi
  fi
  fi

  # For now always upload to the versioned copy.
  if [ -f "${installerFile}" ]; then
    echoStderr ""
    echoStderr "Will copy current development version to same version on GCP:"
    echoStderr "  from:  ${installerFile}"
    echoStderr "    to:  ${gsFileVersion}"
    echoStderr "If 'n' is entered, the index file can still be regenerated."
    read -p "Continue with copy [Y/n/q]? " answer
    if [ -z "${answer}" -o "${answer}" = "y" -o "${answer}" = "Y" ]; then
      echoStderr "gsutil.cmd cp ${dryrun} ${installerFile} ${gsFileVersion}"
      gsutil.cmd cp ${dryrun} ${installerFile} ${gsFileVersion}
    elif [ "${answer}" = "q" -o "${answer}" = "Q" ]; then
      exit 0
    fi
  else
    echoStderr "[WARNING] File does not exist for versioned upload:  ${installerFile}"
  fi
}

# Update the GCP index that lists files.
updateIndex() {
  local answer
  echo ""
  read -p "Do you want to update the GCP index file [Y/n]? " answer
  if [ -z "${answer}" -o "${answer}" = "y" -o "${answer}" = "Y" ]; then
    ${scriptFolder}/create-gcp-statedmi-index.bash
  fi
}

# Entry point for the script.

# Check the operating system.
checkOperatingSystem

# Get the location where this script is located since it may have been run from any folder.
scriptFolder=$(cd $(dirname "$0") && pwd)
scriptName=$(basename $0)
version="1.1.0 (2023-07-28)"

repoFolder=$(dirname "${scriptFolder}")
srcFolder="${repoFolder}/src"
srcMainFolder="${srcFolder}/DWR/DMI/StateDMI"
statedmiFile="${srcMainFolder}/StateDMI.java"
if [ -f "${statedmiFile}" ]; then
  statedmiVersion=$(cat ${statedmiFile} | grep -m 1 'PROGRAM_VERSION' | cut -d '=' -f 2 | cut -d '(' -f 1 | tr -d " " | tr -d '"')
  statedmiModifierVersion=$(getVersionModifier "${statedmiVersion}")
else
  echoStderr "[ERROR] Cannot determine StateDMI version because file not found:  ${statedmiFile}"
  exit 1
fi
if [ -z "${statedmiVersion}" ]; then
  echoStderr "[ERROR] Cannot determine StateDMI version by scanning:"
  echoStderr "[ERROR]   ${statedmiFile}"
  exit 1
fi

echoStderr "scriptFolder=${scriptFolder}"
echoStderr "repoFolder=${repoFolder}"
echoStderr "srcFolder=${srcFolder}"
echoStderr "srcMainFolder=${srcMainFolder}"
echoStderr "statedmiVersion=${statedmiVersion}"

dryrun=""
gsFolderLatest="gs://opencdss.state.co.us/statedmi/latest/software"
# Use historical installer file name:
# - don't differentiate between 32 and 64-bit other than 5.0.0+ is 64-bit
gsFolderVersion="gs://opencdss.state.co.us/statedmi/${statedmiVersion}/software"
if [ "${operatingSystem}" = "mingw" ]; then
  gsFileLatest="${gsFolderLatest}/StateDMI_CDSS_${statedmiVersion}_Setup.exe"
  gsFileVersion="${gsFolderVersion}/StateDMI_CDSS_${statedmiVersion}_Setup.exe"
else
  echoStderr ""
  echoStderr "[ERROR] Don't know how to handle operating system:  ${operatingSystem}"
  exit 1
fi

# Whether to copy to latest in addition to the specific version:
# - default to no because the script can be run on any version, and can't assume latest
copyToLatest="no"

# Parse the command line.
parseCommandLine $@

if [ ! -z "${statedmiVersionModifier}" -a "${copyToLatest}" = "yes" ]; then
  # The version contains "dev" or "beta" so don't allow to be used for "latest".
  echoStderr "StateDMI version $statedmiVersion contains modifier $statedmiVersionModifier- not copying to latest."
  copyToLatest="no"
fi

# Sync the files to the cloud.
installerFile="${repoFolder}/dist/StateDMI_CDSS_${statedmiVersion}_Setup.exe"
syncFiles
exitStatus=$?

# Also update the index.
updateIndex
exitStatus=$?

# Exit with the status from the most recent call above.
exit ${exitStatus}
