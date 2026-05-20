package com.example.movilexplora.core.utils

import com.example.movilexplora.features.map.MapFeature

fun List<MapFeature>.toGeoJson(): String {
    val features = joinToString(",") { feature ->
        val type = if (feature is MapFeature.PostFeature) "post" else "event"
        """
        {
          "type": "Feature",
          "id": "${feature.id}",
          "geometry": {
            "type": "Point",
            "coordinates": [${feature.longitude}, ${feature.latitude}]
          },
          "properties": {
            "title": "${feature.title}",
            "category": "${feature.category}",
            "type": "$type"
          }
        }
        """.trimIndent()
    }
    return """
    {
      "type": "FeatureCollection",
      "features": [$features]
    }
    """.trimIndent()
}
