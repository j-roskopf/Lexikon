package com.joetr.lexikon.domain

class DictionaryRepository(
    private val answersByLength: Map<Int, Set<String>>,
    private val guessesByLength: Map<Int, Set<String>>,
) {
    fun answers(length: Int): Set<String> = answersByLength[length] ?: emptySet()
    fun guesses(length: Int): Set<String> = guessesByLength[length] ?: emptySet()
    fun isValidGuess(length: Int, word: String): Boolean = word in guesses(length)
    fun randomAnswer(length: Int, random: kotlin.random.Random): String {
        val list = answers(length).toList()
        require(list.isNotEmpty()) { "No answers for length $length" }
        return list[random.nextInt(list.size)]
    }

    companion object {
        fun fromWordLists(answers: Map<Int, List<String>>, guesses: Map<Int, List<String>>): DictionaryRepository {
            return DictionaryRepository(
                answersByLength = answers.mapValues { (_, words) -> words.map { it.uppercase() }.toSet() },
                guessesByLength = guesses.mapValues { (_, words) -> words.map { it.uppercase() }.toSet() },
            )
        }
    }
}
