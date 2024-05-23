adventure ![](https://github.com/mmastrac/adventure/workflows/Java%20CI/badge.svg)
=========

Port of the 550 point Colossal Cave Adventure game, originally written by David Platt, to the web.

![](.docs/screenshot.png)

The heavy lifting is all done by a Google Web Toolkit engine and the front-end is straight JS/jQuery.

Rather than port the game itself, I re-implemented a somewhat generic ACODE interpreter. This is easier said than done: much of the ACODE specification is documented by one or two uses in the actual game and some of it is only available in the original FORTRAN.

The game itself is incredibly difficult and unforgiving. Enjoy!

## Playing

Web: You can play a pre-compiled version of the game here: http://grack.com/demos/adventure/

CLI: You can run this from the command-line using `deno run jsr:@mmastrac/adventure` 
