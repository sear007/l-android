package co.ltlabs.ltmechanic.ui.main.main_helper

/*

fun isMobileDataEnabled(context: Context): Boolean? {
    val connectivityService =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
    val cm = connectivityService as ConnectivityManager
    return try {
        val c = Class.forName(cm.javaClass.name)
        val m: Method = c.getDeclaredMethod("getMobileDataEnabled")
        m.isAccessible = true
        m.invoke(cm) as Boolean
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun checkNetworkStatus(context: Context): String? {
    var networkStatus = ""
    val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    networkStatus = when (manager.activeNetworkInfo.type) {
        ConnectivityManager.TYPE_WIFI -> {
            "wifi"
        }
        ConnectivityManager.TYPE_MOBILE -> {
            "mobileData"
        }
        else -> {
            "noNetwork"
        }
    }
    return networkStatus
}

private fun networkAvailable(context: Context): Boolean {
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
        return getNetworkCapabilities(activeNetwork)?.run {
            when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } ?: false
    }
}

private fun hostAvailable(host: String, port: Int): Boolean {
    return try {

        if (ConnectionUtil.hasInternetConnected(applicationContext)) {
            mainScope.launch {
                ConnectionUtil.setInternetConnected(true)
                if (!loggedOut) {
                    dismissPopup()
                }
            }
        } else {
            mainScope.launch {
                if (!switching) {
                    ConnectionUtil.setInternetConnected(false)
                    if (telephonyManager.isDataEnabled) {
                        if (!loggedOut) {
                            dismissPopup()
                        }
                    } else {
                        showPopupWindow(
                            findViewById(R.id.relativeLayout),
                            showSignalNotificationWindow()
                        )
                    }
                }
            }
        }
        true

    } catch (e: IOException) {
        mainScope.launch {
            if (!switching) {
                if (telephonyManager.isDataEnabled) {
                    if (!loggedOut) {
                        dismissPopup()
                    }
                } else {
                    showPopupWindow(
                        findViewById(R.id.relativeLayout),
                        showSignalNotificationWindow()
                    )
                }
            }
        }
        false
    }

    private fun showOverlayPopupWindow(view: View, popupWindowType: PopupWindow) {
        val dm = DisplayMetrics()
        this.windowManager?.defaultDisplay?.getMetrics(dm)

        dismissPopup()
        popupWindow = popupWindowType
        popupWindow?.isOutsideTouchable = true

        view.post {
            popupWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
        }

        DimUtil.dimBehind(popupWindow)

    }

    private fun showPopupOverlayWindow(): PopupWindow {

        val inflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = PopupSignalNotificationBinding.inflate(inflater)

        return PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun initConnectivityEventUtil() {

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

//                Log.d(TAG, "onReceive: ping isConnected(): ${isConnected()}")

                IOScope.launch {
                    try {
                        val connected = isConnected()
                        mainScope.launch {
                            Log.d(TAG, "onCreate: connected: $connected")
                            if (!connected) {
//                                showPopupWindow(findViewById(R.id.relativeLayout), showSignalNotificationWindow())
                                if (telephonyManager.isDataEnabled) {
                                    if (!loggedOut) {
                                        dismissPopup()
                                    }
                                } else {
                                    showPopupWindow(
                                        findViewById(R.id.relativeLayout),
                                        showSignalNotificationWindow()
                                    )
                                }
                            } else {
                                if (!loggedOut) {
                                    dismissPopup()
                                }
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "onCreate: ", e)
                    }
                }


            }

        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        applicationContext.registerReceiver(receiver, intentFilter)
    }

    private fun initWifiEventUtil() {
        val wifiEventReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                when (wifi.wifiState) {

                    WifiManager.WIFI_STATE_DISABLING -> {
                        progressBar.showProgressBar(true)
                    }

                    WifiManager.WIFI_STATE_DISABLED -> {
                        progressBar.showProgressBar(false)
                        if (telephonyManager.isDataEnabled) {
                            if (!loggedOut) {
                                dismissPopup()
                            }
                        } else {
                            showPopupWindow(
                                findViewById(R.id.relativeLayout),
                                showSignalNotificationWindow()
                            )
                        }

                    }

                    WifiManager.WIFI_STATE_ENABLING -> {
                        progressBar.showProgressBar(true)
                    }

                    WifiManager.WIFI_STATE_ENABLED -> {
                        progressBar.showProgressBar(false)

                        IOScope.launch {
                            try {
                                val connected = isConnected()
                                mainScope.launch {
                                    if (!connected) {
                                        showPopupWindow(
                                            findViewById(R.id.relativeLayout),
                                            showSignalNotificationWindow()
                                        )
                                    } else {
                                        if (!loggedOut) {
                                            dismissPopup()
                                        }
                                    }
                                }
                            } catch (e: java.lang.Exception) {
                                Timber.tag(TAG).e(e)
                            }
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)

        applicationContext.registerReceiver(wifiEventReceiver, intentFilter)
    }

}*/


/*if (AuthUtil.role == UserType.LINE_LEADER) {
            navigationView.menu.findItem(R.id.lineLeaderReportedTicketsFragment)?.setOnMenuItemClickListener {

                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToLineLeaderReportedTickets()

                true

            }

            navigationView.menu.findItem(R.id.lineLeaderInRepairTicketsFragment)?.setOnMenuItemClickListener {

                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToLineLeaderInRepairTickets()

                true

            }

            navigationView.menu.findItem(R.id.lineLeaderRepairedTicketsFragment)?.setOnMenuItemClickListener {

                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToLineLeaderRepairedTickets()

                true

            }

            navigationView.menu.findItem(R.id.create_ticket)?.setOnMenuItemClickListener {

                action = "create_ticket"

                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .7).toInt()

                dismissPopup()
                popupWindow = showScanPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

                true

            }
        }
        else {
            navigationView.menu.findItem(R.id.mechanicReportedTicketsFragment)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToMechanicReportedTickets()

                true

            }

            navigationView.menu.findItem(R.id.mechanicInRepairTicketsFragment)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToMechanicInRepairTickets()

                true

            }

            navigationView.menu.findItem(R.id.mechanicRepairedTicketsFragment)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToMechanicRepairedTickets()

                true

            }

            navigationView.menu.findItem(R.id.lineStatusFragment)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToLineOverview()

                true

            }

            navigationView.menu.findItem(R.id.replace_machine)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)


                action = "replace"

                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .7).toInt()

                dismissPopup()
                popupWindow = showScanPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

                true

            }

            navigationView.menu.findItem(R.id.setupLineFragment)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToSetupLine()
                true

            }



            navigationView.menu.findItem(R.id.move_machine)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                action = "move_machine"

                val dm = DisplayMetrics()
                activity?.windowManager?.defaultDisplay?.getMetrics(dm)

                val width = (dm.widthPixels * .9).toInt()
                val height = (dm.heightPixels * .7).toInt()

                dismissPopup()
                popupWindow = showScanPopupWindow()
                popupWindow?.isOutsideTouchable = true
                popupWindow?.isFocusable = true
                popupWindow?.update(0, 0, width, height)
                popupWindow?.showAtLocation(binding.root, Gravity.CENTER, 0, -25)

                popupWindow?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

                true

            }

            navigationView.menu.findItem(R.id.maintenanceFragment)?.setOnMenuItemClickListener {
                activity?.findViewById<DrawerLayout>(R.id.drawer_layout)?.closeDrawer(GravityCompat.START)

                navigateToMaintenance()

                true

            }
        }
        */