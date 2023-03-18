package wordsvirtuoso

import java.io.File
import java.io.FileReader
import kotlin.random.Random


class ValidWordChecks(val wordToCheck: String) {

    fun isValidWord(): Boolean {
        return hasFiveLetters() && hasValidCharacters() && hasNoDuplicateChars()
    }

    fun hasFiveLetters(): Boolean {
        return this.wordToCheck.length == 5
    }

    fun hasValidCharacters(): Boolean {
        val validCharRegex = Regex("""^[a-zA-Z]{5}$""")
        return validCharRegex.matches(this.wordToCheck)
    }

    fun hasNoDuplicateChars(): Boolean {
        val distinctString =this.wordToCheck.split("").distinct().joinToString("")

        return distinctString.length == 5
    }

}


class WordList(val wordsFile: String) {
    var wordSet = mutableSetOf<String>()

    init {
        val reader = FileReader(this.wordsFile)
        reader.use {
            reader.forEachLine {this.wordSet.add(it.lowercase())}
        }
    }

    fun countOfInvalidWords():Int {
        var invalidWordCount = 0
        this.wordSet.forEach() {
            if (!ValidWordChecks(it).isValidWord()) invalidWordCount++
        }

        return invalidWordCount
    }

    fun randomWord(): String {
        val defaultGenerator = Random.Default
        val wordPosition = defaultGenerator.nextInt(this.wordSet.size)
        return this.wordSet.elementAt(wordPosition).toString()

    }

}


fun main() {
    println("Input the words file:")
    val validWordsFile = readln()

    if (!File(validWordsFile).exists()) {
        println("Error: The words file $validWordsFile doesn't exist.")
        kotlin.system.exitProcess(1)
    }

    val newGuessList = WordList(validWordsFile)

}


fun main(args: Array<String> ) {
    // main for use when called from command line.

    if (args.size != 2) {
        println("Error: Wrong number of arguments.")
        kotlin.system.exitProcess(1)
    }

    val allWordsFilename = args[0]
    val candidateWordsFilename = args[1]

    if (!File(allWordsFilename).exists()) {
        println("Error: The words file $allWordsFilename doesn't exist.")
        kotlin.system.exitProcess(1)
    }

    if (!File(candidateWordsFilename).exists()) {
        println("Error: The candidate words file $candidateWordsFilename doesn't exist.")
        kotlin.system.exitProcess(1)
    }

    val allWordsList = WordList(allWordsFilename)
    val allInvalidCount = allWordsList.countOfInvalidWords()
    if (allInvalidCount != 0) {
        println("Error: $allInvalidCount invalid words were found in the $allWordsFilename file.")
        kotlin.system.exitProcess(1)
    }

    val candidateWordList = WordList(candidateWordsFilename)
    val candidateInvalidCount = candidateWordList.countOfInvalidWords()
    if (candidateInvalidCount != 0) {
        println("Error: $candidateInvalidCount invalid words were found in the $candidateWordsFilename file.")
        kotlin.system.exitProcess(1)
    }

    var candidatesNotInAll = 0
    candidateWordList.wordSet.forEach { if (!allWordsList.wordSet.contains(it)) candidatesNotInAll++ }
    if (candidatesNotInAll != 0) {
        println("Error: $candidatesNotInAll candidate words are not included in the $allWordsFilename file.")
        kotlin.system.exitProcess(1)
    }

    println("Words Virtuoso\n")

    //select random secret word from candidate words file
    val wordPosition = Random.nextInt(candidateWordList.wordSet.size)
    val secretWord = candidateWordList.wordSet.elementAt(wordPosition)

    //start game loop
    var userGuess = ValidWordChecks("")
    val startOfGame = System.currentTimeMillis()
    var guessCount = 1

    val previousLetters = mutableSetOf<String>()
    val previousClueStrings = mutableSetOf<String>()

    while (userGuess.wordToCheck != "exit" ) {
        println("Input a 5-letter word:")
        userGuess = ValidWordChecks(readln().lowercase())
        when {
            userGuess.wordToCheck == "exit" -> {
                println("The game is over.")
                kotlin.system.exitProcess(0)
            }
            !userGuess.hasFiveLetters() -> println("The input isn't a 5-letter word.")
            !userGuess.hasValidCharacters() -> println("One or more letters of the input aren't valid.")
            !userGuess.hasNoDuplicateChars() -> println("The input has duplicate letters.")
            !allWordsList.wordSet.contains(userGuess.wordToCheck) ->
                println("The input word isn't included in my words list.")

            userGuess.wordToCheck != secretWord -> {
                var clueOutput = ""
                userGuess.wordToCheck.forEachIndexed { index, letter ->
                    when (letter) {
                        secretWord[index] -> clueOutput += "\u001B[48:5:10m${letter.uppercase()}\u001B[0m"
                        in secretWord -> clueOutput += "\u001B[48:5:11m$letter\u001B[0m"
                        else -> {
                            clueOutput += "\u001B[48:5:7m$letter\u001B[0m"
                            previousLetters.add(letter.uppercase())
                        }
                    }
                }
                previousClueStrings.add(clueOutput)

                previousClueStrings.forEach { println(it) }
                println()
                println("\u001B[48:5:14m${previousLetters.sorted().joinToString(separator = "")}\u001B[0m")
            }

            userGuess.wordToCheck == secretWord -> {
                // hacky
                println()
                previousClueStrings.forEach { println(it) }
                // this has to be split since the problem says each letter has to have coloring.
                userGuess.wordToCheck.uppercase().forEach { print("\u001B[48:5:10m$it\u001B[0m") }
                println("\n")
                println("Correct!")
                val gameDuration = (System.currentTimeMillis() - startOfGame)/1000
                if (guessCount != 1) {
                    println("The solution was found after $guessCount tries in $gameDuration seconds.")
                } else {
                    println("Amazing luck! The solution was found at once.")
                }
                kotlin.system.exitProcess(0)
            }
        }
        guessCount++
    }
}

