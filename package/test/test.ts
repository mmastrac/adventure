import { Adventure } from "../mod.ts";

Deno.test(function smoketest() {
  const inputs: string[] = Deno.readTextFileSync(new URL('./smoketest.txt', import.meta.url)).split('\n');
  let step = 0;
  let adv = new Adventure({randomInt: (n) => { console.log(step, n); return step % n } });
  while (true) {
    if (adv.step() == "INPUT") {
      step = (step + 10) % 100;
      while (true) {
        const next = inputs.shift();
        if (next === undefined) {
          throw "Ran out of inputs!";
        }
        if (next === "" || next[0] === "#") {
          continue;
        }
        if (next.startsWith("@random ")) {
          step = +next.slice(8);
          continue;
        }
        console.log("> " + next);
        adv.input(next);
        break;
      }
    } else {
      break;
    }
  }
});

// Deno.test(function walkthrough() {
//   const inputs: string[] = Deno.readTextFileSync(new URL('./walkthru.txt', import.meta.url)).split('\n');
//   let step = 0;
//   let adv = new Adventure({randomInt: (n) => { console.log(step, n); return step % n } });
//   while (true) {
//     if (adv.step() == "INPUT") {
//       step = (step + 10) % 100;
//       while (true) {
//         const next = inputs.shift();
//         if (next === undefined) {
//           throw "Ran out of inputs!";
//         }
//         if (next === "" || next[0] === "#") {
//           continue;
//         }
//         if (next.startsWith("@random ")) {
//           step = +next.slice(8);
//           continue;
//         }
//         console.log("> " + next);
//         adv.input(next);
//         break;
//       }
//     }
//   }
// });
