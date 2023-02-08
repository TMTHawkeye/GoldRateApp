package live.gold.rates.metal.prices.app.ViewPager

import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.chartcore.common.ChartTypes
import com.github.chartcore.data.chart.ChartCoreModel
import com.github.chartcore.data.chart.ChartData
import com.github.chartcore.data.dataset.ChartNumberDataset
import com.github.chartcore.data.option.ChartOptions
import com.github.chartcore.data.option.elements.Elements
import com.github.chartcore.data.option.plugin.BackgroundColor
import com.github.chartcore.data.option.plugin.Plugin
import com.github.chartcore.data.option.plugin.Tooltip
import com.github.chartcore.view.ChartCoreView
import live.gold.rates.metal.prices.app.Model.Country
import live.gold.rates.metal.prices.app.Model.MetalInfoModel
import live.gold.rates.metal.prices.app.databinding.FragmentViewpagerBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.util.*
import java.util.concurrent.Executors


class ViewPagerFragment(var metalType: String) : Fragment(),
    AdapterView.OnItemSelectedListener {
    var binding: FragmentViewpagerBinding? = null

    var changeInPrice = 0.0f
    var endDate: String? = null
    var yesterdate_date: String? = null

    var standardRate: Float = 0.0f
    var exchangedCurrency: Float = 0.0f
    lateinit var spinnerArray : ArrayList<String>
    var mExecutorService = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())
    private var client: OkHttpClient? = null
    var responseString = ""
    var e_currency_responseString = ""
    lateinit var countriesList: java.util.ArrayList<Country>
    lateinit var rangedModelList: ArrayList<MetalInfoModel>

    lateinit var historicalValueSingleMetalList: ArrayList<MetalInfoModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewpagerBinding.inflate(
            inflater,
            container,
            false
        )
        historicalValueSingleMetalList = ArrayList()
        countriesList = ArrayList()
        client = OkHttpClient()
        rangedModelList = ArrayList()
        spinnerArray = ArrayList<String>()


        //get countryCode, country name and currency
        getCountriesList()
        //set type of metal in title
        setTitle()
        mExecutorService.execute {
            getSharedPreferencesSettings()
            //get json from the server in form of string
            getWebservice()

            var history_list = ArrayList<String>()
            history_list.add("This Week")
            history_list.add("Last Week")
            history_list.add("This Month")
            history_list.add("Last Month")
            history_list.add("Last 6 Months")
            history_list.add("1 Year")

            handler.post {
                binding!!.spinnerCurrency.adapter = setSpinnerContent(spinnerArray)
                Log.d("TAG", "onCreateView: "+spinnerArray.size.toString())
                binding!!.spinnerCurrency.setOnItemSelectedListener(this)
                //for default USD currency
                binding!!.spinnerCurrency.setSelection(236)

                //list of metall type
                var goldTypeList = ArrayList<String>()
                goldTypeList.add("Ounce")
                goldTypeList.add("Gram")
                goldTypeList.add("Tola")
                goldTypeList.add("KG")
                binding!!.spinnerGoldType.adapter = setSpinnerContent(goldTypeList)
                binding!!.spinnerGoldType.setOnItemSelectedListener(this)

                binding!!.spinnerHistory.adapter = setSpinnerContent(history_list)
                binding!!.spinnerHistory.setOnItemSelectedListener(this)
//                binding!!.spinnerHistory.setSelection(5)

                binding!!.spinnerHistory.setOnItemSelectedListener(object :
                    AdapterView.OnItemSelectedListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        val selected_val: String =
                            binding!!.spinnerHistory.getSelectedItem().toString()

                        when (selected_val) {
                            "This Week" -> {
                                binding!!.noHistory.visibility=View.GONE
                                binding!!.progressHistory.visibility=View.VISIBLE
                                binding!!.chartCore1.visibility=View.GONE
                                binding!!.chartCore2.visibility=View.GONE
                                binding!!.chartCore3.visibility=View.GONE
                                binding!!.chartCore4.visibility=View.GONE
                                binding!!.chartCore5.visibility=View.GONE
                                handler.postDelayed({
                                    val c = Calendar.getInstance().time
                                    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val endDate = df.format(c)  //today's Date
                                    val beginDate = getLastDate(7)  //date 1 week before

                                    getJsonFromTimeSeries(beginDate, endDate,binding!!.chartCore0)
                                    Log.d("TAG", "onItemSelected: " + beginDate + "  " + endDate)
                                }, 5000)
                            }
                            "Last Week" -> {
                                binding!!.noHistory.visibility=View.GONE
                                binding!!.progressHistory.visibility=View.VISIBLE
                                binding!!.chartCore0.visibility=View.GONE
                                binding!!.chartCore2.visibility=View.GONE
                                binding!!.chartCore3.visibility=View.GONE
                                binding!!.chartCore4.visibility=View.GONE
                                binding!!.chartCore5.visibility=View.GONE
                                handler.postDelayed({
                                    val endDate = getLastDate(8)
                                    val beginDate = getLastDate(14)


                                    Log.d("TAG", "onItemSelected: " + beginDate + "  " + endDate)
                                    getJsonFromTimeSeries(beginDate, endDate, binding!!.chartCore1)
                                }, 2000)
                            }
                            "This Month" -> {
                                binding!!.noHistory.visibility=View.GONE
                                binding!!.progressHistory.visibility=View.VISIBLE
                                binding!!.chartCore1.visibility=View.GONE
                                binding!!.chartCore0.visibility=View.GONE
                                binding!!.chartCore3.visibility=View.GONE
                                binding!!.chartCore4.visibility=View.GONE
                                binding!!.chartCore5.visibility=View.GONE
                                handler.postDelayed({
                                    val todaydate: LocalDate = LocalDate.now()
                                    val firstOfCurrentMonth: LocalDate =
                                        todaydate.with(ChronoField.DAY_OF_MONTH, 1)


                                    Log.d("TAG", "first of month: "+firstOfCurrentMonth)


                                    val c = Calendar.getInstance().time
                                    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val endDate = df.format(c)
//                                    val beginDate = getLastDate(30)
                                    val beginDate = firstOfCurrentMonth.toString()

                                    getJsonFromTimeSeries(beginDate, endDate, binding!!.chartCore2)

                                    Log.d("TAG", "onItemSelected: " + beginDate + "  " + endDate)
                                }, 2000)
                            }
                            "Last Month" -> {
                                binding!!.noHistory.visibility=View.GONE
                                binding!!.progressHistory.visibility=View.VISIBLE
                                binding!!.chartCore1.visibility=View.GONE
                                binding!!.chartCore2.visibility=View.GONE
                                binding!!.chartCore0.visibility=View.GONE
                                binding!!.chartCore4.visibility=View.GONE
                                binding!!.chartCore5.visibility=View.GONE
                                handler.postDelayed({

                                    val endDate = getLastDate(31)
                                    val beginDate = getLastDate(60)
                                    getJsonFromTimeSeries(beginDate, endDate, binding!!.chartCore3)

                                    Log.d("TAG", "onItemSelected: " + beginDate + "  " + endDate)
                                }, 2000)
                            }
                            "Last 6 Months" -> {
                                binding!!.noHistory.visibility=View.GONE
                                binding!!.progressHistory.visibility=View.VISIBLE
                                binding!!.chartCore1.visibility=View.GONE
                                binding!!.chartCore2.visibility=View.GONE
                                binding!!.chartCore3.visibility=View.GONE
                                binding!!.chartCore0.visibility=View.GONE
                                binding!!.chartCore5.visibility=View.GONE
                                handler.postDelayed({

                                    val c = Calendar.getInstance().time
                                    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val endDate = df.format(c)  //today's Date
                                    val beginDate = getLastDate(180)
                                    getJsonFromTimeSeries(beginDate, endDate, binding!!.chartCore4)
                                }, 2000)
                            }
                            "1 Year" -> {
                                binding!!.noHistory.visibility=View.GONE
                                binding!!.progressHistory.visibility=View.VISIBLE
                                binding!!.chartCore1.visibility=View.GONE
                                binding!!.chartCore2.visibility=View.GONE
                                binding!!.chartCore3.visibility=View.GONE
                                binding!!.chartCore4.visibility=View.GONE
                                binding!!.chartCore0.visibility=View.GONE
                                handler.postDelayed({

                                    val c = Calendar.getInstance().time
                                    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val endDate = df.format(c)  //today's Date
                                    val beginDate = getLastDate(365)
                                    getJsonFromTimeSeries(beginDate, endDate, binding!!.chartCore5)

                                    Log.d("TAG", "onItemSelected: " + beginDate + "  " + endDate)
                                }, 2000)
                            }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                })

                binding!!.spinnerCurrency.setOnItemSelectedListener(object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        binding!!.priceOfMetal.text = "-----"
                        //to get exchanged value from USD to others
                        var currency_extracted=extractCurrency(binding!!.spinnerCurrency.selectedItem.toString())
                        getExchangedCurrency(currency_extracted)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }

                })

                binding!!.spinnerGoldType.setOnItemSelectedListener(object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        arg0: AdapterView<*>?,
                        view: View?,
                        arg2: Int,
                        arg3: Long
                    ) {
                        binding!!.priceOfMetal.text = "-----"

                        val selected_val: String =
                            binding!!.spinnerGoldType.getSelectedItem().toString()

                        when (selected_val) {
                            "Ounce" -> {
                                var price = standardRate * exchangedCurrency
                                val formattedString = java.lang.String.format("%.02f", price)
                                binding!!.priceOfMetal.text = formattedString

                            }
                            "Gram" -> {
                                val inTola = (3 * standardRate) / 8
                                val inGram = inTola / 11.66
                                var price = inGram * exchangedCurrency
                                val formattedString = java.lang.String.format("%.02f", price)
                                binding!!.priceOfMetal.text = formattedString


                            }
                            "Tola" -> {
                                val inTola = (3 * standardRate) / 8
                                var price = inTola * exchangedCurrency
                                val formattedString = java.lang.String.format("%.02f", price)
                                binding!!.priceOfMetal.text = formattedString

                            }
                            "KG" -> {
                                val inTola = (3 * standardRate) / 8
                                val inKG = inTola * 85.735
                                var price = inKG * exchangedCurrency
                                val formattedString = java.lang.String.format("%.02f", price)
                                binding!!.priceOfMetal.text = formattedString

                            }
                        }
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>?) {
                    }
                })
            }
        }

        return binding!!.root
    }

    private fun extractCurrency(spinnerItem: String):String {

        var s = spinnerItem.substring(spinnerItem.indexOf("-") + 1);
        s = s.substring(0, s.indexOf("-"));

        return s
    }

    private fun getJsonFromTimeSeries(beginDate: String, endDate: String, chartCore: ChartCoreView) {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date1: Date = sdf.parse(beginDate)
        val date2: Date = sdf.parse(endDate)

        Log.d("TAG", "getJsonFromTimeSeries: " + date1 + " " + date2)
//        Log.d("TAG", "getJsonFromTimeSeries: " +historicalValueSingleMetalList.size)

        if (historicalValueSingleMetalList.size != 0) {
            chartCore.visibility = View.VISIBLE
            binding!!.progressHistory.visibility=View.GONE
            binding!!.noHistory.visibility = View.GONE

            rangedModelList = ArrayList()
            for (i in 0 until historicalValueSingleMetalList.size) {
                var dt: Date = sdf.parse(historicalValueSingleMetalList.get(i).date)
                if ((dt.after(date1) || dt.equals(date1)) && (dt.before(date2) || dt.equals(date2))) {
                    rangedModelList.add(historicalValueSingleMetalList.get(i))
//                    Log.d("TAG", "getJsonFromTimeSeries: " + rangedModelList.get(i).date)
                }
            }

            //set chart values according to API
            setChainChart(chartCore)


        } else {
//            Toast.makeText(requireContext(), "Not connected to the server!", Toast.LENGTH_SHORT)
//                .show()
            chartCore.visibility = View.GONE
            binding!!.progressHistory.visibility=View.GONE
            binding!!.noHistory.visibility = View.VISIBLE
        }


//        Log.d("TAG", "getJsonFromTimeSeries: " + rangedModelList.size)
    }

    fun getLastDate(value: Int): String {
        val c = Calendar.getInstance().time
        val calendar = Calendar.getInstance()
        calendar.time = c
        calendar.add(Calendar.DAY_OF_YEAR, -value)
        val newDate = calendar.time
        val df1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate1 = df1.format(newDate)
        return formattedDate1
    }

    fun getSharedPreferencesSettings() {
        Log.d("TAG", "loadContent: " + "Hello News")

        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val formattedDate = df.format(c)
        endDate = formattedDate

    }


    fun setTitle() {
        when (metalType) {
            "gold" -> {
                binding!!.titleMetalpice.text = "Gold Price"
            }
            "silver" -> {
                binding!!.titleMetalpice.text = "Silver Price"
            }
            "platinum" -> {
                binding!!.titleMetalpice.text = "Platinum Price"
            }
        }
    }

    //list of countries along with countryCode, and currency
    fun getCountriesList() {
        val countryCodes: Array<String> = Locale.getISOCountries()

        for (countryCode in countryCodes) {
            val obj = Locale("", countryCode)
            System.out.println(
                "Country Code = " + obj.getCountry().toString() +
                        ", Country Name = " + obj.getDisplayCountry() + ", Currency : " + getCurrencySymbol(
                    countryCode
                )
            )
            spinnerArray.add(obj.getDisplayCountry().toString()+" -"+getCurrencySymbol(countryCode).toString()+"- "+getSymbol(countryCode).toString())
            countriesList.add(
                Country(
                    obj.getCountry().toString(), obj.getDisplayCountry().toString(),
                    getCurrencySymbol(countryCode).toString(),getSymbol(countryCode).toString()
                )
            )
        }

        Collections.sort(spinnerArray);
    }

    //set spinner list for metal measurements and currencies
    fun setSpinnerContent(currencyList: ArrayList<String>): SpinnerAdapter {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, currencyList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    //get currency for according to country code
    fun getCurrencySymbol(countryCode: String?): String? {
        var currencyCode = ""
        var locale: Locale? = null
        var currency: Currency? = null
        try {
            locale = Locale("", countryCode)
            currency = Currency.getInstance(locale)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        if (currency != null) {
            currencyCode = currency.currencyCode
        }

        return currencyCode
    }
    //get currency symbol for according to country code
    fun getSymbol(countryCode: String?): String? {
        var currencySymbol = ""
        var locale: Locale? = null
        var currency: Currency? = null
        try {
            locale = Locale("", countryCode)
            currency = Currency.getInstance(locale)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        if (currency != null) {
            currencySymbol = currency.symbol
        }

        return currencySymbol
    }

    //get live price of metal
    private fun getWebservice() {
        val policy: StrictMode.ThreadPolicy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        mExecutorService.execute {
            var startDate = getLastDate(364)
            val request: Request = Request.Builder()
                .url("https://beta-angelstech.com/beta/metal-api/get_metals.php?start_date=" + startDate + "&end_date=" + endDate)
                .get().build()
            handler.post {
                client!!.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        requireActivity().runOnUiThread { binding!!.priceOfMetal.setText("0.00") }
                        Log.d("TAG result", "getJsonValuefail: " + "failure")
                    }
                    override fun onResponse(call: Call, response: Response) {
                        requireActivity().runOnUiThread {
                            try {
                                responseString = response.body!!.string()
                                Log.d("TAG result", "getJsonValuesuccess: " + responseString)
                                var metal = ""
                                if (metalType.equals("gold")) {
                                    metal = "XAU"
                                } else if (metalType.equals("silver")) {
                                    metal = "XAG"

                                } else {
                                    metal = "XPT"
                                }
                                if (!responseString.contains("Connection failed: Access denied for user ")) {
                                    getJsonValue(metal)
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        responseString,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }

                            } catch (ioe: IOException) {
                                binding!!.priceOfMetal.setText("Error during getting body")
                            }
                        }
                    }

                })
            }
        }
    }

    //get value of price of metal from the jason obtained from the response of API
    fun getJsonValue(key: String) {
        //list of historical data for the metal selected
        var jsonItemsList = ArrayList<JSONObject>()
        val jsonArray = JSONArray(responseString)

        for (n in 0 until jsonArray.length()) {
            val `object`: JSONObject = jsonArray.getJSONObject(n)
            jsonItemsList.add(`object`)
            historicalValueSingleMetalList.add(
                MetalInfoModel(
                    `object`.get(key.lowercase()).toString(), `object`.get("on_date").toString()
                )
            )
        }

        val convertedVal = convert_float_to_standard(
            historicalValueSingleMetalList.get(historicalValueSingleMetalList.lastIndex).price.toFloat()
        )

        standardRate = convertedVal
        Log.d("TAG", "convert_float_to_standard: "+historicalValueSingleMetalList.get(historicalValueSingleMetalList.lastIndex).price)

        val formattedString = java.lang.String.format("%.02f", convertedVal)
        binding!!.priceOfMetal.text = formattedString


        //show current date
        binding!!.dateLastupdate.text = historicalValueSingleMetalList.get(historicalValueSingleMetalList.lastIndex).date

        setChangeInPrice()
    }

    fun convert_float_to_standard(value: Float): Float {
        //get latest value from the list and set to metal price
        val decimalResult: Float = value

        val convertedVal = 1 / decimalResult
        return convertedVal
    }

    fun setChangeInPrice() {
        var yesterday_price = convert_float_to_standard(
            historicalValueSingleMetalList.get(historicalValueSingleMetalList.lastIndex - 1).price.toFloat()
        )
        var today_price = convert_float_to_standard(
            historicalValueSingleMetalList.get(historicalValueSingleMetalList.lastIndex).price.toFloat()
        )
        changeInPrice = today_price - yesterday_price
        val formattedString = java.lang.String.format("%.02f", changeInPrice)
        binding!!.changeInPriceId.text = formattedString.toString()
    }


    //set chart for historical data of price fluctuation
    fun setChainChart(chartCore: ChartCoreView) {
        if (rangedModelList.size != 0) {

            chartCore.visibility=View.VISIBLE
            binding!!.noHistory.visibility=View.GONE

            var pricesList_x_axis = ArrayList<String>()
            var datesList_y_axis = ArrayList<String>()
//            Log.d("TAG", "setChainChart: " + rangedModelList.size)

//            datesList_y_axis.add("Price Difference")
            for (i in 0 until rangedModelList.size) {
                //,ake list of prices from the ranged list of model e.g 2343,2134,1243,2133
//                var float_price = 1 / rangedModelList.get(i).price.toString().toFloat()
                var float_price = (3*(1 / rangedModelList.get(i).price.toString().toFloat()))/8
                Log.d("TAG", "setChainChart: " + float_price)
                val formattedString = java.lang.String.format("%.02f", float_price)

                pricesList_x_axis.add(formattedString)

                //make date list from model list in format : dd MM
                var date_item = rangedModelList.get(i).date
                datesList_y_axis.add(date_item)

            }
            var varianceList = ArrayList<Double>()
//            varianceList.add(10f)
            varianceList.clear()
            for (i in 0 until pricesList_x_axis.size) {
                var item = pricesList_x_axis.get(i).toString().toDouble()
                Log.d("TAG", "setChainChart: " + item)
                varianceList.add(item)

            }

            var coreData = ChartData()
                .addDataset(
                    ChartNumberDataset()
                        .data(varianceList)
                        .label("USD - Per Tola")
                        .offset(10)
                        .borderColor("#11CA9D")
                )
                .labels(datesList_y_axis)

            var chartOptions = ChartOptions()
                .plugin(
                    Plugin()
                        .tooltip(
                            Tooltip(true)
                        )
                        .customCanvasBackgroundColor(BackgroundColor("#11CA9D"))
                )
                .elements(
                    Elements()
                        .line(
                            com.github.chartcore.data.option.elements.Line().tension(0.05f)
                                .borderColor("#11CA9D")
                        )
                )

            var chartModel = ChartCoreModel()
                .type(ChartTypes.LINE)
                .data(coreData)
                .options(chartOptions)
            chartCore.draw(chartModel)

        } else {
            binding!!.noHistory.visibility=View.VISIBLE
            chartCore.visibility=View.GONE
//            Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show()
        }
    }

    //get currency rates according to standard USD
    fun getExchangedCurrency(to: String) {
        val request: Request = Request.Builder()
            .url("https://api.exchangerate.host/convert?from=USD&to=" + to).get().build()
        handler.post {
            client!!.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requireActivity().runOnUiThread { binding!!.priceOfMetal.setText("0.00") }
                    Log.d("TAG currency", "getJsonValuefail: " + "failure")
                }

                override fun onResponse(call: Call, response: Response) {
                    requireActivity().runOnUiThread {
                        try {
                            e_currency_responseString = response.body!!.string()
                            setPrice()

                        } catch (ioe: IOException) {
                            binding!!.priceOfMetal.setText("Error during get body")
                        }
                    }
                }

            })
        }

    }

    //set values for spinner according to item selected and multiplied with currency rates
    fun setPrice() {
        val objectJson = JSONObject(e_currency_responseString)
        var infoList = objectJson.getString("info")
        Log.d("TAG", "setPrice: " + infoList) // setPrice: {"rate":0.936757}
        infoList = infoList.substring(8, infoList.length - 1) //value like  : 0.936757
        val currencyRate: Float = infoList.toFloat()
        exchangedCurrency = currencyRate
        var convertedPrice = 0.0f
        var formattedString = ""
        when (binding!!.spinnerGoldType.selectedItem) {
            "Ounce" -> {
                convertedPrice = standardRate * currencyRate


            }
            "Gram" -> {
                val inTola = (3 * standardRate) / 8
                val inGram = inTola / 11.66
                convertedPrice = (inGram * currencyRate).toFloat()

            }
            "Tola" -> {
                val inTola = (3 * standardRate) / 8
                convertedPrice = (inTola * currencyRate).toFloat()
            }
            "KG" -> {
                val inTola = (3 * standardRate) / 8
                convertedPrice = (inTola * 85.735).toFloat()


            }
        }
        formattedString = java.lang.String.format("%.02f", convertedPrice)
        binding!!.priceOfMetal.text = formattedString
    }

}


