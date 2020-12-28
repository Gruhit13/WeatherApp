package com.example.weatherapp

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(){

    var CITY: String = "Anand"
    val API: String = "02cdeaa3cbd891e9005b7155a1f6d000"
    val TAG:String = "MainActivity:- "


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Executing weather task
        weatherTask().execute()

        //  Refresh the content on click
        refresh.setOnClickListener(){
            Log.i(TAG, "Refreshing content")
            weatherTask().execute()
            Toast.makeText(this, "Weather Data is Up-to-Date", Toast.LENGTH_LONG).show()
        }

        //  Handling EditText content change and Keyboard DONE button
        address.setOnEditorActionListener{
            v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                Log.i("EDITTEXT", "Text changed from here")
                changeCity();
                true
            } else{
                Log.i("EDITTEXT", "Text Could not be changed from here")
                false
            }
        }

        //  Handling EditText on click
        address.setOnClickListener(){
            //  Requesting Focus for EditText
            address.requestFocus()

            //  Selecting all the content present in EditText
            address.selectAll()

            //  Setting up the cursor for EditText
            address.setCursorVisible(true)
        }

        //  Handling Back button
        this@MainActivity.onBackPressedDispatcher?.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                //  Creating alert box when back is pressed
                val builder = AlertDialog.Builder(this@MainActivity, R.style.AlertDialogStyle)
                builder.setMessage("Do you want to exit application?")

                builder.setPositiveButton("Yes"){
                    dialog, id -> finish()
                }

                builder.setNegativeButton("No"){
                    dialog, which -> Toast.makeText(this@MainActivity, "Enjoy Weather", Toast.LENGTH_SHORT).show()
                }
                val dialog = builder.create()
                dialog.show()
            }
        })
    }

    public fun changeCity(){
        val cityName:String? = address.text.toString()

        if(cityName == null){
            Toast.makeText(this, "Must enter a valid city name", Toast.LENGTH_LONG).show()
            return
        }

        CITY = cityName!!

        //  Hiding the softKeyboard
        val controller = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        controller.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

        //  Dis-selecting the text and disabling Cursor
        address.setSelectAllOnFocus(false)
        address.setCursorVisible(false)

        //  Removing the focus and the
        address.clearFocus()

        //  Execute weatherTask with new city
        weatherTask().execute()

        Toast.makeText(this, "Dispaying weather for " + CITY, Toast.LENGTH_LONG).show()
    }

    inner class weatherTask(): AsyncTask<String, Void, String>()
    {
        val EXCPTTAG:String = "EXCEPTION"
        override fun onPreExecute() {
            Log.i(TAG, "Refresing in onPreExecute")
            super.onPreExecute()
            //  Showing the progress bar in beginning
            findViewById<ProgressBar>(R.id.Loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            Log.i(TAG, "Refresing in doInbackground")
            var response: String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API")
                        .readText(Charsets.UTF_8)
            } catch(e: Exception){
                //  Displying the error in Logs for Debugging
                Log.i(EXCPTTAG , "Error in reading data from API")
                Log.i(EXCPTTAG, "Exception " + e)
                response = null
            }

            return response //  Returning response
        }

        //  Fething the Data from the json Object of API
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try{
                Log.i(TAG, "Refresing content onPostExecute")
                val jsonObj = JSONObject(result)

                //  Getting the main content from the json object obtained from API
                val main = jsonObj.getJSONObject("main")

                //  Getting system info from the jsonObject
                val sys = jsonObj.getJSONObject("sys")

                //  Getting info. for Winds from object
                val wind = jsonObj.getJSONObject("wind")

                //  Getting weather info from the jsonObj
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                //  Getting Updated at time
                val updatedAt:Long = jsonObj.getLong("dt")

                //  Converting obtained date into a readabel date format
                //  Here updated at does not depend on User updation but on API updatetion
                val updatedAtText = "Updated at: "+SimpleDateFormat("dd/mm/yyyy HH:mm a", Locale.getDefault()).format(Date(updatedAt*1000))
                //  Getting temperature
                //  Obtained in float so convert toInt then toString
                val temp = main.getString("temp")+"°C"

                //  Getting minimum and maximum temperatur
                val tempMin = "Min Temp:" + main.getString("temp_min")+"°C"
                val tempMax = "Max Temp" + main.getString("temp_max")+"°C"

                //  Getting pressure from main
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                //  Getting Sunrise & Sunset
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")

                //  Getting wind speed
                val windSpeed = wind.getString("speed")

                //  Description of weather
                val weatherDescription = weather.getString("description")

                //  Getting address
                val address = jsonObj.getString("name")+ ", " +sys.getString("country")

                //  Populating TextViews with respective datas
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                //  Disableing ProgressBar and enabling Layout
                findViewById<ProgressBar>(R.id.Loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch(e: Exception){
                //  If any exception is caught then display the error text
                Log.i(EXCPTTAG, "Exception:- " + e)
                findViewById<ProgressBar>(R.id.Loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }
    }
}