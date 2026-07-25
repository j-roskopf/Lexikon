package com.joetr.lexikon.domain

import com.joetr.lexikon.model.Difficulty

class DictionaryRepository(
    private val answersByLength: Map<Int, Map<Difficulty, Set<String>>>,
    private val guessesByLength: Map<Int, Set<String>>,
) {
    fun answers(length: Int, difficulty: Difficulty): Set<String> =
        answersByLength[length]?.get(difficulty) ?: emptySet()

    /** Every answer of this length, across all difficulties. */
    fun allAnswers(length: Int): Set<String> =
        answersByLength[length]?.values?.flatten()?.toSet() ?: emptySet()

    fun guesses(length: Int): Set<String> = guessesByLength[length] ?: emptySet()

    fun isValidGuess(length: Int, word: String): Boolean = word in guesses(length)

    fun randomAnswer(length: Int, difficulty: Difficulty, random: kotlin.random.Random): String {
        val list = answers(length, difficulty).toList()
        require(list.isNotEmpty()) { "No answers for length $length at $difficulty" }
        return list[random.nextInt(list.size)]
    }

    companion object {
        fun fromWordLists(
            answers: Map<Int, Map<Difficulty, List<String>>>,
            guesses: Map<Int, List<String>>,
        ): DictionaryRepository {
            return DictionaryRepository(
                answersByLength = answers.mapValues { (_, byDifficulty) ->
                    byDifficulty.mapValues { (_, words) -> words.map { it.uppercase() }.toSet() }
                },
                guessesByLength = guesses.mapValues { (_, words) -> words.map { it.uppercase() }.toSet() },
            )
        }
    }
}
