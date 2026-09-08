package dev.gabrielchl.intellijPets.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import dev.gabrielchl.intellijPets.utils.Constants

@Service
@State(
    name = "dev.gabrielchl.intellijpets.settings.AppSettingsState",
    storages = [Storage("IntellijPetsSettings.xml")]
)
class PetsSettings: PersistentStateComponent<PetsSettings.State> {
    class State {
        var petList = arrayListOf(Constants.DEFAULT_PET)
        var petScale = Constants.DEFAULT_SCALE
    }

    companion object {
        val instance: PetsSettings
            get() = ApplicationManager.getApplication().getService(PetsSettings::class.java)
    }

    var petsState: State = State()

    override fun getState(): State {
        return petsState
    }

    override fun loadState(state: State) {
        state.petList = ArrayList(state.petList.map(Constants::validPetType))
        state.petScale = Constants.validPetScale(state.petScale)
        this.petsState = state
    }
}
