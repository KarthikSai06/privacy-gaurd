package com.privacyguard.ui.screens.breach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class BreachInfo(
    val name: String,
    val domain: String,
    val breachDate: String,
    val dataClasses: List<String>,
    val description: String
)

data class BreachUiState(
    val email: String = "",
    val breaches: List<BreachInfo> = emptyList(),
    val isLoading: Boolean = false,
    val hasChecked: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BreachViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(BreachUiState())
    val state: StateFlow<BreachUiState> = _state.asStateFlow()

    fun checkBreaches(email: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, email = email) }

            try {
                val breaches = withContext(Dispatchers.IO) {
                    fetchBreaches(email)
                }
                _state.update { it.copy(
                    breaches = breaches,
                    isLoading = false,
                    hasChecked = true
                )}
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    hasChecked = true,
                    error = e.message ?: "Failed to check breaches"
                )}
            }
        }
    }

    private fun fetchBreaches(email: String): List<BreachInfo> {
        val url = URL("https://haveibeenpwned.com/api/v3/breachedaccount/${email}?truncateResponse=false")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("hibp-api-key", "")  // User must provide
        connection.setRequestProperty("User-Agent", "PrivacyGuard-Android")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        return when (connection.responseCode) {
            200 -> {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(response)
                (0 until jsonArray.length()).map { i ->
                    val obj = jsonArray.getJSONObject(i)
                    val dataClasses = mutableListOf<String>()
                    val dcArray = obj.getJSONArray("DataClasses")
                    for (j in 0 until dcArray.length()) dataClasses.add(dcArray.getString(j))

                    BreachInfo(
                        name = obj.getString("Name"),
                        domain = obj.optString("Domain", "Unknown"),
                        breachDate = obj.getString("BreachDate"),
                        dataClasses = dataClasses,
                        description = obj.optString("Description", "").take(200)
                    )
                }
            }
            404 -> emptyList() // No breaches found
            else -> throw Exception("API returned ${connection.responseCode}")
        }
    }
}
