package com.example.movilexplora.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.movilexplora.core.component.ButtonReu
import com.example.movilexplora.R


@Composable
fun HomeScreen(){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)

    ) {
        Image(
            modifier = Modifier.width(400.dp),
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
        )
        Text(
            text = "Descubre tu siguiente aventura"
        )

        ButtonReu(
            icon = Icons.Default.Done,
            contentDescription = "Icono de login",
            onClick = { /*TODO*/ },
            text = "Iniciar sesion"
        )

        ButtonReu(
            icon = Icons.Default.Person,
            contentDescription = "Icono de registro",
            onClick = { /*TODO*/ },
            text = "Registrarse"
        )
    }

}