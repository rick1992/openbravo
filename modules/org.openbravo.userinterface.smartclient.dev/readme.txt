This README describes how to install a new smartclient version into the system.

All processes here described can be automatically done using upgrade.sh script.
Depending on many factors (SO, installed software, ...) it could not work, so use it at your own risk.


STEP A: Upgrade this module (org.openbravo.userinterface.smartclient.dev)

1) Download new sources
Go here:
http://www.smartclient.com/builds/SmartClient/
And then inside the desired version, then inside the LGPL directory and finally the desired nightly build revision
Download the zip file

After downloading unzip/untar the file and you will see 2 subfolders in the created folder:
smartclientRuntime
smartclientSDK

the first one (smartclientRuntime) will be used to get the runtime javascript from (to build the ISC_Combined.js file).

2) Copy/Move the following files/directories to the org.openbravo.userinterface.smartclient.dev module
smartclientRuntime/isomorphic --> web/org.openbravo.userinterface.smartclient/isomorphic (note see remove not-needed skins step below)
smartclientSDK/source/client --> web/org.openbravo.userinterface.smartclient/isomorphic
smartclientSDK/source/copyright.txt --> web/org.openbravo.userinterface.smartclient/isomorphic
smartclientSDK/isomorphic/system/reference --> web/org.openbravo.userinterface.smartclient/isomorphic/system/reference
smartclientSDK/docs --> docs
smartclientSDK/tools/selenium --> selenium

3) Remove unneeded skins except:
-Enterprise
-Smartclient
-Standard
from web/org.openbravo.userinterface.smartclient/isomorphic/skins

4) Prevent build errors:
The SmartClient reference contains java examples which our build system will try to compile (with errors of course),
therefore remove not needed java files by removing the following directories
web/org.openbravo.userinterface.smartclient/isomorphic/system/reference/inlineExamples/serverExamples
web/org.openbravo.userinterface.smartclient/isomorphic/system/reference/inlineExamples/dataIntegration

5) Prevent 404 error in source mode
Inside web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_DataBinding.js
comment out the lines which load the ds.xml files:
//        "schema/DataSource.ds.xml",
//        "schema/DataSourceField.ds.xml",
//        "schema/Validator.ds.xml",
//        "schema/SimpleType.ds.xml",
//        "schema/XSComplexType.ds.xml",
//        "schema/XSElement.ds.xml",
//        "schema/SchemaSet.ds.xml",
//        "schema/WSDLMessage.ds.xml",
//        "schema/WebService.ds.xml",
//        "schema/WebServiceOperation.ds.xml",
//        "schema/WSOperationHeader.ds.xml",

6) Remove the .js.gz files of
web/org.openbravo.userinterface.smartclient/isomorphic/

7) Create a combined file with name ISC_Combined.uncompressed.js with these files:
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_Core.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_Foundation.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_Containers.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_Grids.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_Forms.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_DataBinding.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_Calendar.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_PluginBridges.js
web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/ISC_RichTextEditor.js
You can use the following ant task to generate it: ant combineSource

8) Copy ISC_History.js as ISC_History.uncompressed.js to modules/org.openbravo.userinterface.smartclient/web/org.openbravo.userinterface.smartclient/isomorphic
ISC_History.js can be found in org.openbravo.userinterface.smartclient.dev/web/org.openbravo.userinterface.smartclient/isomorphic/client/modules/

9) Move ISC_Combined.uncompressed.js to modules/org.openbravo.userinterface.smartclient/web/org.openbravo.userinterface.smartclient/isomorphic


STEP B: Upgrade Openbravo org.openbravo.userinterface.smartclient module

1) Create a combined file with name ISC_Combined.js with these files:
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_Core.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_Foundation.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_Containers.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_Grids.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_Forms.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_DataBinding.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_Calendar.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_PluginBridges.js
web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/ISC_RichTextEditor.js
You can use the following ant task to generate it: ant combine

2) Copy ISC_Combined.js to modules/org.openbravo.userinterface.smartclient/web/org.openbravo.userinterface.smartclient/isomorphic
ISC_Combined.js can be found in org.openbravo.userinterface.smartclient.dev/

3) Copy ISC_History.js to modules/org.openbravo.userinterface.smartclient/web/org.openbravo.userinterface.smartclient/isomorphic
ISC_History.js can be found in org.openbravo.userinterface.smartclient.dev/web/org.openbravo.userinterface.smartclient/isomorphic/system/modules/

Note: If you already have this module (org.openbravo.userinterface.smartclient.dev) inside your Openbravo modules folder
you can use the following command to generate ISC_Combined.js and copy it and ISC_History.js to its target path: ant

4) Copy empty.html to modules/org.openbravo.userinterface.smartclient/web/org.openbravo.userinterface.smartclient/isomorphic/system/helpers
empty.html can be found in org.openbravo.userinterface.smartclient.dev/web/org.openbravo.userinterface.smartclient/isomorphic/system/helpers/

5) Copy the Enterprise skin to our skin directory:
org.openbravo.userinterface.smartclient.dev/isomorphic/skins/Enterprise --> 
modules/org.openbravo.userinterface.smartclient/web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/smartclient

6) Make the following changes to:
modules/org.openbravo.userinterface.smartclient/web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/smartclient/load_skin.js
6.1) changes in the top (note see that loadStylesheets is commented out):
   // must be relative to your application file or isomorphicDir
 //   isc.Page.setSkinDir("[ISOMORPHIC]/skins/Enterprise/")
  isc.Page.setSkinDir("[ISOMORPHIC]/../openbravo/skins/Default/smartclient/");
  isc.Browser.useCSS3 = false;
  isc.Browser.useSpriting = false;


//----------------------------------------
// Load skin style sheet(s)
//----------------------------------------
  //  isc.Page.loadStyleSheet("[SKIN]/skin_styles.css", theWindow)

6.2) add a ; at the end to:
isc.loadSkin();

(needs to be reported to isomorphic).


Now build Openbravo.


Final considerations: If you want to have the Smartclient source code at runtime execute
the following ant task: ant source
For more details, please read: http://wiki.openbravo.com/wiki/Client_Side_Development_and_API#Using_non-obfuscated_Smartclient_code
