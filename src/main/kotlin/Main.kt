import java.io.File
import kotlin.system.measureTimeMillis

fun main() {

    val time = measureTimeMillis {
        // Read the words and keep only 5 letters words
        val textWords = File(Signature::class.java.getResource("words_alpha.txt")!!.toURI())
            .readLines()
            .filter { it.length == 5 }

        // Let's group the words by their signature and only keep the words that have 5 distinct letters
        // Also the signature is the same for anagrams since we don't keep track of the order of letters in the signature
        val wordsBySignature: Map<Signature, List<String>> = textWords
            .groupBy { it.toSignature() }
            .filterKeys { it.distinctLetters() == 5 }


        // We get the letters and the number of time those letters appears in each signature
        // We'll use that to start with the least common letters first
        val letterCount = letters.values
            .associateWith { letter -> wordsBySignature.keys.fold(0) { acc, signature -> if (letter in signature) acc + 1 else acc } }.toList()
            .sortedBy { it.second }
            .toMap()

        println("--- Letters and occurrences")
        letterCount.forEach { (letterSignature, count) -> println("${letterSignature.letter()} -> $count") }

        // Fortunately all of those methods keep the order so it's very useful
        val orderedLetters = letterCount.keys

        // We group the word signatures by the first of the letter from the ordered letters
        // This works because we will try to fill in the letters using the same order
        val wordsByLetter = wordsBySignature
            .keys
            .groupBy { word -> orderedLetters.first { letter -> letter in word } }

        println("--- Letters and words count associated to it")
        wordsByLetter.forEach { (letterSignature, words) -> println("${letterSignature.letter()} -> ${words.size}") }

        println("--- Starting the solver loop")
        var solution = listOf(WordGroup(emptyList(), Signature.empty))
        orderedLetters.forEachIndexed { index, letter ->
            val newSolution = mutableListOf<WordGroup>()

            //This is all the letters that we tried to add so far + the current one
            val expectedLetters = letterCount.keys.take(index + 1).merge()
            //We add the previous groups that have all the letters - 1 (we want solution that have 25 of the 26 letters so we can have 1 gap)
            solution
                .filter { it.signature.commonLetters(expectedLetters).distinctLetters() >= index }
                .let { newSolution.addAll(it) }

            val wordsToCheck = wordsByLetter[letter] ?: emptyList()
            println("${lettersInverted[letter]} -> Words to check ${wordsToCheck.size}")

            // Do a cartesian product of the current solutions and words for this letter
            // Ignore any word that creates has duplicate letters or that do not have enough distinct letters (1 gap max)
            wordsToCheck
                .flatMap { word ->
                    solution
                        .filter { word !in it }
                        .map { it + word }
                        .filter { it.signature.commonLetters(expectedLetters).distinctLetters() >= index }
                }
                .let { newSolution.addAll(it) }

            // Update the solution with the new result
            solution = newSolution
            println("${lettersInverted[letter]} -> Current solution ${solution.size}")
        }


        // Now that we the solutions but we probably want to output it
        // The solution removed the anagrams and only contains the signatures we need to transform that back into words
        val words = solution
            .flatMap { it.toWords(wordsBySignature) }
            .onEach { println(it.joinToString(" ")) }

        println()
        println("Total possibilities including anagrams: ${words.size}")
        println()
        println("Total possibilities excluding anagrams: ${solution.size}")

    }

    println("${time / 1000.0} seconds")
}

// The signature is a bitset representation of the word each bit represent whether a letter is present or not
@JvmInline
value class Signature(private val value: Int) {
    operator fun plus(other: Signature): Signature = Signature(value or other.value)
    operator fun contains(other: Signature): Boolean = value and other.value != 0
    fun distinctLetters(): Int = value.countOneBits()
    fun commonLetters(other: Signature) = Signature(value and other.value)
    fun letter(): Char {
        if (distinctLetters() == 1) {
            return lettersInverted[this]!!
        } else {
            throw IllegalStateException("There is more than one letter in this signature")
        }
    }

    companion object {
        val empty = Signature(0)
    }
}

