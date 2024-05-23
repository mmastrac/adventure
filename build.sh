#!/bin/bash
set -eu -o pipefail
echo Adventure
echo =========
echo

echo Build target: ./build/
mkdir -p build || true

echo Running Java tests...
mvn test > /dev/null

echo Building GWT output...
mvn package > /dev/null
rm -rf build/web/ || true
cp -R com.grack.adventure.web/war/ build/web/

echo Building JSR package...
rm -rf build/jsr/ || true
cp -R package/ build/jsr/

cp com.grack.adventure.web/war/adventure/*.cache.js /tmp/__adventure.js
deno eval 'const adventure={ onScriptDownloaded: (x) => Deno.writeTextFileSync("/tmp/__adventure.js", x.join("\n")) }; eval(Deno.readTextFileSync("/tmp/__adventure.js"))'

cat <(awk '/__DEV_MODE__/ {exit} {print}' package/acode.js) /tmp/__adventure.js > build/jsr/acode.js

echo '// base64-encoded ADVENTURE.ACODE' > build/jsr/script.js
echo 'export const defaultScriptText = atob(`' >> build/jsr/script.js 
base64 < com.grack.adventure.web/war/ADVENTURE.ACODE >> build/jsr/script.js
echo '`);' >> build/jsr/script.js

echo Testing JSR package...
deno test -A build/jsr/ 2>/dev/null > /dev/null

echo
echo Done\!
