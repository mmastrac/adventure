var SCREEN_HEIGHT = 25;

var currentState = null;
var buffered = '';
var screen = [];
var inputting = false;

var input;

var blink = true;
var focused = true;
var lines = 0;
var more = "";
var acode;

var blinkInterval;
var multiSelection;

function restartBlink() {
	$(document.body).addClass('blink');
	if (blinkInterval)
		clearInterval(blinkInterval);
	
	blinkInterval = window.setInterval(function() {
		blink = !blink;
		if (blink || multiSelection)
			$(document.body).addClass('blink');
		else
			$(document.body).removeClass('blink');
	}, 500);
}

window.ACodeReady = function() {
	input = $('#input');

	acode = new ACode('ADVENTURE.ACODE', {
		print : print,
		stateChange : stateChange,
		log : log,
		save : save,
		restore : restore,
		setScore : setScore,
		trace : trace,
	});

	input.focus();

	window.setInterval(function() {
		refreshInput();
	}, 500);
	
	input.on('keydown', function(e) {
		restartBlink();

		if (processMore()) {
			e.preventDefault();
			return;
		}
		
		if (inputting && e.which == 13) {
			lines = 0;
			var s = cleanup(input.val());
			log('TRACE', 'Got input: ' + s);
			input.val('');
			screen[SCREEN_HEIGHT - 1] = "> " + s;
			print("\n");
			inputting = false;
			acode.input(s);
		} else {
			refreshInput();
		}
	});
	
	input.on('keyup', function(e) {
		refreshInput();
	});
	
	input.on('keypress', function(e) {
		refreshInput();
	});

	input.on('change', function(e) {
		refreshInput();
	});
	
	$(document).on('click', function(e) {
		processMore();
	});

	input.on('blur', function(e) {
		log('TRACE', 'blur');
		focused = false;
		input.focus();
	});
	
	input.on('focus', function(e) {
		log('TRACE', 'focus');
		focused = true;
	});
	
	$('#input-catcher').on('focus', function() {
		input.focus();
	});
};

function processMore() {
	if (more.length) {
		log('TRACE', "... more");
		var buffered = more;
		more = "";
		lines = 0;
		screen[SCREEN_HEIGHT - 1] = "";
		print(buffered);
		if (!more.length)
			acode.resume();
		return true;
	}
	
	return false;
}

function cleanup(s) {
	var out = "";
	for (var i = 0; i < s.length; i++) {
		var c = s.charCodeAt(i);
		if (c < 32 || c > 127)
			out += ' ';
		else
			out += s[i];
	}
	
	return out;
}

function refreshInput() {
	if (inputting && !more.length) {
		var s = input.val();
		screen[SCREEN_HEIGHT - 1] = "> " + cleanup(s);
//		var cursorPos = input.caret().start;
//		if (cursorPos < s.length)
//			screen[SCREEN_HEIGHT - 1] = screen[SCREEN_HEIGHT - 1].slice(0, cursorPos + 2) + CURSOR_CHAR + screen[SCREEN_HEIGHT - 1].slice(cursorPos + 3);
//		else
//			screen[SCREEN_HEIGHT - 1] += CURSOR_CHAR;
		refreshOutput();
	}
}

function print(string) {
	if (more.length) {
		more += string;
		return;
	}
	
	for (var i = 0; i < string.length; i++) {
		if (string[i] == '\n') {
			lines++;
			screen = screen.slice(1);
			screen.push('');
			
			if (lines == SCREEN_HEIGHT) {
				log('TRACE', "more... ");
				screen[SCREEN_HEIGHT - 1] = " --- More ---";
				more = string.slice(i + 1);
				refreshOutput();
				acode.pause();
				return;
			}
		} else {
			screen[SCREEN_HEIGHT - 1] += string[i];
		}
	}
	
	refreshOutput();
}

function stateChange(state) {
	log("TRACE", "State -> " + state);
	if (currentState == 'LOADING') {
		for (var i = 0; i < SCREEN_HEIGHT; i++)
			screen.push('');
		refreshOutput();
	} 
	
	if (state == 'INPUT') {
		restartBlink();
		
		setTimeout(function() {
			// Workaround for iOS setting the value of the input a few ms after user hits enter
			input.val('');
			print("> ");
			inputting = true;
		}, 10);
	}
	
	currentState = state;
}

function refreshOutput() {
	if (inputting && focused) {
		$('#output').text('');
		for (var i = 0; i < screen.length; i++) {
			if (i == SCREEN_HEIGHT - 1) {
				var line = screen[i] + ' ';
				var start = input.caret().start + 2;
				var end = input.caret().end + 2;
				multiSelection = (start != end);
				if (start == end)
					end++;
				
				$('#output').append($('<span>').text(line.slice(0, start)));
				$('#output').append($('<span class="hilite">').text(line.slice(start, end)));
				$('#output').append($('<span>').text(line.slice(end)));
			} else {		
				if (screen[i].length) {
					$('#output').append($('<div>').text(screen[i]));
				} else {
					$('#output').append($('<br>'));
				}
			}
		}
	} else {
		$('#output').text(screen.join('\n'));
	}
}

function save(s) {
	if (s)
		localStorage['save'] = s;
	else
		delete localStorage['save'];
}

function restore() {
	return localStorage['save'];
}

function log(category, string) {
	console.log(category + ": " + string);
}

function trace(string) {
	log("TRACE-GWT", string);
}

function setScore(score, total) {
	if (score == 0 && total == 0)
		return;
	
	$('#score').show();
	$('#score_current').text(score);
	$('#score_total').text(total);
}