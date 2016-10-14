
From the book:

  Learn BlackBerry Games Development
  Carol Hamer and Andrew Davison
  Apress, 2010
  Website: http://frogparrot.net/blackberry/


Author responsible for this chapter:
    Carol Hamer
    E-mail: carol.hamer@gmail.com


If you use this code, please mention the book's title and authors, 
and include a link to the website.

Thanks,
  Carol and Andrew

============================

Ant build notes:

Requirements:
* BlackBerry Ant Tools: http://bb-ant-tools.sourceforge.net
* At least one BlackBerry JDE

Notes:
* Each target device model needs a corresponding properties file 
  in the models/ dierctory. The "rim.version" property is used 
  in the build instructions: it corresponds to the JDE version 
  number that is used when building. Hence, the value of "rim.version" 
  must correspond to a BlackBerry JDE that is installed on 
  your computer.
* Some property valuess in the build.xml file and build-all.xml 
  need to be updated to match your local configuration. As a hint,
  any paths that start "C:\Carol\" are paths on my computer that 
  need to be changed to the corresponding paths on your computer.


---------
Last updated: 7 March 2010

