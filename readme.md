Wub
==============
Break a song into beats and bars using Echo Nest, then mix and match the pieces.

Get an Echo Nest API key

URL: https://developer.echonest.com/account/register

Wub it hard!

java -jar Wub.jar *EchnoNestAPIkey*

Sequencer Window
----

Action			|Result
----------------|-----------------------------------------
Left Mouse 		|Arrange tracks
Shift+Left Mouse|Keep tracks from overlapping
Ctrl+Left Mouse |Push tracks out of the way
Left Mouse 		|Move tracks
Right Mouse		|Play from position
Scroll Wheel	|Zoom
Space			|Pause/Play
Insert			|Insert copy of selected track
Delete			|Delete selected track
Enter			|Save to file
Arrows			|Pan and zoom


Audio Window
----

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