fun Iterable<Signature>.merge(): Signature = fold(Signature.empty) { acc, signature -> acc + signature }

data class WordGroup(
    val words: List<Signature>,
    val signature: Signature,
) {
    operator fun contains(word: Signature): Boolean = word in signature
    operator fun plus(word: Signature): WordGroup = WordGroup(words + word, signature + word)

    fun toWords(wordsBySignature: Map<Signature, List<String>>): List<List<String>> {
        return words
            .map { wordSignature -> wordsBySignature[wordSignature]!! }
            .fold(emptyList()) { acc, words ->
                if (acc.isEmpty()) {
                    words.map { listOf(it) }
                } else {
                    words.flatMap { word -> acc.map { it + word } }
                }
            }
    }
}

// Each letter has its own signature
val letters = mapOf(
    'a' to Signature(0b00000000000000000000000001),
    'b' to Signature(0b00000000000000000000000010),
    'c' to Signature(0b00000000000000000000000100),
    'd' to Signature(0b00000000000000000000001000),
    'e' to Signature(0b00000000000000000000010000),
    'f' to Signature(0b00000000000000000000100000),
    'g' to Signature(0b00000000000000000001000000),
    'h' to Signature(0b00000000000000000010000000),
    'i' to Signature(0b00000000000000000100000000),
    'j' to Signature(0b00000000000000001000000000),
    'k' to Signature(0b00000000000000010000000000),
    'l' to Signature(0b00000000000000100000000000),
    'm' to Signature(0b00000000000001000000000000),
    'n' to Signature(0b00000000000010000000000000),
    'o' to Signature(0b00000000000100000000000000),
    'p' to Signature(0b00000000001000000000000000),
    'q' to Signature(0b00000000010000000000000000),
    'r' to Signature(0b00000000100000000000000000),
    's' to Signature(0b00000001000000000000000000),
    't' to Signature(0b00000010000000000000000000),
    'u' to Signature(0b00000100000000000000000000),
    'v' to Signature(0b00001000000000000000000000),
    'w' to Signature(0b00010000000000000000000000),
    'x' to Signature(0b00100000000000000000000000),
    'y' to Signature(0b01000000000000000000000000),
    'z' to Signature(0b10000000000000000000000000),
)

val lettersInverted = mapOf(
    Signature(0b00000000000000000000000001) to 'a',
    Signature(0b00000000000000000000000010) to 'b',
    Signature(0b00000000000000000000000100) to 'c',
    Signature(0b00000000000000000000001000) to 'd',
    Signature(0b00000000000000000000010000) to 'e',
    Signature(0b00000000000000000000100000) to 'f',
    Signature(0b00000000000000000001000000) to 'g',
    Signature(0b00000000000000000010000000) to 'h',
    Signature(0b00000000000000000100000000) to 'i',
    Signature(0b00000000000000001000000000) to 'j',
    Signature(0b00000000000000010000000000) to 'k',
    Signature(0b00000000000000100000000000) to 'l',
    Signature(0b00000000000001000000000000) to 'm',
    Signature(0b00000000000010000000000000) to 'n',
    Signature(0b00000000000100000000000000) to 'o',
    Signature(0b00000000001000000000000000) to 'p',
    Signature(0b00000000010000000000000000) to 'q',
    Signature(0b00000000100000000000000000) to 'r',
    Signature(0b00000001000000000000000000) to 's',
    Signature(0b00000010000000000000000000) to 't',
    Signature(0b00000100000000000000000000) to 'u',
    Signature(0b00001000000000000000000000) to 'v',
    Signature(0b00010000000000000000000000) to 'w',
    Signature(0b00100000000000000000000000) to 'x',
    Signature(0b01000000000000000000000000) to 'y',
    Signature(0b10000000000000000000000000) to 'z',
)

fun String.toSignature(): Signature = map { letters[it]!! }.merge()