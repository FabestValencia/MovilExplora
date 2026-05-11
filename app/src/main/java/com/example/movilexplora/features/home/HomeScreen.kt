package com.example.movilexplora.features.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movilexplora.R
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@Composable
fun HomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo central
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.homescreen_logo_0),
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Título principal
        Text(
            text = stringResource(R.string.home_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        Text(
            text = stringResource(R.string.home_desc),
            fontSize = 16.sp,
            color = GrayText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón Crear cuenta
        Button(
            onClick = onNavigateToRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Turquoise,
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.home_create_account),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón Iniciar sesión
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.home_login),
                color = Turquoise,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
