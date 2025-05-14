import com.buoyancy.playback.model.music.SavedTrack
import com.google.gson.annotations.SerializedName

class SavedTracksResponse (
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("previous") val previousPage: String,
    @SerializedName("next") val nextPage: String,
    @SerializedName("items") val tracks: List<SavedTrack>
)