package uz.dckroff.jadidlar.data.models

data class Jadid(
    val id: String = "",
    val name: String = "",
    val nameUz: String = "",
    val birthYear: Int = 0,
    val deathYear: Int = 0,
    val shortDescription: String = "",
    val fullDescription: String = "",
    val imageUrl: String = "",
    val orderIndex: Int = 0
)
