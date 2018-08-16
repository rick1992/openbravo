#!/bin/sh

#
# *************************************************************************
# * The contents of this file are subject to the Openbravo  Public  License
# * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
# * Version 1.1  with a permitted attribution clause; you may not  use this
# * file except in compliance with the License. You  may  obtain  a copy of
# * the License at http://www.openbravo.com/legal/license.html 
# * Software distributed under the License  is  distributed  on  an "AS IS"
# * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
# * License for the specific  language  governing  rights  and  limitations
# * under the License. 
# * The Original Code is Openbravo ERP. 
# * The Initial Developer of the Original Code is Openbravo SLU 
# * All portions are Copyright (C) 2012 Openbravo SLU 
# * All Rights Reserved. 
# * Contributor(s):  ______________________________________.
# ************************************************************************
#

clear

if [ ! -f locations ]; then
  cp locations.template locations
fi

while read myline
do
  if [ `expr "$myline" : "^sc\.source\.zip"` = "13" ]; then
    srcPath=$(echo $myline | sed 's,^sc\.source\.zip\=,,')
  elif [ `expr "$myline" : "^org\.openbravo\.userinterface\.smartclient"` = "39" ]; then
    obScModuleDir=$(echo $myline | sed 's,^org\.openbravo\.userinterface\.smartclient\=,,')
  fi
done < locations

firstParam="0"
if [ $1 ]; then
  firstParam=$1
  if [ $firstParam = "this" ] && [ $2 ]; then
    srcPath=$2
  elif [ $firstParam = "obmodule" ] && [ $2 ]; then
    obScModuleDir=$2
  fi
fi

#STEP A
if [ $firstParam = "this" ]; then

  #1)
  srcFile=$(echo $srcPath | sed 's,.*/,,')
  srcDir=$(echo $srcFile | sed 's,.zip$,,')
  currentDir=$PWD

  if [ `expr "$srcPath" : ".*http"` = "4" ] || [ `expr "$srcPath" : ".*ftp"` = "3" ]; then
    wget $srcPath
  else
    cp $srcPath .
  fi

  if [ -f "$srcFile" ]; then
    unzip -o $srcFile
    rm -rf $srcFile

    if [ -d "$srcDir" ]; then

      #2)
      isomorphicDir=web/org.openbravo.userinterface.smartclient/isomorphic
      rm -rf $isomorphicDir
      mv $srcDir/smartclientRuntime/isomorphic $isomorphicDir
      rm -rf docs
      mv $srcDir/smartclientSDK/docs .
      mv $srcDir/smartclientSDK/source/client $isomorphicDir
      mv $srcDir/smartclientSDK/source/copyright.txt $isomorphicDir
      mv $srcDir/smartclientSDK/isomorphic/system/reference $isomorphicDir/system/reference
      rm -rf selenium
      mv $srcDir/smartclientSDK/tools/selenium selenium

      #3)
      cd $isomorphicDir/skins
      rm -rf $(ls | grep -v Enterprise$ | grep -v SmartClient$ | grep -v standard$)
      cd $currentDir

      #4)
      rm -rf $isomorphicDir/system/reference/inlineExamples/serverExamples
      rm -rf $isomorphicDir/system/reference/inlineExamples/dataIntegration

      #5)
      sed 's/.*ds.xml.*/\/\/&/' $isomorphicDir/client/modules/ISC_DataBinding.js > $isomorphicDir/client/modules/ISC_DataBinding.js2
      rm -rf $isomorphicDir/client/modules/ISC_DataBinding.js
      mv $isomorphicDir/client/modules/ISC_DataBinding.js2 $isomorphicDir/client/modules/ISC_DataBinding.js

      #6)
      find $isomorphicDir/ -type f -name "*.gz" -exec rm -rf {} \;

      #7)
      ant combineSource

      #8)
      cp ISC_Combined.js $isomorphicDir/ISC_Combined.uncompressed.js
      rm -rf ISC_Combined.js

      #9)
      cp $isomorphicDir/client/modules/ISC_History.js $isomorphicDir/ISC_History.uncompressed.js

      #We are finish, just change permissions and remove the remaining not used content in the unziped SmartClient_*** folder
      rm -rf $srcDir
      find . -type f | grep -v '\.hg' | sed 's/ /\\ /g' | xargs chmod a-x
      chmod a+x upgrade.sh


    else
      echo "\033[41m-------------------------------------------------------------------\033[0m"
      echo "\033[1;41mERROR:\033[0m $srcFile was expected to be unziped to $srcDir, but it isn't \033[0m"
      echo "\033[41m-------------------------------------------------------------------\033[0m"
      echo ""
    fi

  else
    echo "\033[41m-------------------------------------------------------------------\033[0m"
    echo "\033[1;41mERROR:\033[0m Can not get file $srcPath \033[0m"
    echo "\033[41m-------------------------------------------------------------------\033[0m"
    echo ""
  fi

