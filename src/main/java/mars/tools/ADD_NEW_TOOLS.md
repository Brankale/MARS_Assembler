A MARS Tool is a program listed in the MARS Tools menu.
It is launched when you select its menu item and typically interacts with executing MIPS  
programs to do something exciting and informative or at least interesting. 
 
A MARS Application is a stand-alone program for similarly interacting with
executing MIPS programs. It uses MARS MIPS assembler and runtime simulator
in the background to control MIPS execution.
 
The basic requirements for building a MARS Tool are: 
 
1.    It must be a class that implements the MarsTool interface.
        This has only two methods: 'String getName()' to return the
        name to be displayed in its Tools menu item, and
        'void action()' which is invoked when that menu item
        is selected by the MARS user.
 
  2.    It must be added to the mars.venus.ToolLoader.loadTools() method,
        in order to be displayed in the menu 
 
If these requirements are met, MARS will recognize and load  
your Tool into its Tools menu the next time it runs. 
 
There are no fixed requirements for building a MARS Application, a  
stand-alone program that utilizes the MARS API. 
 
You can build a program that may be run as either a MARS Tool or an Application.   
The easiest way is to extend an abstract class provided in the MARS distribution:  
mars.tools.AbstractMarsToolAndApplication.   
 
  1.    It defines a suite of methods and provides default definitions for  
        all but two: getName() and buildMainDisplayArea().
 
  2.    String getName() was introduced above.
 
  3.    JComponent buildMainDisplayArea() returns the JComponent to be placed in the  
        BorderLayout.CENTER region of the tool/app's user interface.  The NORTH and  
        SOUTH are defined to contain a heading and a set of button controls, respectively.   
 
  4.    It defines a default 'void go()' method to launch the application. 
 
  5.    Conventional usage is to define your application as a subclass then launch it  
        by invoking its go() method. 
 
The frame/dialog you are reading right now is an example of an  
AbstractMarsToolAndApplication subclass.  If you run it as an application, you  
will notice the set of controls at the bottom of the window differ from those  
you get when running it from MARS' Tools menu.  It includes additional controls  
to load and control the execution of pre-existing MIPS programs.
 
See the mars.tools.AbstractMarsToolAndApplication API or the source code of  
existing tool/apps for further information.