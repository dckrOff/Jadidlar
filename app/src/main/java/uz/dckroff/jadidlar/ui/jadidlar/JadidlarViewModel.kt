package uz.dckroff.jadidlar.ui.jadidlar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.dckroff.jadidlar.data.models.Jadid
import uz.dckroff.jadidlar.data.repository.JadidRepository
import uz.dckroff.jadidlar.utils.Resource

class JadidlarViewModel : ViewModel() {
    private val repository = JadidRepository()

    private val _jadids = MutableLiveData<Resource<List<Jadid>>>()
    val jadids: LiveData<Resource<List<Jadid>>> = _jadids

    init {
        loadAllJadids()
    }

    fun loadAllJadids() {
        _jadids.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getAllJadids()
            _jadids.value = result
        }
    }
}