#STEP B
elif [ $firstParam = "obmodule" ]; then
  obScModuleDir=$(echo $obScModuleDir | sed 's,/$,,')

  if [ -d "$obScModuleDir" ]; then

    #1)
    ant combine

    #2)
    cp ISC_Combined.js $obScModuleDir/web/org.openbravo.userinterface.smartclient/isomorphic

    #3)
    cp web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_History.js $obScModuleDir/web/org.openbravo.userinterface.smartclient/isomorphic

    #4)
    cp web/org.openbravo.userinterface.smartclient/isomorphic/system/helpers/empty.html $obScModuleDir/web/org.openbravo.userinterface.smartclient/isomorphic/system/helpers

    #5)
    rm -rf $obScModuleDir/web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/smartclient
    cp -r web/org.openbravo.userinterface.smartclient/isomorphic/skins/Enterprise $obScModuleDir/web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/smartclient

    #6.1)
    tmp_fileNameA=$obScModuleDir/web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/smartclient/load_skin.js
    tmp_fileNameB=$obScModuleDir/web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/smartclient/load_skin.js2
    sed 's/isc\.Page\.setSkinDir/\/\/ isc.Page.setSkinDir/' $tmp_fileNameA > $tmp_fileNameB
    rm -rf $tmp_fileNameA
    mv $tmp_fileNameB $tmp_fileNameA
    sed 's/isc\.Page\.loadStyleSheet/\/\/ isc.Page.loadStyleSheet/' $tmp_fileNameA > $tmp_fileNameB
    rm -rf $tmp_fileNameA
    mv $tmp_fileNameB $tmp_fileNameA

    tmp_lineNo=$(awk '/isc.Page.setSkinDir/ {print NR}' $tmp_fileNameA)
    tmp_newLineNo=$(($tmp_lineNo+1))
    sed ''$tmp_newLineNo'i\ \ \ \ isc.Page.setSkinDir("[ISOMORPHIC]/../openbravo/skins/Default/smartclient/");' $tmp_fileNameA > $tmp_fileNameB
    rm -rf $tmp_fileNameA
    mv $tmp_fileNameB $tmp_fileNameA
    tmp_newLineNo=$(($tmp_lineNo+2))
    sed ''$tmp_newLineNo'i\ \ \ \ isc.Browser.useCSS3 = false;' $tmp_fileNameA > $tmp_fileNameB
    rm -rf $tmp_fileNameA
    mv $tmp_fileNameB $tmp_fileNameA
    tmp_newLineNo=$(($tmp_lineNo+3))
    sed ''$tmp_newLineNo'i\ \ \ \ isc.Browser.useSpriting = false;' $tmp_fileNameA > $tmp_fileNameB
    rm -rf $tmp_fileNameA
    mv $tmp_fileNameB $tmp_fileNameA

    #6.2)
    sed 's/isc\.loadSkin()/isc.loadSkin();/' $tmp_fileNameA > $tmp_fileNameB
    rm -rf $tmp_fileNameA
    mv $tmp_fileNameB $tmp_fileNameA
    sed 's/isc\.loadSkin();;/isc.loadSkin();/' $tmp_fileNameA > $tmp_fileNameB
    rm -rf $tmp_fileNameA
    mv $tmp_fileNameB $tmp_fileNameA

    rm -rf ISC_Combined.js

  else
    echo "\033[41m-------------------------------------------------------------------\033[0m"
    echo "\033[1;41mERROR:\033[0m Target directory doesn't exist: $obScModuleDir \033[0m"
    echo "\033[41m-------------------------------------------------------------------\033[0m"
    echo ""
  fi

else

    echo "\033[1;32m-------------------------------------------------------------------\033[0m"
    echo "\033[1;30mThis is an unsupported tool for automatic upgrade this module and"
    echo "\033[1;01morg.openbravo.userinterface.smartclient. Use it at your own risk."
    echo "\033[1;32m-------------------------------------------------------------------\033[0m"
    echo "\033[1;01mYou must pass at least one argument"
    echo "\033[1;32m-------------------------------------------------------------------\033[0m"
    echo ""
    echo "  sh upgrade.sh this            <-- it upgrades this module"
    echo "                                    with this file $srcPath"
    echo ""
    echo "  sh upgrade.sh this <file>     <-- it upgrades this module"
    echo "                                    with the <file> given as second argument"
    echo ""
    echo "  sh upgrade.sh obmodule        <-- it upgrades org.openbravo.userinterface.smartclient module"
    echo "                                    placed at $obScModuleDir"
    echo ""
    echo "  sh upgrade.sh obmodule <dir>  <-- it upgrades org.openbravo.userinterface.smartclient module"
    echo "                                    placed at the <dir> path given as second argument"
    echo ""

fi