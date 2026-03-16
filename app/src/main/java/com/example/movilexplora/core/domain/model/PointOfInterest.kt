package com.example.movilexplora.core.domain.model

import com.example.movilexplora.core.domain.enums.PriceRange
import com.example.movilexplora.core.domain.enums.TouristCategory

data class PointOfInterest (
    var id: String = "",
    val title: String,
    val category: TouristCategory,
    val description: String,
    val priceRange: PriceRange,
    val location: Location,
    val suggestedHours: String, // Ejemplo: "08:00 - 18:00"
    val photoUrl: String, // URL de la imagen gestionada externamente
    val ownerId: String,
    val verified: Boolean = false // Se crea sin estar verificada por defecto [8]
)
