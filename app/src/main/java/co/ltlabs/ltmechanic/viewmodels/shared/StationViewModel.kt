package co.ltlabs.ltmechanic.viewmodels.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ltlabs.ltmechanic.domain.MachineInStation
import co.ltlabs.ltmechanic.network.main.MachineApi
import co.ltlabs.ltmechanic.network.main.dto.asMachineInStationDomainModel
import co.ltlabs.ltmechanic.util.ApiStatus
import co.ltlabs.ltmechanic.util.AuthUtil
import co.ltlabs.ltmechanic.util.getTranslation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "StationViewModel";

class StationViewModel @Inject constructor(
    private val machineApi: MachineApi,
    private val languageJsonObject: JSONObject
) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

    private val _machinesInStation = MutableLiveData<MutableList<MachineInStation>>()
    val machinesInStation: LiveData<MutableList<MachineInStation>>
        get() = _machinesInStation

    val stationsTemp = mutableListOf<MachineInStation>()

    fun getMachinesInStation(mfgLineId: Long, userId: Int = 1, module: String = "") {
        viewModelScope.launch {
            val getMachinesByLinePlacementDeferred = machineApi.getMachinesByLinePlacementAsync(
                mfgLineId,
                userId,
                "Bearer ${AuthUtil.token}"
            )
            try {
                _status.value = ApiStatus.LOADING
                val result = getMachinesByLinePlacementDeferred.await()

                if (result.machine.isNotEmpty()) {

                    val machinesInStation = result.asMachineInStationDomainModel().toMutableList()
//                    machinesInStation.map {
//                        it.station = if (it.station[0].toString() == "0") it.station else "0${it.station}"
//                    }

                    // If the list from api does not have station "01", add to the list.
                    if (machinesInStation.none { it.station == "01" }) {
                        machinesInStation.add(
                            MachineInStation(
                                0,
                                "01",
                                languageJsonObject.getTranslation("empty"),
                                "",
                                "",
                                0,
                                empty = true
                            )
                        )
                    }

                    // Separate stations that doesn't have "-A"
                    val machinesInStationWithoutA =
                        machinesInStation.filter { !it.station.contains("-A") }
                            .sortedBy { it.station.toInt() }.toMutableList()

                    // Create empty list for complete list of stations (with and without -A)
                    val completeListMachinesInStation = mutableListOf<MachineInStation>()

                    // Create empty list for stations with "-A"
                    val completeListMachinesInStationWithA = mutableListOf<MachineInStation>()

                    // Create list for empty stations
                    val emptyStations = mutableListOf<MachineInStation>()

                    // Loop the the list of stations that doesn't have "-A"
                    for ((index, value) in machinesInStationWithoutA.withIndex()) {
                        if (index < machinesInStationWithoutA.size - 1) {

                            // Get the current station number
                            val currentStation = machinesInStationWithoutA[index].station.toInt()

                            // Get the next station number
                            val nextStation = machinesInStationWithoutA[index + 1].station.toInt()

                            // Calculate the difference of current station and next station
                            val countBetween = nextStation - currentStation

                            machinesInStationWithoutA[index].index = index

                            // If the calculated difference is greater that one,
                            // there is an empty station(s) between the current and next station
                            if (countBetween > 1) {
                                // Loop the distance/difference of current and next station
                                for (count in (currentStation + 1) until nextStation) {
                                    // Add the emppty station to empty list
                                    emptyStations.add(
                                        MachineInStation(
                                            0,
                                            if (count > 9) "$count" else "0${count}",
                                            languageJsonObject.getTranslation("empty"),
                                            "",
                                            "",
                                            count,
                                            empty = true
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Create list to separate stations with "-A"
                    val machinesInStationWithA =
                        machinesInStation.filter { it.station.contains("-A") }
                            .sortedBy { it.station }.toMutableList()

                    // Add all the content of empty stations to the machine list without "-A"
                    machinesInStationWithoutA.addAll(emptyStations)

                    // Sort the stations by station number
                    machinesInStationWithoutA.sortBy { it.station.toInt() }

                    // Loop the machines without "-A"
                    machinesInStationWithoutA.forEachIndexed { index, value ->

                        // Add the station to the complete list
                        completeListMachinesInStation.add(value)

                        // Check every station if the station has a match station in the
                        // list of machines with "-A"
                        if (machinesInStationWithA.any {
                                it.station.replace(
                                    "-A",
                                    ""
                                ) == value.station
                            }) {
                            // Check the list size to avoid index out of bounds
                            if (machinesInStationWithA.isNotEmpty()) {
                                // Get the matched station from the list of stations with "-A"
                                val machineWithA =
                                    machinesInStationWithA.filter { it.station.contains(value.station) }[0]

                                // Add the station with "-A" to the complete list
                                completeListMachinesInStation.add(machineWithA)

                                // Remove the station from the list of station with "-A"
                                machinesInStationWithA.remove(machineWithA)
                            }
                        }

                    }

                    // This scenario is if the complete list of station size is 1,
                    // but there are next stations from the list of machines with "-A"
                    // that we have to append to the end.
                    // We have to calculate the distance of the first station and the next
                    // following stations.
                    if (completeListMachinesInStation.size == 1) {

                        // Loop the complete list of stations
                        completeListMachinesInStation.forEach { machine ->

                            // Check the size to avoid index out of bounds
                            if (machinesInStationWithA.isNotEmpty()) {

                                // Check the current and next station number
                                // Get the difference/distance
                                val currentStation = machine.station.toInt()
                                val nextStation =
                                    machinesInStationWithA[0].station.replace("-A", "").toInt()
                                val countBetween = nextStation - currentStation

                                if (countBetween > 1) {

                                    // Loop the distance
                                    for (count in (currentStation + 1) until nextStation + 1) {
                                        // Add the empty stations to the complete list of stations
                                        completeListMachinesInStation.add(
                                            MachineInStation(
                                                0,
                                                if (count > 9) "$count" else "0${count}",
                                                languageJsonObject.getTranslation("empty"),
                                                "",
                                                "",
                                                count,
                                                empty = true
                                            )
                                        )

                                        // Check every station if the station has a match station in the
                                        // list of machines with "-A"
                                        if (machinesInStationWithA.any {
                                                it.station.replace(
                                                    "-A",
                                                    ""
                                                ) == if (count > 9) "$count" else "0${count}"
                                            }) {
                                            if (machinesInStationWithA.isNotEmpty()) {
                                                // Get the matched station from the list of stations with "-A"
                                                val machineWithA = machinesInStationWithA.filter {
                                                    it.station.contains(if (count > 9) "$count" else "0${count}")
                                                }[0]

                                                // Add the station with "-A" to the complete list
                                                completeListMachinesInStation.add(machineWithA)

                                                // Remove the station from the list of station with "-A"
                                                machinesInStationWithA.remove(machineWithA)
                                            }
                                        }

                                    }
                                }

                                // Loop the list of stations with "-A"
                                machinesInStationWithA.forEachIndexed { index, machineWithA ->
                                    // Add the station to the complete list of station with "-A"
                                    completeListMachinesInStationWithA.add(machineWithA)

                                    if (index < machinesInStationWithA.size - 1) {

                                        // Check the current and next station number
                                        // Get the difference/distance
                                        val currentStationWithA =
                                            machineWithA.station.replace("-A", "").toInt()
                                        val nextStationWithA =
                                            machinesInStationWithA[index + 1].station.replace(
                                                "-A",
                                                ""
                                            ).toInt()
                                        val withACountBetween =
                                            nextStationWithA - currentStationWithA

                                        if (withACountBetween > 1) {
                                            // Loopt the distance
                                            for (count in (currentStationWithA + 1) until nextStationWithA + 1) {
                                                // add empty stations to the complete list of stations with "-A"
                                                completeListMachinesInStationWithA.add(
                                                    MachineInStation(
                                                        0,
                                                        if (count > 9) "$count" else "0${count}",
                                                        languageJsonObject.getTranslation("empty"),
                                                        "",
                                                        "",
                                                        count,
                                                        empty = true
                                                    )
                                                )
                                            }
                                        }

                                    }

                                }

                            }

                        }

                    } else {

                        // This scenario is for the list of station that has a first
                        // and last station.
                        // We have to calculate the distance between them.
                        // Check the size of the list of machines with a to avoid index out of bounds.
                        if (machinesInStationWithA.isNotEmpty()) {
                            val lastStationCompleteList =
                                completeListMachinesInStation[machinesInStationWithoutA.size - 1].station.replace(
                                    "-A",
                                    ""
                                ).toInt()
                            val firstStationWithA =
                                machinesInStationWithA[0].station.replace("-A", "").toInt()

                            // Loop the first and last stations distance
                            for (count in (lastStationCompleteList + 1) until firstStationWithA + 1) {
                                // add empty station to the complete list of station with "-A"
                                completeListMachinesInStationWithA.add(
                                    MachineInStation(
                                        0,
                                        if (count > 9) "$count" else "0${count}",
                                        languageJsonObject.getTranslation("empty"),
                                        "",
                                        "",
                                        count,
                                        empty = true
                                    )
                                )
                            }

                        }

                    }

                    // Add complete list of stations with "-A" to the complete list
                    completeListMachinesInStation.addAll(completeListMachinesInStationWithA)

                    // Add complete list of remaining stations with "-A" to the complete list
                    completeListMachinesInStation.addAll(machinesInStationWithA)

                    _status.value = ApiStatus.DONE

                    _machinesInStation.value = completeListMachinesInStation
                    stationsTemp.addAll(completeListMachinesInStation)

                } else {
                    _status.value = ApiStatus.DONE
                    _machinesInStation.value = null
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = ApiStatus.ERROR
            }
        }
    }
}