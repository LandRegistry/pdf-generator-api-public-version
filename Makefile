# This file contains common administration commands. It is language-independent.

# Run this with 'make unittest' or 'make report="true" unittest'
unittest:
	if [ -z ${report} ]; then gradle test; else gradle test jacocoTestReport; fi

integrationtest:
	TODO

run:
	# To prevent "Could not open terminal for stdout" error
	export TERM=${TERM:-dumb} 
	gradle run

buildjar:
	export TERM=${TERM:-dumb}
	gradle clean shadowJar
