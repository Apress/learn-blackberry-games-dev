
Chapter 12. Introducing 3D with JSR-239

From the book:

  Learn BlackBerry Games Development
  Carol Hamer and Andrew Davison
  Apress, 2010
  Website: http://frogparrot.net/blackberry/


Author responsible for this chapter:
    Andrew Davison
    Dept. of Computer Engineering
    Prince of Songkla University
    Hat yai, Songkhla 90112, Thailand
    E-mail: ad@fivedots.coe.psu.ac.th


If you use this code, please mention the book's title and authors, 
and include a link to the website.

Thanks,
  Carol and Andrew


============================
Directory contents:
     
    BoxTrix.java, BoxTrixScreen.java, HelpScreen.java,
    Floor.java, TexCube.java, Billboard.java, Overlay.java,
    ModedCamera.java, Camera.java, Utils.java
       - the BoxTrix (10 Java files)


   images\
     - grass.png, matrix.png, metal.png, tree.png, bozo.png, noticeBd.png
         (currently used by BoxTrix)

     - brick.png, brick2.png, cloth.png, rock.png, wood.png, bigGrid.png
         (not currently used by BoxTrix)

   fonts\
     - SceptreRegular.ttf   (currently used by BoxTrix)
     - Marathon.ttf         (not currently used by BoxTrix)


   boxes.png
      - the RIMlet icon

   BoxTrix.jdp, BoxTrix.rapc
       - sample BlackBerry JDE project files


----------------------------
Compilation and Execution

You must JDE 5.0 or later, which supports JSR-239. 
For more details, see "Using JSR-239" below.

Inside JDE 5.0, create a new Project called BoxTrix, and
add:
  - all the Java files
  - the 6 images used by BoxTrix inside images\,
  - the SceptreRegular.ttf font used by BoxTrix inside fonts\

 Make boxes.png the resource icon file (via the projects properties)

 Build and Run inside the 5.0 simulator.


----------------------------
Device Installation

Since no BlackBerry device currently supports JSR-239 (as of
December 2009), then this isn't possible yet.

----------------------------
Using JSR-239

1. Download and install the BlackBerry Java Application Development v5.0 Beta 5 
   (http://na.blackberry.com/eng/developers/devbetasoftware/devbeta.jsp)

2. Download and test a JSR-239 example (OpenGLTest.java) from RIM:
   (http://docs.blackberry.com/en/developers/deliverables/11942/CS_OpenGLTest_952941_11.jsp)
      - it renders a 2D multi-coloured triangle


---------
Last updated: 7 March 2010
