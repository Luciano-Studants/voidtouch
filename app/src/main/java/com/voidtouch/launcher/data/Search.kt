package com.voidtouch.launcher.data

import java.text.Normalizer

/** Busca de apps pelo nome: ignora maiúsculas e acentos ("camera" acha "Câmera"). */
object Search {

    private val accents = Regex("\\p{InCombiningDiacriticalMarks}+")

    fun normalize(text: String): String =
        accents.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "")
            .lowercase()
            .trim()

    /**
     * Filtra e ordena por relevância: primeiro os nomes que começam com o texto, depois os que
     * têm uma palavra começando com ele, depois os que apenas contêm. Dentro de cada grupo
     * mantém a ordem alfabética da lista original.
     */
    fun filter(apps: List<AppEntry>, query: String): List<AppEntry> {
        val q = normalize(query)
        if (q.isEmpty()) return apps

        val startsWith = ArrayList<AppEntry>()
        val wordStart = ArrayList<AppEntry>()
        val contains = ArrayList<AppEntry>()
        for (app in apps) {
            val name = app.searchLabel
            when {
                name.startsWith(q) -> startsWith += app
                name.contains(" $q") -> wordStart += app
                name.contains(q) -> contains += app
            }
        }
        return startsWith + wordStart + contains
    }
}
