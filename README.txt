RapidMiner
 
 Copyright (C) 2001-2006 by Rapid-I and contributors
 
 Complete list of developers available at our web site:

 http://rapid-i.com

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as 
published by the Free Software Foundation; either version 2 of the
License, or (at your option) any later version. 

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA.

--------------------------------------------------------------------

Please note:
------------

* The license of all libraries can be found in the directory 
  "licences" located in the main directory of RapidMiner.
  
* The only other libraries under GPL are Weka and JUnit.

* The library "felt.jar" is not licensed under the GPL and
  it is NOT allowed to use it for other applications.

* It is NOT allowed to use the icons of RapidMiner for other
  applications.
  
* All other libraries are licensed under LGPL or BSD-style
  licenses.
  
* RapidMiner itself (without the library "felt.jar", see above) is 
  licensed under the GPL (see the corresponding license file).
  
 
Library Notes:
--------------

Along with the core RapidMiner classes you have received some software
packages and libraries not all of which are subject to the RapidMiner
license terms. You can find the license texts of these libraries
in the 'licenses' directory of RapidMiner. These libraries are

  src/libsvm/*	The java source files are part of the libsvm software
		by Chih-Chung Chang and Chih-Jen Lin, who hold the
		Copyright 2000-2005. The software can be downloaded
		from

                   http://www.csie.ntu.edu.tw/~cjlin/libsvm/

        We have made some minor modifications (removed
		System.out messages, added a method for writing the
		model to a stream, renamed some classes). You can find the 
		libsvm license in README.libsvm.
		
		Current Version: own based on 2.84

  lib/weka.jar  This java file is part of the Weka machine learning 
                software which has its home at

		   http://www.cs.waikato.ac.nz/ml/weka/

		Weka comes under the GNU General Public License and
		the original copyright note is

		Copyright (C) 1998, 1999, 2000, 2001  Eibe Frank, 
		   Leonard Trigg, Mark Hall, Richard Kirkby

		You can find the Weka original readme file in
		README.weka
		
		Current Version: 3.5.6 (latest CVS build)

  lib/colt.jar  Colt is a library for scalable scientific and technical 
                computing in Java. It has its home at
                
                   http://dsd.lbl.gov/~hoschek/colt/
                   
                Current Version: 1.2.0

  lib/Jama.jar  JAMA is a basic linear algebra package for Java. This 
		archive holds the classes of the JAva MAtrix package 
		which is a cooperative product of The MathWorks and
		the National Institute of Standards and Technology 
		(NIST) which has been released to the public domain.
		The software can be downloaded from

		   http://math.nist.gov/javanumerics/jama/
		   
		Current Version: 1.0.2

  lib/jung-algorithms.jar
  lib/jung-api.jar
  lib/jung-graph-impl.jar
  lib/jung-io.jar
  lib/jung-visualization.jar
                JUNG is the Java Universal Network/Graph Framework --
                a software library that provides a common and extendible 
                language for modeling, analysis, and visualization of 
                data. It has its home at

		           http://jung.sourceforge.net/
		           
		        Current Version: 2.0alpha2

  lib/commons-collections.jar
          
                The Jakarta commons library provides some reusable 
                Java components. It has its home at

                   http://jakarta.apache.org/commons/
                   
                It is needed by xstream.
                   
                Current Version: 3.2
                
  lib/collections-generic.jar
          
                The Jakarta generic collections library provides 
                some reusable Java generic collections components. 
                It has its home at

                   http://jakarta.apache.org/commons/
                   
                It is needed by Jung.
                   
                Current Version: 4.0.1
                
   lib/concurrent.jar
          
                This package provides standardized, efficient versions
                of utility classes commonly encountered in concurrent
                Java programming. It has its home

                   http://g.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html
                   
                It is needed by Jung.
                   
                Current Version: 1.3.4

  lib/junit.jar  JUnit is a testing framework. Please note that JUnit is only
                 used during development and is not part of the non-GPL releases
                 Is has its home at

                    http://www.junit.org/index.htm
                    
                 Current Version: 4.3.1
                   
  lib/jmathplot.jar  JMathPlot is a plotter framework which is used for the
                     3D plots and some of the 2D plots. Is has its home at

                       http://jmathtools.sourceforge.net/
                       
                     Current Version: 2006-09-15          

  lib/xstream.jar  XStreams is a very convenient library for creating XML
                   based serializations for arbitrary Java objects. It can
                   be found at

                    http://xstream.codehaus.org
                   
                   Current Version: 1.2.2

  lib/xpp3.jar  XPP3 is a fast XML pull parser used by XStreams. It is 
                part of the XStreams package.
                
                Current Version: 3
  
  lib/felt.jar  This library contains the look and feel of RapidMiner. 
                Very important: This library is not part of the GPL
                open-source version of RapidMiner and is not allowed to
                be used for other applications. The same applies for
                the icons of RapidMiner.
                
                Current Version: own based on 1.8.2
                
  lib/jugpreview.jar  Contains the Print Preview dialog
  
                      Current Version: own
                
  lib/jxl.jar  JExcel is used to load Microsoft Excel spreadsheets into
               RapidMiner as data input.
               
               Current Version: 2.6.2
               
  lib/joone.jar  A library for neural network learning. This library is
                 changed from the original by introducing the usage of 
                 fixed random numbers and some other small modifications.
  
                 Current Version: own based on 1.2.1

  lib/jfreechart.jar  A library for painting some of the charts available
                      in RapidMiner
  
                 Current Version: 1.0.5


  lib/jcommon.jar  This library is used by the jfreechart library
  
                 Current Version: 1.0.9
                                 
  lib/freehep/*	 These jar files are part of the Java FreeHEP
                 library. It is used for PDF and PS file export.
		         The Copyright 2000 holders are:
		
		            CERN, Geneva, Switzerland
		            SLAC, Stanford, California, U.S.A.
                    University of California Santa Cruz, U.S.A.

		         This software is distributed under the LGPL license, see
		         the file lib/freehep/LGPL.txt. It can be downloaded from

		            http://www.freehep.org/
		            
		         Current Version: 2.0