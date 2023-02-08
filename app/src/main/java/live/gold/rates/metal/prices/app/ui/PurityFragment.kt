package live.gold.rates.metal.prices.app

import android.R
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import live.gold.rates.metal.prices.app.Adaptors.PurityAdaptor
import live.gold.rates.metal.prices.app.Model.Country
import live.gold.rates.metal.prices.app.Model.PurityModel
import live.gold.rates.metal.prices.app.databinding.FragmentPurityBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class PurityFragment : Fragment(), AdapterView.OnItemSelectedListener  {

    //copied content fro viewpager
    lateinit var childList: ArrayList<JSONObject>
    lateinit var purityModel: ArrayList<PurityModel>
    var e_currency_responseString = ""
    lateinit var convertedString: ArrayList<String>
    var exchangedCurrency: Float = 1.0f
    lateinit var standardRate: ArrayList<Float>

    var spinnerArray = ArrayList<String>()
    var mExecutorService = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())
    private var client: OkHttpClient? = null
    var responseString = ""
    lateinit var countriesList: java.util.ArrayList<Country>
    lateinit var KeysList: ArrayList<String>

    //    lateinit var ValueList: ArrayList<Any>
    lateinit var purityList: kotlin.collections.ArrayList<String>
    lateinit var valueList: kotlin.collections.ArrayList<String>


    lateinit var binding: FragmentPurityBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        getWebservice()
        getCountriesList()
        binding.spinnerGoldType.setSelection(0)
        binding.spinnerCurrency.adapter = setSpinnerContent(spinnerArray)
        binding.spinnerCurrency.setOnItemSelectedListener(this)
        //for default USD currency
        binding.spinnerCurrency.setSelection(236)

        //list of metall type
        var goldTypeList = java.util.ArrayList<String>()

        goldTypeList.add("Ounce")
        goldTypeList.add("Gram")
        goldTypeList.add("Tola")
        goldTypeList.add("KG")
        binding.spinnerGoldType.adapter = setSpinnerContent(goldTypeList)
        binding.spinnerGoldType.setOnItemSelectedListener(this)

        binding.spinnerGoldType.adapter = setSpinnerContent(goldTypeList)
        binding.spinnerGoldType.setOnItemSelectedListener(this)

        binding.spinnerCurrency.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //to get exchanged value from USD to others
                var currency_extracted=extractCurrency(binding!!.spinnerCurrency.selectedItem.toString())
                getExchangedCurrency(currency_extracted)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        })

        var dummyList=purityModel
        binding.spinnerGoldType.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, view: View?, arg2: Int, arg3: Long) {
//                binding!!.priceOfMetal.text="-----"
                var formattedString = ""
                val selected_val: String = binding!!.spinnerGoldType.getSelectedItem().toString()
                for (i in 0 until standardRate.size) {

                    when (selected_val) {
                        "Ounce" -> {
                            var price = standardRate.get(i) * exchangedCurrency
                            formattedString = java.lang.String.format("%.02f", price)
                            val purityUpdated=purityModel.get(i).purity
                            purityModel.set(i, PurityModel(purityUpdated,formattedString))
                            setAdaptor()
                        }
                        "Gram" -> {
                            val inTola = (3 * standardRate.get(i)) / 8
                            val inGram = inTola / 11.66
                            var price = inGram * exchangedCurrency
                            formattedString = java.lang.String.format("%.02f", price)
                            val purityUpdated=purityModel.get(i).purity
                            purityModel.set(i, PurityModel(purityUpdated,formattedString))
                            setAdaptor()
                        }
                        "Tola" -> {
                            val inTola = (3 * standardRate.get(i)) / 8
                            var price = inTola * exchangedCurrency
                            formattedString = java.lang.String.format("%.02f", price)
                            val purityUpdated=purityModel.get(i).purity
                            purityModel.set(i, PurityModel(purityUpdated,formattedString))
                            setAdaptor()
                        }
                        "KG" -> {
                            val inTola = (3 * standardRate.get(i)) / 8
                            val inKG = inTola * 85.735
                            var price = inKG * exchangedCurrency
                            formattedString = java.lang.String.format("%.02f", price)
                            val purityUpdated=dummyList.get(i).purity
                            purityModel.set(i, PurityModel(purityUpdated,formattedString))
                            setAdaptor()
                        }
                    }

                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {
            }
        })


    }

    fun getWebservice() {
        if (Build.VERSION.SDK_INT > 9) {
            val policy: StrictMode.ThreadPolicy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        mExecutorService.execute {

            val request: Request = Request.Builder()
                .url("https://beta-angelstech.com/beta/metal-api/xau.php")
                .get().build()

            handler.post {
                client!!.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        binding.progressPuritylist.visibility=View.GONE
                        binding.linearData.visibility=View.GONE
                        binding.historyImg.visibility=View.VISIBLE
                    }

                    override fun onResponse(call: Call, response: Response) {
                        requireActivity().runOnUiThread {
                            try {
                                responseString = response.body!!.string()
                                Log.d("TAG", "onResponse: "+responseString)
                                createSeperateList(responseString)

                            } catch (ioe: IOException) {
                                Toast.makeText(requireContext(), "Error while fetching data from the server!", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }

                })
            }
        }
    }

    fun convert_float_to_standard(value: Float): Float {
        //get latest value from the list and set to metal price
        val decimalResult: Float = value

        val convertedVal = 1 / decimalResult
        return convertedVal
    }

    fun extractCurrency(spinnerItem: String):String {

        var s = spinnerItem.substring(spinnerItem.indexOf("-") + 1);
        s = s.substring(0, s.indexOf("-"));

        return s
    }

    fun setAdaptor() {
        binding.progressPuritylist.visibility=View.GONE
        binding!!.recyclerViewPurities.layoutManager = LinearLayoutManager(requireContext())
        binding!!.recyclerViewPurities.adapter =
            PurityAdaptor(requireContext(), purityModel)
    }

    fun initializeVariables() {
        KeysList = ArrayList()
        childList = ArrayList()
        convertedString = ArrayList<String>()
        purityList = ArrayList<String>()
        valueList = ArrayList<String>()
        countriesList = ArrayList()
        client = OkHttpClient()
        standardRate = ArrayList<Float>()
        purityModel = ArrayList<PurityModel>()
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
        Collections.sort(spinnerArray)
    }

    //get currency symbol according to country code
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

    fun setSpinnerContent(currencyList: ArrayList<String>): SpinnerAdapter {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item, currencyList
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        return adapter

    }

    fun getCurrencySymbol(countryCode: String?): String? {
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
            currencySymbol = currency.currencyCode
        }
        return currencySymbol
    }

    fun createSeperateList(str: String)
    {
        var strNew=str.substring(1,str.length-1)

        var output = strNew.split(",".toRegex()).toTypedArray()
        for (i in output.indices) {
            processLineString(output[i].toString(), i)
        }
//        binding.progressPuritylist.visibility=View.GONE
//        binding.recyclerViewPurities.visibility=View.VISIBLE
//        setAdaptor()

        var formattedString = ""
        for (i in 0 until standardRate.size) {
            var price = standardRate.get(i) * exchangedCurrency

            formattedString = java.lang.String.format("%.03f", price)
            val purityUpdated=purityModel.get(i).purity
            purityModel.set(i, PurityModel(purityUpdated,formattedString))
            setAdaptor()
        }

    }

    fun processLineString(line1: String, i: Int) {
        var purity = line1.substring(0, line1.indexOf(':'))
        var value = line1.substring(line1.indexOf(':') + 1, line1.length)
        purity=purity.substring(1,purity.length-1)
        value=value.substring(1,value.length-1)

        Log.d("TAG json created", "processLineString: " + purity + " , " + value)

        val decimalResult: Float =value.toFloat()
        standardRate.add(decimalResult*141.7476.toFloat())
        val formattedString = java.lang.String.format("%.003f", decimalResult)
        purityModel.add(PurityModel(purity, formattedString.toString()))

    }

    fun getExchangedCurrency(to: String) {
        val request: Request = Request.Builder()
            .url("https://api.exchangerate.host/convert?from=USD&to=" + to).get().build()

        handler.post {
            client!!.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("TAG currency", "getJsonValuefail: " + "failure")
                }

                override fun onResponse(call: Call, response: Response) {

                    requireActivity().runOnUiThread {
                        try {
                            e_currency_responseString = response.body!!.string()
                            setPrice()

                        } catch (ioe: IOException) {
//                            binding!!.priceOfMetal.setText("Error during get body")
                        }
                    }
                }

            })
        }
    }

    fun setPrice() {
        val objectJson = JSONObject(e_currency_responseString)
        var infoList = objectJson.getString("info")
        Log.d("TAG", "setPrice: " + infoList) // setPrice: {"rate":0.936757} for EUR

        infoList = infoList.substring(8, infoList.length - 1) //value like  : 0.936757
        val currencyRate: Float = infoList.toFloat()
        exchangedCurrency = currencyRate

        for (i in 0 until standardRate.size) {
            when (binding!!.spinnerGoldType.selectedItem) {
                "Ounce" -> {
                    var value=standardRate.get(i) * currencyRate
                    val formattedString = java.lang.String.format("%.02f", value)

                    purityModel.set(i, PurityModel(purityModel.get(i).purity,formattedString.toString()))
                    setAdaptor()
                }
                "Gram" -> {
                    val inTola = (3 * standardRate.get(i)) / 8
                    val inGram = inTola / 11.66
                    var value=(inGram * currencyRate).toFloat()
                    val formattedString = java.lang.String.format("%.02f", value)

                    purityModel.set(i, PurityModel(purityModel.get(i).purity,formattedString.toString()))
                    setAdaptor()

                }
                "Tola" -> {
                    val inTola = (3 * standardRate.get(i)) / 8
                    var value=(inTola * currencyRate).toFloat()
                    val formattedString = java.lang.String.format("%.02f", value)

                    purityModel.set(i, PurityModel(purityModel.get(i).purity,formattedString.toString()))
                    setAdaptor()

                }
                "KG" -> {
                    val inTola = (3 * standardRate.get(i)) / 8
                    var value=((inTola * 85.735).toFloat())
                    val formattedString = java.lang.String.format("%.02f", value)

                    purityModel.set(i, PurityModel(purityModel.get(i).purity,formattedString.toString()))
                    setAdaptor()

                }
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}