Wub
==============
Break a song into beats and bars using Echo Nest, then mix and match the pieces.

Get an Echo Nest API key
URL: https://developer.echonest.com/account/register

Wub it!
java -jar Wub.jar *Echnonest API key*

Sequencer Window
----
Left Mouse 		Move tracks
Right Mouse		Play from position
Scroll Wheel	Zoom

Space			Pause/Play
Insert			Insert copy of selected track
Delete			Delete selected track
Enter			Save to file

File Window
----
Left Mouse		Add piece to queue
Right Mouse		Remove piece from queue
Scroll Wheel	Zoom

Alt+Key			Bind key to selected piece
Ctrl+Key		Clear key binding
Key				Play piece


*SPACE			Pause/Play
*ESC			Load new audio
*F1				Loop toggle, queue does not clear after piece plays
*F2         	Reverse order of queue
*F3				Skip currently playing piece
*F4				Clear queue
*F5             Clear key bindings
*F6             Create new audio track from queued pieces
*F7				Create new track, and reverse the audio
*F8				Add copy of track to sequencer
