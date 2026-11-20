package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fellowship models.
 *
 * Only the public stats are strongly typed. Programme config, centres, wizard payloads and dashboard
 * envelopes are config-driven / loosely typed on the backend, so they are mapped from JSON in
 * `ui/fellowship/FellowshipData.kt` (see FellowshipRepository). The previous typed request/response
 * classes did not match the live API (e.g. `title` vs `name`, `name` vs `institution_name`) and were removed.
 */
@Serializable
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
data class FellowshipStats(
    // Live API (GET /fellowship/stats/public):
    // {"completed_fellows","active_fellows","total_fellows","training_centers","procedures_logged"}
    @SerialName("active_fellows") val activeFellows: Int = 0,
    @SerialName("training_centers") val trainingCenters: Int = 0,
    @SerialName("completed_fellows") @kotlinx.serialization.json.JsonNames("graduated_fellows") val graduatedFellows: Int = 0,
    @SerialName("total_fellows") val totalFellows: Int = 0,
    @SerialName("master_trainers") val masterTrainers: Int = 0,
    @SerialName("procedures_logged") @kotlinx.serialization.json.JsonNames("total_cases_logged") val totalCasesLogged: Int = 0
)
