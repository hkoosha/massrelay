.PHONY: build
build: clean
	./gradlew build -xdistZip -xdistTar 
	#-xspotbugsMain

.PHONY: dirty-build
dirty-build: 
	./gradlew build -xdistZip -xdistTar 
	# -xspotbugsMain

.PHONY: bugs
bugs:
	./gradlew build -xdistZip -xdistTar

.PHONY: dist
dist: build
	./gradlew distTar -xdistZip 
	# -xspotbugsMain

.PHONY: clean
clean:
	./gradlew clean

.PHONY: publish-local
publish-local:
	./gradlew publishToMavenLocal

.PHONY: loc
loc: clean
	ohcount
