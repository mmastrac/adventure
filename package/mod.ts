/**
 * @module
 * Creates an engine to run the original 550-point Adventure text-adventure game.
 * 
 * @example
 * ```ts
 * import { Adventure } from "jsr:@mmastrac/adventure";
 * const adv = new Adventure();
 * while (true) {
 *   const state = adv.step();
 *   if (state == "INPUT") {
 *     adv.input(prompt(`Score: ${adv.score}/550 >`) || "");
 *   } else if (state == "COMPLETED") {
 *     break;
 *   }
 * }
 * ```
 */

import { ACode } from "./acode.js";
import { defaultScriptText } from "./script.js";

export class Adventure {
  #acode: any;
  #state: string = "LOADING";
  #score: number = 0;
  #print: (s: string) => void;
  #console: string = "";
  constructor(
    options: {
      scriptText?: string;
      logLevel?: number;
      save?: (s: string) => void;
      restore?: () => string;
      print?: (s: string) => {};
      randomInt?: (n: number) => {};
    } = {},
  ) {
    const { scriptText, logLevel, save, restore, print, randomInt } = options;
    const logLevel_ = logLevel || 0;
    const save_ = save || (() => {
      console.log("Save not implemented!");
    });
    const restore_ = restore || (() => {
      console.log("Restore not implemented!");
    });
    this.#print = print || ((s) => console.log(s));
    const scriptText_ = scriptText || defaultScriptText;
    const randomInt_ = randomInt || ((n) => ~~(Math.random() * n));
    this.#acode = ACode(scriptText_, {
      stateChange: (s: string) => {
        this.#state = s;
      },
      print: (str: string) => {
        this.#console += str;
      },
      restore: () => {
        restore_();
      },
      save: (s: string) => {
        save_(s);
      },
      log: (str: string) => {
        if (logLevel_ >= 1) console.log("%c%s", "color: yellow;", str);
      },
      trace: (str: string) => {
        if (logLevel_ >= 2) console.log("%c%s", "color: blue;", str);
      },
      setScore: (s: number) => {
        this.#score = s;
      },
      randomInt: (n: number) => {
        return randomInt_(n);
      }
    });
  }
  input(s: string): void {
    if (this.#state == "INPUT") {
      this.#acode.input(s);
    }
  }
  step(max: number = 100): string {
    if (this.#state !== "RUNNING") {
      return this.#state;
    }
    for (let i = 0; i < max; i++) {
      if (this.#state !== "RUNNING") {
        this.#print(this.#console);
        this.#console = "";
        break;
      }
      this.#acode.step();
    }
    return this.#state;
  }
  get score(): number {
    return this.#score;
  }
}

if (import.meta.main) {
  let adv = new Adventure({
    save: (s) => Deno.writeTextFileSync("save.json", s),
    restore: () => Deno.readTextFileSync("save.json"),
  });
  while (true) {
    let state = adv.step();
    if (state == "INPUT") {
      adv.input(prompt(`Score: ${adv.score}/550 >`) || "");
    }
    if (state == "COMPLETED") {
      Deno.exit(0);
    }
  }
}
