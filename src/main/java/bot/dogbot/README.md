Dogbot
======

This is an implementation of [Chatbot](https://github.com/hollandjake/Chatbot)
which has been created for the use in the Computer Science Chat at The University Of Sheffield.

Commands
========

Inherited commands
------------------
| Command | Response |
| ------- | -------- |
| `!commands` or `!help` | Links to this readme |
| `!github` | Links to [this](https://github.com/hollandjake/Chatbot) github repository |
| `!ping` | Checks if bot is active |
| `!shutdown [code]` | shuts the bot down. The code is output at boot for the bot |
| `!stats` | Outputs information about the bots configuration |
| `!uptime` or `!puptime` | Outputs how long the bot has been running |

Image responses
---------------
| Command | Response |
| ------- | -------- |
| `!bird` or `!birb` | Sends a picture of a bird |
| `!cat` | Sends a picture of a cat |
| `!dog` | Sends a picture of a dog |
| `!extragooddog` | Send a picture of a dog from a curated list |
| `!inspire` | Sends an inspirational quote from [InspiroBot](http://inspirobot.me) |
| `!react` or `!reac` or `!reacc` | Gives the cats reaction |
| `!tab` | Summons the tabulance to make things better |
| `!xkcd` | Sends a random XKCD |
| `!xkcd l` or `!xkcd latest` | Sends the latest XKCD |
| `!xkcd [num]` | Sends a specific XKCD |

Message responses
-----------------
| Command | Response |
| ------- | -------- |
| `!ask [message]` or `!8ball [message]` | Query the all knowing magic 8 ball | 
| `!grab` | Grabs the previous message |
| `!grab [num]` | Grabs the message [num] behind `!grab 1` is equal to `!grab` |
| `!quote` | Returns a random grabbed message |
| `!quote reload` | Reloads the quote file if external changes have been made |
| `!reddits` | Outputs list of reddits being used |
| `!roll` | Returns a random number between 1-6 |
| `!roll [num]` | Returns a random number between 1-[num] |
| `!think` or `!thonk` | Returns a think emoji|
| `!think [num]` or `!thonk [num]` | Returns [num] amount of think emoji's (limited to 100) |
| `!trello` | Links to the group [Trello page](https://trello.com/b/9f49WSW0/second-year-compsci) |
