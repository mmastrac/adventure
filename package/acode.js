export const ACode = function (script, opts) {
  return new window.parent.ACode(script, opts);
};
let adventure = () => {};
adventure.__sendStats = () => {};
let document = {};
let ErrorStub = {};
let window = { parent: { adventure, Math, Date, Error: ErrorStub, document } };

// GWT code is loaded below. See https://github.com/mmastrac/adventure
// __DEV_MODE__

// This stub script is replaced when we build the final package
adventure.onScriptDownloaded = function (s) {
  eval(s.join("\n"));
};
const file = Deno.readTextFileSync(
  "com.grack.adventure.web/war/adventure/compilation-mappings.txt",
).split("\n")[0];
const script = Deno.readTextFileSync(
  `com.grack.adventure.web/war/adventure/${file}`,
);
eval(script);
