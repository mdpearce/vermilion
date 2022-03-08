import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Composable
fun rememberVermilionAppState(): VermilionAppState =
    rememberSaveable(saver = VermilionAppState.Saver) {
        VermilionAppState()
    }

class VermilionAppState {
    private val _appBarClicks = MutableSharedFlow<AppBarClicked>()
    val appBarClicks = _appBarClicks.asSharedFlow()

    suspend fun onAppBarClicked() {
        _appBarClicks.emit(AppBarClicked)
    }

    companion object {
        val Saver: Saver<VermilionAppState, *> = listSaver(
            save = { listOf<Unit>() },
            restore = { VermilionAppState() }
        )
    }
}

object AppBarClicked
