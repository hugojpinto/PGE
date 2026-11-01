#!/bin/sh
# Makes PDF versions of the docs: docs/GUIDE.pdf and docs/ARCHITECTURE.pdf.
#
# Needs pandoc (https://pandoc.org) and a Chromium-based browser (Chrome, Brave,
# Edge or Chromium) to print the HTML to PDF. Set BROWSER to its path if it is
# not found automatically. Run from the PGE folder:   sh tools/make-pdfs.sh
set -e
cd "$(dirname "$0")/.."

if [ -z "$BROWSER" ]; then
    for candidate in \
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
        "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser" \
        "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge" \
        "/Applications/Chromium.app/Contents/MacOS/Chromium" \
        "$(command -v google-chrome || true)" \
        "$(command -v chromium || true)"; do
        if [ -n "$candidate" ] && [ -x "$candidate" ]; then
            BROWSER="$candidate"
            break
        fi
    done
fi
if [ -z "$BROWSER" ]; then
    echo "No Chromium-based browser found. Set BROWSER=/path/to/browser and try again."
    exit 1
fi

work="$(mktemp -d)"
for doc in GUIDE ARCHITECTURE; do
    title="$(head -1 docs/$doc.md | sed 's/^# //')"
    # gfm: GitHub's Markdown, so the links inside the docs work the same as on GitHub.
    pandoc -f gfm -t html5 --standalone --embed-resources \
        --metadata title="$title" --variable title="" \
        --css tools/docs.css --syntax-highlighting tango \
        -o "$work/$doc.html" "docs/$doc.md"
    "$BROWSER" --headless=new --disable-gpu --no-pdf-header-footer \
        --print-to-pdf="$(pwd)/docs/$doc.pdf" "file://$work/$doc.html" 2>/dev/null
    echo "Wrote docs/$doc.pdf"
done
rm -rf "$work"
