Wub
==============
Break a song into beats and bars using Echo Nest, mix and match the pieces, and save the results as wave files.



Wub it in!
----
Get an Echo Nest API key:https://developer.echonest.com/account/register

java -jar Wub.jar *EchnoNestAPIkey*

Alternatively, set an environment variable: ECHO_NEST_API_KEY=*EchnoNestAPIkey*

java -jar Wub.jar (or double click the jar file)

Pressing F5 or F6 will autosave resulting file as a .wav file, and a .wub file. The wub file saves the Echno Nest analysis with the audio. The whole state can also be saved from the Play window, as a .play file.

Play Window
----
!(https://cloud.githubusercontent.com/assets/385280/5042056/4338c13c-6b94-11e4-866d-cba77885aef5.png)

Action			|Result
----------------|-----------------------------------------
Left Mouse 		|Arrange tracks
Shift+Left Mouse|Keep tracks from overlapping
Ctrl+Left Mouse |Push tracks out of the way
Double click	|reopen tracks audio window
Right Mouse		|Play from position
Scroll Wheel	|Zoom
Space			|Pause/Play
Insert			|Insert copy of selected track
Delete			|Delete selected track
Enter			|Save to file
Shift-Enter		|Save state
Escape			|Load state
Arrows			|Pan and zoom


Audio Window
----
!(https://cloud.githubusercontent.com/assets/385280/5041990/d08c4182-6b92-11e4-975e-d96523ddd970.png)

Action			|Result
----------------|-----------------------------------------
Left Mouse		|Add piece to queue
Right Mouse		|Remove piece from queue
Scroll Wheel	|Zoom
Alt+Key			|Bind key to selected piece
Ctrl+Key		|Clear key binding
Key				|Play piece
Space			|Pause/Play
Escape 			|Load new audio
F1				|Toggle loop mode, queue does not clear after piece plays
F2         		|Reverse order of play queue
F3				|Skip currently playing piece in queue
F4				|Clear queue
F5              |Clear key bindings
F6              |Create new audio track from queued pieces
F7				|Create new track, and reverse the audio
F8				|Add copy of track to sequencer
Arrows			|Pan and zoom