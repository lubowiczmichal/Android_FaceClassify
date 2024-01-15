package lubowicz.michal.faceclassify
import kotlinx.serialization.Serializable

@Serializable
data class AgeData (
    var age0to6: ArrayList<String> = arrayListOf(),
    var age07to14: ArrayList<String> = arrayListOf(),
    var age15to18: ArrayList<String> = arrayListOf(),
    var age19to24: ArrayList<String> = arrayListOf(),
    var age25to34: ArrayList<String> = arrayListOf(),
    var age35to49: ArrayList<String> = arrayListOf(),
    var age50to59: ArrayList<String> = arrayListOf(),
    var age60Plus: ArrayList<String> = arrayListOf()
)

