#!/bin/bash

# We're using 'jekyll serve' which makes the documentation available via http
# on localhost. Ideally, we should simply use 'jekyll build', and have chrome
# read the html from disk (_site/index.html), but that'll need some tweaking to get
# the links to css working. 

HTML_HOST=localhost:4000
DOC_PATH=index.html

bundle install

rm -rf _site

bundle exec jekyll serve > jekyll.log 2>&1 &
JEKYLL_PID=$!

function finish {
  kill $JEKYLL_PID
}
trap finish EXIT

# wait for jekyll to produce output
while true; do
	if [ -f _site/$DOC_PATH ]; then
		# jekyll needs a bit more time for css??
		sleep 2
		break
	fi 

	echo "Waiting for Jekyll to run..."
	sleep 1
done

if ! which google-chrome &> /dev/null; then
	echo "google-chrome not installed. Cannot make pdf"
	exit 1
fi 
google-chrome --headless --disable-gpu --print-to-pdf=medicinfo-api.pdf --print-to-pdf-no-header http://localhost:4000
