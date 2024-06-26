### Game Rules

- The game consists of 20 rounds, where the player matches words with their translations.
- Each round presents two columns: the left with foreign words and the right with their translations in the player's language. Each column has 5 randomly ordered words.
- The player matches words from the left column with their translations in the right column.
- Each correct match earns the player 1 point.
- If the player makes a mistake, the game notifies them and allows another attempt.
- Each mistake deducts 1 point.
- The game ends if the player makes three mistakes.
- At the end of the game, the player can view their results.

### General Game Mechanics

- If the dictionary has at least 100 words, the game will have 20 rounds, each using 5 words.
- If the dictionary has fewer than 100 words, the game will have fewer rounds, but no less than 5.
- The game prioritizes words that have been shown less frequently or where the player has made more mistakes.
- Words and their translations are displayed on the screen to be matched.
- After each round, the game reports the points earned.
- At the end of the game, the total points are displayed.
- The player can choose to restart the game or exit.

### Statistics, Randomization, and Word Display Mechanics

- The game reads the word dictionary from the file `vocabulary.xlsx` at startup.
- This file contains words in the foreign language in the first column, translations in the player's language in the second column, the number of times each word has appeared in the third column, and the points earned or lost in the fourth column.
- Words are sorted by two criteria: priority is given to words that have appeared less frequently and those for which the player has earned fewer points.
- Words in the same priority group are shown in random order.
- If the dictionary has fewer words than needed for the game, the program re-reads the dictionary and updates the statistics, displaying words according to the new priorities.
- The game minimizes the repetition of word combinations in rounds by prioritizing words that have appeared less frequently with others in the current round.

### Console Version Mechanics

- The player starts the game.
- The game uses a provided word dictionary or defaults to `vocabulary.xlsx` if none is provided.
- The player enters the numbers of the words they want to match, separated by a space.
- The player can type "exit" at any time to leave the game.
- The game reports the points earned after each round and proceeds to the next round.
- At the end, the game prompts the player to restart or exit by entering "y" or "n".