package co.ltlabs.ltmechanic.service

interface ConnectivityListener {
    fun onConnectionPoor()
    fun onConnectionStrong()
    fun scanStrongestWifi(currentSSID : String)
}