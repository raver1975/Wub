Wub
==============
Break a song into beats and bars using Echo Nest, mix and match the pieces, and save the results as wave files.



Wub it in!
----
1. [Get an Echo Nest API key](https://developer.echonest.com/account/register)

2. (Download jar)[]
https://github.com/raver1975/Wub/blob/master/Wub.jar?raw=true

3. Start Wub from a command line:<br>
java -jar Wub.jar *EchnoNestAPIkey*

3. Alternatively, set an environment variable: ECHO_NEST_API_KEY=*EchnoNestAPIkey*<br>
Then start with:<br>
java -jar Wub.jar (or double click the jar file)

Pressing F6 or F7(reverse audio) will autosave resulting file as a .wav file, and a .wub file. The wub file saves the Echno Nest analysis with the audio. The Play window state can also be saved from the Play window, as a .play file.

Audio Window
----
![](https://cloud.githubusercontent.com/assets/385280/5041990/d08c4182-6b92-11e4-975e-d96523ddd970.png)

From top to bottom:<br>
blue bars   - sections<br>
green bars  - bars<br>
yellow bars - beats<br>
red bars    - tatums<br>
Then waveform, pitch analysis, and timbre analysis<br>

Click on the sections at the top to queue up that section to be played. Loop mode will keep the queue from emptying as it plays. Press F6 or F7 to autosave the queue to a new WAV, and also insert that audio into the Play window.

Action			|Result
----------------|-----------------------------------------
Left Mouse		|Add piece to queue
Right Mouse		|Remove piece from queue
Scroll Wheel	|Zoom
Alt+Key			|Bind key to selected piece
Ctrl+Key		|Clear key binding
Key				|Play piece
Space			|Pause/Play queue
Escape 			|Load new audio
F1				|Toggle loop mode, queue does not clear after piece plays
F2         		|Reverse order of play queue
F3				|Skip currently playing piece in queue
F4				|Clear queue
F5              |Clear all key bindings
F6              |Create/save new audio track from queued pieces
F7				|Create/save new track, and reverse the audio
F8				|Add copy of full track to sequencer
Arrows			|Pan and zoom

Play Window
----
![](https://cloud.githubusercontent.com/assets/385280/5042056/4338c13c-6b94-11e4-866d-cba77885aef5.png)

Action			|Result
----------------|-----------------------------------------
Left Mouse 		|Arrange tracks
Shift+Left Mouse|Keep tracks from overlapping
Ctrl+Left Mouse |Push tracks out of the way
Double click	|Reopen tracks audio window
Right Mouse		|Play from current mous position
Scroll Wheel	|Zoom
Space			|Pause/Play
Insert			|Insert copy of selected track
Delete			|Delete selected track
Enter			|Save to file
Shift-Enter		|Save Play window
Escape			|Load Play window
Arrows			|Pan and zoom