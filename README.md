JAsteroids 3D, v1.0

THE GAME:

You are the little white ship at the center of the cube. Use the
controls listed above to navigate in 3D-space and shoot the
asteroids. Use the radar (side and top) on the bottom right and the
ship's-eye view (bottom left) to help determine where you are. Note
that when asteroids (and your ship) leave one side of the cube, they
return on the opposite side. Watch your energy...shields, thrusting
and firing uses up energy. Hitting an asteroid adds to your score
(smaller asteroids are worth more) and splits larger asteroids into
multiple smaller ones. When you destroy them all you move onto the
next round, which is harder. You get a total of 3 lives, but gain
additional lives when you score a multiple of 10000. It's that simple!

_______________________________________________________________________

TO COMPILE:

1. Install a Java DK (anything older than Java 1.1 will work)
2. Edit the Makefile if necessary
3. Type "make"

_______________________________________________________________________

TO RUN:

Type "appletviewer JAsteroids.html" or open JAsteroids.html in a
Java-enabled web browser.

Or, type "JAsteroids.sh" or (Windows:) double-click "JAsteroids.bat".

The various rendering methods available to the 3-D library (see below)
are shown via the world3dApplet:

Type "appletviewer world3dApplet.html" or open world3dApplet.html in a
Java-enabled web browser.

_______________________________________________________________________

Additional information:

This game is based on a custom-written 100% pure Java general-use
cross-platform 3-D rendering and animation library that does not rely
upon Java3D (I wrote it about 4 years before Java3D came out!). It is
very fast and flexible, and easily extended. It uses flat polygon
shading and depth-cueing for the game, but this is easily changed
among several rendering methods, including Gourad (intensity-
interpolated) shading, image mapping, wireframe drawing. Shading
options include multiple colored directional, point, and attenuated
point light sources, ambient, diffuse, and specular shading, and depth
cueing. I wrote the library (and the game) as an excercise in 3-D
programming, and learned quite a lot. Other utilities in the library
include a custom animated image/sprite blitter, dynamic image filters,
fast polygon drawing algorithms, and nifty explosion animator methods.

Additional bits included in the game itself include the ability to
save high scores on a remote web server (perl CGI script
included). Also, a Javascript-based web page that allows the user to
set their user-name (for the high-score server), the screen size, and
computer speed (not really necessary anymore with today's
computers). Many of the game's playing and rendering parameters are
set via applet parameters (examples included).

I believe that others may learn from and use the library, and the
algorithms in the asteroids game, to write other 3-D games. I would be
happy to work with them. Some ideas I have had include other 3-D
versions of classic arcade games such as Space Invaders or Galaga,
Pac-Man, Defender, and so on.

The source code and library are distributed under the GPL and LGPL
(see COPYING and COPYING.LIB). The applet may be used free of charge
on any web site so long as a link to jasteroids3d.sourceforge.net is
visibly included in the applet's web page. This may be done with the
understanding that the applet and library are distributed in the hope
that they will be useful, but WITHOUT ANY WARRANTY, to the extent
permitted by law; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE.

Though not required, I would be eager to hear about any ideas or
modifications that anyone chooses to make to the software.

------------------------------------------------------------

