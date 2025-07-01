package com.madtitan.estimator.feature_auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.madtitan.estimator.feature_auth.data.AdminRepository
import kotlinx.coroutines.launch


@Composable
fun PendingUsersScreen(adminRepository: AdminRepository) {
    val coroutineScope = rememberCoroutineScope()
    val pendingUsers by adminRepository.getPendingUsers().collectAsState(initial = emptyList())

    LazyColumn {
        items(pendingUsers) { user ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = user.name, fontWeight = FontWeight.Bold)
                    Text(text = user.email, fontSize = 14.sp, color = Color.Gray)

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { coroutineScope.launch { adminRepository.approveUser(user.id)  } }) {
                            Text("Approve")
                        }
                        Button(
                            onClick = { coroutineScope.launch { adminRepository.makeAdmin(user.id) } },
                            colors = ButtonDefaults.buttonColors(containerColor  = Color.Red)
                        ) {
                            Text("Make Admin", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
