package com.example.barbershop

class BarberShopEngine(private val shopData: Shop) {
    val activeBarbers = ArrayDeque<Barber>()
    val waitingBarber = mutableListOf<Barber>()
    val waitingRoom = mutableListOf<Customer>()
    var customerCount = 0
    var start = 540
    val end = 1020
    val midShift = 780
    val arrivalMap : Map<Int, Int> = shopData.events.associate{ Utils().timeToMinutes(it.arrival_time) to it.haircut_duration}

    fun tick(minute: Int) {
        //while (minute < end || activeBarbers.isNotEmpty()) {
            val timeStr = formatTime(minute)
            if (minute == start) {

                println(" [$timeStr] Stellar Salon is open for business")
                shopData.barber_shifts["shift_1"]?.forEach {
                    activeBarbers.add(
                        Barber(
                            it, isFirstShift = true,
                        )
                    )
                    println("[${formatTime(minute)}] $it started shift")
                }
            }
            if (minute == midShift) {
                prepareSecondShift(minute)
            }
            if (minute == end) {
                handleClosing(minute)
            }
            val duration = arrivalMap[minute]
            if(duration != null) {
                handleNewCustomer(minute, timeStr, duration)
            }
            startNewHaircuts(minute)
            processFinishedHaircuts(minute)
            checkForWaitingBarbers(minute)
            checkForFrustratingCustomers(minute)

    }

    private fun checkForFrustratingCustomers(minute: Int) {
        if(waitingRoom.isNotEmpty()) {
            val frustratedCustomers = waitingRoom.filter { //check if waiting time is > 20
                minute - Utils().timeToMinutes(it.arrivalTime)  > 20
            }
            if(frustratedCustomers.isNotEmpty()) {
                frustratedCustomers.forEach { customer ->
                    waitingRoom.remove(customer)
                    println("[${formatTime(minute)}] ${customer.name} leaves Frustrated")
                }
            }
        }
    }

    fun handleClosing(minute: Int) {
        println("[${formatTime(minute)}] Stellar Salon is closed for business")
        //check for any waiting customers
        if(waitingRoom.isNotEmpty()) {
            waitingRoom.forEach { customer ->
                println("[${formatTime(minute)}] ${customer.name} leaves Cursing")
            }
        }
        waitingRoom.clear()
        activeBarbers.forEach { barber ->
            if(barber.currentCustomer!= null) println("[${formatTime(minute)}] ${barber.name} is finishing ${barber.currentCustomer!!.name}'s haircut")
            else println("[${formatTime(minute)}] ${barber.name} ended shift")
        }
        activeBarbers.clear()
    }

    private fun prepareSecondShift(minute: Int) {
        var iterator = activeBarbers.iterator()
        var vacatedChairs = 0
        while(iterator.hasNext()) {
            val barber = iterator.next()
            if(barber.isFirstShift && barber.currentCustomer == null) {
                iterator.remove()
                vacatedChairs ++
            } else {
                 println("[${formatTime(minute)}] ${barber.name} is finishing ${barber.currentCustomer!!.name}'s haircut")
            }
        }
        shopData.barber_shifts["shift_2"]?.forEach {
            if(vacatedChairs > 0) {
                activeBarbers.add(
                    Barber(
                        it, isFirstShift = false,
                    )
                )
                println("[${formatTime(minute)}] $it started shift")
                vacatedChairs--
            } else {
                waitingBarber.add(
                    Barber(
                        it, isFirstShift = false,
                    )
                )
            }


        }

    }
    private fun processFinishedHaircuts(minute: Int) {
        for(barber in activeBarbers) {
            if(barber.currentCustomer != null && barber.finishTime == minute ) {
                    println("[${formatTime(minute)}] ${barber.name} finished cutting ${barber.currentCustomer!!.name}'s hair")
                    println("[${formatTime(minute)}] ${barber.currentCustomer!!.name} leaves satisfied")
                    barber.currentCustomer = null
            }
        }

    }

    fun checkForWaitingBarbers(minute: Int) {
        val barbersFromFirstShift = activeBarbers.filter {
            barber -> barber.isFirstShift && barber.currentCustomer == null
        }
        if(waitingBarber.isNotEmpty() && barbersFromFirstShift.isNotEmpty()) {
            activeBarbers.removeAll(barbersFromFirstShift)
            barbersFromFirstShift.forEach { barber -> println("[${formatTime(minute)}] ${barber.name} ended shift")}

           repeat(minOf(barbersFromFirstShift.size, waitingBarber.size)){
               val nextBarber = waitingBarber.removeAt(0)
                activeBarbers.add(nextBarber)
                println("[${formatTime(minute)}] ${nextBarber.name} started shift")
            }

        }

    }
    private fun startNewHaircuts(minute: Int) {
        for (barber in activeBarbers) {
            if (barber.currentCustomer == null && waitingRoom.isNotEmpty()) {
                val nextCustomer = waitingRoom.removeAt(0) // Get the first person in line
                barber.currentCustomer = nextCustomer
                val duration = nextCustomer.haircutDuration
                barber.finishTime = minute + duration

                println("[${formatTime(minute)}] ${barber.name} started cutting ${nextCustomer.name}'s hair")
            }
        }
    }



    fun formatTime(minute: Int): String {
        val hours = minute / 60
        val minutes = minute % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun handleNewCustomer(minute: Int, timeStr: String, duration: Int) {
         customerCount++
        val customerName = "Customer-$customerCount"
        println("[${formatTime(minute)}] $customerName entered")
        //if all the barbers are busy, add to waiting list
        if (minute == end) {
            println("[${formatTime(minute)}] $customerName leaves Disappointed")
            return
        }
        // check if the shop is full
        val currentOccupancy = activeBarbers.size + waitingRoom.size
        if(currentOccupancy == 8) {
            println("[${formatTime(minute)}] $customerName leaves Unfulfilled")
        } else {
            waitingRoom.add(Customer(customerName, haircutDuration = duration, arrivalTime = timeStr))

        }
    }
}

data class Shop (
    val shop_name: String,
    val barber_shifts: Map<String, List<String>>,
    val events: List<Event>

)
data class Barber(
    val name: String,
    var currentCustomer: Customer? = null,
    val isFirstShift: Boolean,
    var finishTime: Int? = null,
)
data class Customer (
    val name: String,
    val haircutDuration: Int,
    val arrivalTime: String,
)

data class Event (
    val arrival_time: String,
    val haircut_duration: Int
)