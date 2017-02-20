#!/bin/sh

# Check out all the necessary repositories for StateDMI and reset the head to versions consistent with 3.12.02 version.
# This is the May 2016 work prior to implementing the new well processing approach.
# See:  http://stackoverflow.com/questions/3489173/how-to-clone-git-repository-with-specific-revision-changeset

echo "This script is maintained for historical record to document how the StateDMI-4-RioGrande archive branch was created."
echo "Do not run this script as is. Do the following:"
echo " - Copy to location under where repositories can be cloned"
echo " -  Modify and run to totally check out a new copy of the repositories, as needed"
exit 1

# ----------- StateDMI main program ------------
#git clone https://github.com/OpenWaterFoundation/cdss-app-statedmi.git
#cd cdss-app-statedmi
## Reset from StateDMI.java history...
#git reset --hard fc324f20274ba5696b1e862e24f447ecbab1811f
## Reset from .project history (apparently later than above because .project was not in above)
## Following is for May 15 20:56:57 2016
##git reset --hard aa7ac4f82492b2aa44869b5d2734d732ead786cf
# Following is for May 16 00:22:22 2016
#git reset --hard 4eb84e0e8aac7fed348f1ba70d0efdc832278f7d
#cd ..

# ----------- CDSS library ------------
#git clone https://github.com/OpenWaterFoundation/cdss-lib-cdss-java.git
#cd cdss-lib-cdss-java
# Following is for March 15 02:34:11 2015 - no changes more recent
#git reset --hard 04e206b4af213d77ffbfa8bc3034d5964d892bfa
#cd ..

# ----------- Common library ------------
#git clone https://github.com/OpenWaterFoundation/cdss-lib-common-java.git
#cd cdss-lib-common-java
# Following is for Mar 15 02:40:37 2015
##git reset --hard e19989da4eba59a519cea967958c286525f381e2
# Following is for May 15 20:25:12 2016
#git reset --hard b3a8415c13251847348e259e7ed3e95764058814
#cd ..

# ----------- HydroBase library ------------
#git clone https://github.com/OpenWaterFoundation/cdss-lib-dmi-hydrobase-java.git
#cd cdss-lib-dmi-hydrobase-java
#git reset --hard ca8960351b5e981e77b5c299b23346727bdccb68
# The following is for Apr 29 00:42:31 2016
#git reset --hard f5629fd0529d73653b4b4a3567dac99f8a3a7fac
#cd ..

# ----------- Model library ------------
#git clone https://github.com/OpenWaterFoundation/cdss-lib-models-java.git
#cd cdss-lib-models-java
#git reset --hard 4cb5ccfbf27818678efc21c8261a032b83c10515
# The following is for Jun 29 04:12:19 2015 - no changes between then and well changes
#git reset --hard eccf7d8aa978fb1b47d0e7f95a2ecb8e7cd55533
#cd ..

# ----------- Build tools ------------
#git clone https://github.com/OpenWaterFoundation/cdss-util-buildtools.git
cd cdss-util-buildtools
# Reset from March 15, 2015 - no changes since then
git reset --hard 6b0fc1320cae675a321ae6ebc953048c1f74bd67
cd ..
