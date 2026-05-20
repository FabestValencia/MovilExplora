package com.example.movilexplora.data.ai

import com.example.movilexplora.domain.ai.CategoryRecommender
import com.example.movilexplora.domain.ai.Recommendation
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class GeminiCategoryRecommender @Inject constructor(
    private val generativeModel: GenerativeModel
) : CategoryRecommender {
    override suspend fun recommendCategory(description: String): Recommendation? {
        val prompt = """
            Eres un clasificador inteligente para la aplicación MovilExplora. Tu tarea es analizar la descripción de un lugar o evento y clasificarlo en UNA de las siguientes cinco categorías, basándote estrictamente en los ejemplos proporcionados:

            1. Gastronomia: Restaurantes, cafés, comida callejera, cafeterías, reposterías, mercados gastronómicos, locales de comida rápida, tabernas, bares de tapas o cualquier lugar/evento culinario.
            2. Cultura: Museos, galerías de arte, monumentos, teatros de artes escénicas, centros culturales, bibliotecas, ferias de artesanías, exposiciones de arte o talleres creativos y educativos.
            3. Naturaleza: Parques, miradores, senderos para caminatas, reservas ecológicas, playas, lagos, ríos, montañas, áreas de campamento o cualquier actividad y destino natural al aire libre.
            4. Entretenimiento: Bares, discotecas, salas de conciertos, clubes nocturnos, parques de diversión, teatros de comedia/comerciales, cines, boleras, salas de juego o eventos festivos y de ocio.
            5. Historia: Sitios históricos, ruinas arqueológicas, estatuas/monumentos antiguos, castillos, templos antiguos, archivos de historia, plazas coloniales o recorridos históricos guiados.

            Instrucciones de formato:
            Debes responder en español utilizando EXACTAMENTE el siguiente formato de dos líneas, sin añadir ningún otro texto, ni asteriscos, ni marcas adicionales:
            CATEGORIA: [Elegir estrictamente una de estas opciones exactas: Gastronomia, Cultura, Naturaleza, Entretenimiento, Historia]
            RAZON: [Una breve explicación en español, de máximo 15 palabras, que justifique por qué se seleccionó esa categoría basándose en la descripción]

            Descripción a clasificar:
            $description
        """.trimIndent()
        
        return try {
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: return Recommendation("Gastronomia", "Sin respuesta de IA")
            
            val cleanText = text.replace("*", "").trim()
            val lines = cleanText.lines()
            
            val categoryLine = lines.find { it.contains("CATEGORIA", ignoreCase = true) }
            val reasonLine = lines.find { it.contains("RAZON", ignoreCase = true) }
            
            val validCategories = listOf("Gastronomia", "Cultura", "Naturaleza", "Entretenimiento", "Historia")
            
            var category = categoryLine?.substringAfter(":")?.trim()?.let { raw ->
                validCategories.find { it.equals(raw, ignoreCase = true) }
                    ?: validCategories.find { raw.contains(it, ignoreCase = true) }
            }
            
            if (category == null) {
                category = validCategories.find { cleanText.contains(it, ignoreCase = true) } ?: "Gastronomia"
            }
            
            val reason = reasonLine?.substringAfter(":")?.trim()
                ?: cleanText.substringAfter("RAZON:").trim().takeIf { it.isNotEmpty() }
                ?: "Sugerido por IA"
            
            Recommendation(category, reason)
        } catch (e: Exception) {
            Recommendation("Gastronomia", "Error: ${e.message?.take(25)}")
        }
    }
}
