package com.example.productdealstrackerkotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_tracking_details_layout.*
import org.jetbrains.anko.doAsync
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), AddProductBottomSheetFragment.BottomSheetListener {

    val dummyList = ArrayList<CardData>()

    lateinit var toggle : ActionBarDrawerToggle //variable for the 3line sign at Top-left corner
    val db = Database(this, null) // Database object
    private val adapter = ProductListAdapter(dummyList,this, db)

    //Notification
    private val CHANNEL_ID = "channel_id_1"
    private val notificationId = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutInflater = findViewById<View>(R.id.emptyDatabasePrompt)
        // Navigation Menu
        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)


        if(isOnline(this)){
            Snackbar.make(drawerLayout, "Internet Found - Loading Items", Snackbar.LENGTH_SHORT).show()

        }else{

            Snackbar.make(drawerLayout, "No Internet Found", Snackbar.LENGTH_INDEFINITE).show()

            val builder = MaterialAlertDialogBuilder(this)
                .setTitle("Product Deals Tracker")
                .setCancelable(false)
                .setMessage("App Requires Internet Connectivity.\nTurn On Your Internet/Wi-Fi")
                .setPositiveButton("Try Again") { _,i -> recreate()}
                .setNegativeButton("Exit"){_,i->finish()}

            builder.show()

        }

        if(!isIgnoringBatteryOptimizations(this)){

            val name = resources.getString(R.string.app_name)

            val builder = MaterialAlertDialogBuilder(this)
                .setTitle("Product Deals Tracker")
                .setMessage("App Requires Background Activity.\nBattery optimization -> All apps -> $name -> Don't optimize")
                .setPositiveButton("Change") { _,i -> checkBattery(this)}
                .setNeutralButton("Exit"){_,i -> finish()}
                .setNegativeButton("Done"){_,i->recreate()}

            builder.show()

        }else{
            Snackbar.make(drawerLayout, "Battery Optimization Disabled - Okay", Snackbar.LENGTH_SHORT).show()
        }

        // DB Populate on Open/Restart
        if(db.isExist()){
            val noOfItems = dbpopulate()
        }

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            when(it.itemId){
                R.id.nav_home -> Toast.makeText(applicationContext,"Clicked Home",Toast.LENGTH_SHORT).show()
                R.id.nav_message -> Toast.makeText(applicationContext,"Clicked Message",Toast.LENGTH_SHORT).show()
                R.id.nav_trash -> Toast.makeText(applicationContext,"Clicked Delete",Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(applicationContext,"Clicked Settings",Toast.LENGTH_SHORT).show()
                R.id.nav_login -> Toast.makeText(applicationContext,"Clicked Login",Toast.LENGTH_SHORT).show()
                R.id.nav_share -> Toast.makeText(applicationContext,"Clicked Share",Toast.LENGTH_SHORT).show()
                R.id.nav_rate_us -> Toast.makeText(applicationContext,"Clicked Rate us",Toast.LENGTH_SHORT).show()

            }
            true
        }

        // Adapter For List View
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)


        title = "Product Deals Tracker" // App Title

        //Bottom Sheet Fragment
        var addProductBottomSheet = AddProductBottomSheetFragment()
        var floatingButton = findViewById<FloatingActionButton>(R.id.add_fab)

        floatingButton.setOnClickListener{

            addProductBottomSheet.show(supportFragmentManager,"BottomSheet")

        }

    }

    //Navigation Menu Item - On Selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun addProduct(url_input: String,budget: Int){

        doAsync {

            val card : CardData

            if(url_input.contains("flipkart"))
                card = FlipKartScraper.addProduct(url_input,budget)
            else
                card = AmazonScraper.addProduct(url_input,budget)

            db.addURL(url_input,budget)
            dummyList.add(0, card)
            adapter.notifyItemInserted(0)

            var actual_price = card.productPrice.substringAfter("₹").replace(",","")
//            println(actual_price)
//
//            try{
//
//                if(actual_price.toInt() <= budget.toInt()){
//
//                    print("Yes")
//                    createNotificationChannel()
//                    sendNotification(card.productName)
//                }
//
//            }catch (ex : NumberFormatException){
//                println("Null Input Exception")
//            }

        }

    }

    // Pass user input URL to addProduct() on button click
    override fun onAddItemButtonClicked(url_input: String,budget: Int,trackInterval:String) {
        addProduct(url_input,budget)

        if(url_input.isNotEmpty() and budget.toString().isNotEmpty()){
            setPeriodicWorkRequest(url_input,budget,trackInterval)
            Snackbar.make(drawerLayout,"Tracking Product $trackInterval",Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun dbpopulate():Int{

        var db_length = 0
        for((i,j) in db.getURL()){
            db_length +=1
            addProduct(i,j)
        }

        return db_length

    }

    private fun restartApp(){
        startActivity(Intent(applicationContext,MainActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    private fun setPeriodicWorkRequest(url_input: String, budget: Int,trackingValue: String){

        var trackInterval : Long = 0

        if(trackingValue.equals("Every Hour")) {
            trackInterval = 1
        }

        if(trackingValue.equals("Every 5 Hours")) {
            trackInterval = 5
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        if(trackingValue.contains("Minutes")){

            val workRequest = PeriodicWorkRequestBuilder<TrackerWorker>(15,TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(url_input)
                .setInputData(workDataOf("url" to url_input,
                    "budget" to budget)
                )
                .build()

            WorkManager.getInstance(applicationContext).enqueue(workRequest)

        }

        else{

            val workRequest = PeriodicWorkRequestBuilder<TrackerWorker>(trackInterval,TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag(url_input)
                .setInputData(workDataOf("url" to url_input,
                    "budget" to budget)
                )
                .build()

            WorkManager.getInstance(applicationContext).enqueue(workRequest)
        }

    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

    fun checkBattery(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
        recreate()
    }

    fun checkEmptyDatabase() {
        if (adapter.itemCount == 0) {
//            recycler_view.visibility = View.INVISIBLE
            emptyDatabasePrompt.visibility = View.VISIBLE
        }
        else {
////            emptyDatabasePrompt.visibility = View.GONE
//            recycler_view.visibility = View.VISIBLE
            dbpopulate()
        }

    }

    fun isOnline(context: Context): Boolean {

        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false

    }

}