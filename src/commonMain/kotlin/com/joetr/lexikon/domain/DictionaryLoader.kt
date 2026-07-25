package com.joetr.lexikon.domain

import com.joetr.lexikon.lexikon.generated.resources.Res
import com.joetr.lexikon.model.Difficulty
import org.jetbrains.compose.resources.ExperimentalResourceApi

object DictionaryLoader {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun load(): DictionaryRepository {
        val answers = mutableMapOf<Int, Map<Difficulty, List<String>>>()
        val guesses = mutableMapOf<Int, List<String>>()
        for (length in 5..10) {
            answers[length] = Difficulty.entries.associateWith { difficulty ->
                readWordFile("files/words/answers-$length-${difficulty.slug}.txt")
            }
            guesses[length] = readWordFile("files/words/guesses-$length.txt")
        }
        return DictionaryRepository.fromWordLists(answers, guesses)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readWordFile(path: String): List<String> {
        val bytes = Res.readBytes(path)
        return bytes.decodeToString()
            .lineSequence()
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() && it.all { c -> c in 'A'..'Z' } }
            .toList()
    }
}
