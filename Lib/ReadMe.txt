Advanced Card Systems Ltd.
Unit 1008, 10th Floor
Hongkong International Trade and Exhibition Centre
1 Trademart Drive, Kowloon Bay
Hong Kong
http://www.acs.com.hk 
Tel: +852 2796 7873
Fax: +852 2796 1286



Contents
--------

   1. Release Notes
   2. Installation
   3. History
   4. File Contents
   5. Support



1. Release Notes
----------------

Product: ACR120S JNI
Version: 1.0.0.2
Date:    10 January 2007



2. Installation
---------------

1. Place the DLL files to your Windows system directory
   (e.g. C:\Windows\System32).
2. Place the JAR file to your class path. There are 2 JAR files in this release.
   One is for JDK 1.5 and another one is for JDK 1.4.
3. To allow the applet to access the reader, you need to add the following line
   to lib\security\java.policy file in your JRE installation directory.
   
   permission java.lang.RuntimePermission "loadLibrary.ACR120SJNI";



3. History
----------

1.0.0.0:	New Release (3/11/2006)
1.0.0.1:	Fix JNI DLL export function name (27/11/2006)
		Add JAR file for JDK 1.4 (18/12/2006)
1.0.0.2:	Fix the bug in login function (27/12/2006)
		Update API documentation (10/1/2007)



4. File Contents
----------------

ACR120.dll			1.2.0.0
ACR120S.jar			x.x.x.x (for JDK 1.5)
ACR120S_jdk1.4.1_07.jar		x.x.x.x (for JDK 1.4)
ACR120S-API-doc-html.zip	x.x.x.x
ACR120SJNI.dll			1.0.0.2



5. Support
----------

In case of problem, please contact ACS through:

web site: http://www.acs.com.hk/
email: info@acs.com.hk
tel: (852) 2796-7873



-----------------------------------------------------------------



Copyright 
Copyright by Advanced Card Systems Ltd. (ACS) No part of this reference manual may be reproduced or transmitted in any from without the expressed, written permission of ACS. 

Notice 
Due to rapid change in technology, some of specifications mentioned in this publication are subject to change without notice. Information furnished is believed to be accurate and reliable. ACS assumes no responsibility for any errors or omissions, which may appear in this document. 
