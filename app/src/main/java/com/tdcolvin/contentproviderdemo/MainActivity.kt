package com.tdcolvin.contentproviderdemo

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tdcolvin.contentproviderdemo.ui.theme.ContentProviderDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContentProviderDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BirthdaysScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BirthdaysScreen(modifier: Modifier = Modifier, viewModel: BirthdaysViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.refreshBirthdates() }

    val birthdates by viewModel.birthdates.collectAsStateWithLifecycle()

    LazyColumn(modifier = modifier) {
        item {
            Column {
                Text("ContentProvider app", style = MaterialTheme.typography.displayMedium)
                Text("This app contains a database of birthdates, shown below. Now it's installed, load the ContentProviderClientDemo app, from which you will be able to access this app's data and add to it.")
            }
        }
        item {
            Button(onClick = viewModel::refreshBirthdates) {
                Text("Refresh")
            }
        }
        items(items = birthdates) { item ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .border(border = BorderStroke(width = 2.dp, color = Color.Red), shape = RoundedCornerShape(10.dp))
                    .padding(10.dp),
                text = "${item.name} born ${item.birthdate}",
                fontSize = 25.sp
            )
        }
    }
}

class BirthdaysViewModel(app: Application): AndroidViewModel(app) {
    val birthdates: MutableStateFlow<List<UserBirthdate>> = MutableStateFlow(emptyList())

    fun refreshBirthdates() {
        viewModelScope.launch(Dispatchers.IO) {
            birthdates.value = BirthdatesDatabase(getApplication()).getBirthdates()
        }
    }
}