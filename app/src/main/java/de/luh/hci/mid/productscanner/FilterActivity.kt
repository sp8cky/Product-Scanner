package de.luh.hci.mid.productscanner

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import de.luh.hci.mid.productscanner.ui.theme.Blue40
import de.luh.hci.mid.productscanner.ui.navigationbar.BottomNavigationBar
import de.luh.hci.mid.productscanner.ui.navigationbar.TopNavigationBar

// DataStore für Kontext
private val Context.dataStore by preferencesDataStore("filter_preferences")

class FilterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filterRepository = FilterRepository(this)

        setContent {
            val filters = filterRepository.filters.collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()

            FilterScreen(
                filters = filters.value,
                onFilterToggled = { filterLabel, isActive ->
                    scope.launch {
                        filterRepository.saveFilter(filterLabel, isActive)
                    }
                },
                onSpeakerClicked = { /* Action for Lautsprecher */ }
            )
        }
    }
}

data class FilterOption(
    val label: String,
    val isActive: Boolean
)

class FilterRepository(context: Context) {

    private val dataStore = context.dataStore

    // Schlüssel für Filter
    private val vegetarianKey = booleanPreferencesKey("vegetarian")
    private val veganKey = booleanPreferencesKey("vegan")
    private val nutFreeKey = booleanPreferencesKey("nut_free")
    private val soyFreeKey = booleanPreferencesKey("soy_free")
    private val glutenFreeKey = booleanPreferencesKey("gluten_free")
    private val lactoseFreeKey = booleanPreferencesKey("lactose_free")

    // Filterstatus abrufen
    val filters = dataStore.data.map { preferences ->
        listOf(
            FilterOption("Vegetarisch", preferences[vegetarianKey] ?: false),
            FilterOption("Vegan", preferences[veganKey] ?: false),
            FilterOption("Nussfrei", preferences[nutFreeKey] ?: false),
            FilterOption("Sojafrei", preferences[soyFreeKey] ?: false),
            FilterOption("Glutenfrei", preferences[glutenFreeKey] ?: false),
            FilterOption("Milch/Laktosefrei", preferences[lactoseFreeKey] ?: false)
        )
    }

    // Filterstatus speichern
    suspend fun saveFilter(label: String, isActive: Boolean) {
        dataStore.edit { preferences ->
            when (label) {
                "Vegetarisch" -> preferences[vegetarianKey] = isActive
                "Vegan" -> preferences[veganKey] = isActive
                "Nussfrei" -> preferences[nutFreeKey] = isActive
                "Sojafrei" -> preferences[soyFreeKey] = isActive
                "Glutenfrei" -> preferences[glutenFreeKey] = isActive
                "Milch/Laktosefrei" -> preferences[lactoseFreeKey] = isActive
            }
        }
    }
}

@Composable
fun FilterScreen(
    filters: List<FilterOption>,
    onFilterToggled: (String, Boolean) -> Unit,
    onSpeakerClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopNavigationBar(title = "Filter") },
        bottomBar = { BottomNavigationBar(navController = null) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filterliste mit Toggles
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filters.forEach { filter ->
                    SettingItem(
                        label = filter.label,
                        isChecked = filter.isActive,
                        onCheckedChange = { isChecked ->
                            onFilterToggled(filter.label, isChecked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingItem(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Switch(
            checked = isChecked,
            onCheckedChange = { onCheckedChange(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Blue40,
                uncheckedThumbColor = Color.Gray
            )
        )
    }
}
