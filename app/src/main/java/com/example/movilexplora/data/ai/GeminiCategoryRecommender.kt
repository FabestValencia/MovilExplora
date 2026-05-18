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
            Eres un clasificador. Devuelve SOLO una línea con este formato:
            CATEGORIA: [Elegir entre: Gastronomia, Cultura, Naturaleza, Entretenimiento, Historia]
            RAZON: [Breve explicación]
            
            Descripción: $description
        """.trimIndent()
        
        return try {
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: return Recommendation("Gastronomia", "Sin respuesta de IA")
            
            val cleanText = text.replace("*", "").trim()
            val lines = cleanText.lines()
            
            val categoryLine = lines.find { it.contains("CATEGORIA", ignoreCase = true) }
            val reasonLine = lines.find { it.contains("RAZON", ignoreCase = true) }
            
            if (categoryLine == null) {
                // Si no encontramos la línea, devolvemos el texto crudo para saber qué está pasando
                return Recommendation("Gastronomia", "Debug: ${cleanText.take(40)}")
            }

            val categoryRaw = categoryLine.substringAfter(":").trim()
            val reason = reasonLine?.substringAfter(":")?.trim() ?: "Sin razón específica"
            
            val validCategories = listOf("Gastronomia", "Cultura", "Naturaleza", "Entretenimiento", "Historia")
            val category = validCategories.find { it.equals(categoryRaw, ignoreCase = true) }
                ?: validCategories.find { categoryRaw.contains(it, ignoreCase = true) }
                ?: "Gastronomia"
            
            Recommendation(category, reason)
        } catch (e: Exception) {
            Recommendation("Gastronomia", "Error: ${e.message?.take(25)}")
        }
    }
}
