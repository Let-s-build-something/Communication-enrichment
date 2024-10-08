package data.io.user

import kotlinx.serialization.Serializable

/** user object specific to our database saved in custom DB */
@Serializable
data class UserIO(
    /** username of the current user */
    val username: String? = null,

    /** tag of the current user, unique in combination with [username]  */
    val tag: String? = null,

    /** current idToken which should be active and can be associated with data in Cloud Identity */
    val idToken: String? = null,

    /** current user's public id, generated by our BE - not associated with Firebase */
    val publicId: String? = null
)