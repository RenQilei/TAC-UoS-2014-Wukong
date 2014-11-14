SICS TAC Game Data Toolkit 0.5 beta

This is a quickly compiled README file.  Please bear with it.

This package contains some tools for downloading and parsing game data
files in the SICS TAC Classic server format.


Requirements
------------
The TAC Game Data Toolkit requires Java SDK 1.4.


Compiling
---------
Type "compile.bat" (or "compile.sh" for unix) to compile the game
data toolkit.


Running the example statistics generator
----------------------------------------

java -jar gametools.jar -handler se.sics.tac.util.TACGameStatistics \
   -startGame 3973 -endGame 3975 \
   -download -server tac1.sics.se >statistics.txt

This command will download the games 3973 to 3975 from the server
tac1.sics.se to the directory 'games' (if not already downloaded) and
store the statistics for the games in the file 'statistics.txt'.


Configuring the game data tool
------------------------------

Options are specified as arguments at startup. This is a short
description of the more important arguments:

 -path <path>          Specifies the path to where the game data is
                       locally stored (downloaded data will be stored
                       in this path). Default is 'games'.

 -base <baseURL>       Set the base URL to download game data from.
                       Default is 'http://tac1.sics.se:8080/history/'

 -server <host>        Set the server to download game data from.
                       This is equivalent to setting the base to
                       http://<host>:8080/history/ (se above)

 -download             Specifies that game data should be downloaded
                       if not found locally. The end game must be
                       specified if data is to be downloaded.
                       Default is that download is not permitted.

 -startGame <game id>  Specifies the first game to be included in
                       this session.

 -endGame <game id>    Specifies the last game to be included in
                       this session.

 -handler <className>  Set the handler for the game data in form of
                       the name of a Java class that must implement
                       the interface TACGameLogListener. An instance
                       of this class is created and handed all game data.
                       Default is to simply display the game id.


Implementing new game data handlers
-----------------------------------

Create a Java class implementing TACGameLogListener with these
methods:

  // Called at startup for initialization and handling of any
  // extra arguments
  public void init(ArgEnumerator a);

  // Returns any extra argument this listener handles or NULL if
  // it does not handle any argument
  public String getUsage();

  // Called when a new game is opened for parsing
  public void gameOpened(String path, TACGameInfo game);

  // Called when a game has been completely parsed
  public void gameClosed(String path, TACGameInfo game);

  // Called when all games have been read
  public void finishedGames();

Usually it is the method gameClosed that will do most of the work
since the game has then been completely parsed.

To use the new data handler call:

java -jar gametools.jar -handler <Your handler class name> \
   -endGame 10 -download


Best,
The TAC Team, SICS
