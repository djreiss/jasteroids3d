.KEEP_STATE:

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY, to the extent permitted by law; without
# even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.

all: cfiles jar

VERSION = 1.1.3
PACKAGE = JAsteroids3D

JC = javac
#JC = jikes -g:none
JCFLAGS = -O -nowarn
JDOC = javadoc
JAVA = java

JFILES = djr/d3d/anim/animStrip.java \
	djr/d3d/anim/SplitImage.java \
	djr/d3d/anim/animStrip3d.java \
	djr/d3d/matrix.java \
	djr/d3d/vector.java \
	djr/d3d/vertex.java \
	djr/d3d/world3d.java \
	djr/d3d/face.java \
	djr/d3d/light.java \
	djr/d3d/movableObject3d.java \
	djr/d3d/object3d.java \
	djr/d3d/polyDraw.java \
	djr/d3d/world3dApplet.java \
	djr/d3d/JAsteroids/JAsteroids.java \
	djr/util/gui/AWTUtils.java

CFILES = djr/d3d/anim/animStrip.class \
	djr/d3d/anim/SplitImage.class \
	djr/d3d/anim/animStrip3d.class \
	djr/d3d/matrix.class \
	djr/d3d/vector.class \
	djr/d3d/vertex.class \
	djr/d3d/world3d.class \
	djr/d3d/face.class \
	djr/d3d/light.class \
	djr/d3d/movableObject3d.class \
	djr/d3d/object3d.class \
	djr/d3d/polyDraw.class \
	djr/d3d/world3dApplet.class \
	djr/d3d/JAsteroids/JAsteroids.class \
	djr/d3d/JAsteroids/ship.class \
	djr/d3d/JAsteroids/asteroid.class \
	djr/d3d/JAsteroids/torpedo.class \
	djr/d3d/JAsteroids/ufo.class \
	djr/util/gui/AWTUtils.class

INFOFILES = README COPYING COPYING.LIB Authors TODO images/jasteroids3d.png
JARFILE = jasteroids.jar
INTOJAR = ${CFILES} images/explosion.gif images/dave.gif sounds/*.au
HFILES = *.html for_web_site/*.html *.sh *.bat

cfiles: ${CFILES}
	${JC} ${JCFLAGS} djr/d3d/JAsteroids/JAsteroids.java

jar: ${CFILES}
	${JC} ${JCFLAGS} djr/util/MakeJarRunnable.java
	jar -cf ${JARFILE} ${INTOJAR} 
	${JAVA} djr.util.MakeJarRunnable ${JARFILE} djr.d3d.JAsteroids.JAsteroids temp_r.jar
	mv -f temp_r.jar ${JARFILE}

doc:
	echo '<body><pre>' > README.html
	cat README >>README.html
	echo '</pre></body>' >> README.html
	mkdir -p temp
	cp -rLf --parents ${JFILES} temp
	cd temp && javadoc -d ../docs -author -private -overview ../README.html ${JFILES}
	rm -rf temp README.html

dist: jar ${CFILES} ${INFOFILES} ${HFILES} 
	mkdir -p ${PACKAGE}-${VERSION}-bin
	cp -rL --parents ${JARFILE} ${INFOFILES} ${HFILES} ${PACKAGE}-${VERSION}-bin
	tar cf - ${PACKAGE}-${VERSION}-bin | gzip -c >${PACKAGE}-${VERSION}-bin.tar.gz
	rm -rf ${PACKAGE}-${VERSION}-bin

srcdist: jar doc ${CFILES} ${INFOFILES} ${HFILES} 
	mkdir -p ${PACKAGE}-${VERSION}-src
	cp -rLf --parents Makefile docs ${INFOFILES} ${JFILES} ${HFILES} images sounds ${PACKAGE}-${VERSION}-src
	tar cf - ${PACKAGE}-${VERSION}-src | gzip -c >${PACKAGE}-${VERSION}-src.tar.gz
	rm -rf ${PACKAGE}-${VERSION}-src docs

clean: 
	rm -f ${CFILES} ${JARFILE} 

.SUFFIXES: .java .class
.java.class:
	${JC} ${JCFLAGS} $<
