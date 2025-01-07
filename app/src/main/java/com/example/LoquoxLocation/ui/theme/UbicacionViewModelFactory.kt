import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.LoquoxLocation.ui.theme.UbicacionViewModel

class UbicacionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UbicacionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UbicacionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
